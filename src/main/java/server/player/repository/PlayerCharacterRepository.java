package server.player.repository;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subscribers.DefaultSubscriber;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.validation.Valid;
import server.common.configuration.MongoConfiguration;
import server.common.dto.Motion;
import server.player.exceptions.CharacterException;
import server.player.model.Character;

@Singleton
public class PlayerCharacterRepository {
    // This repository is connected to MongoDB
    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<Character> characters;

    public PlayerCharacterRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        this.characters = getCollection();
    }

    @PostConstruct
    public void createIndex() {
        // Micronaut does not yet support index annotation, we have to create manually
        // https://www.javaer101.com/en/article/20717814.html

        characters
                .createIndex(Indexes.text("name"))
                .subscribe(
                        new DefaultSubscriber<String>() {
                            @Override
                            public void onNext(String s) {
                                System.out.format("Index %s was created.%n", s);
                            }

                            @Override
                            public void onError(Throwable t) {
                                t.printStackTrace();
                            }

                            @Override
                            public void onComplete() {
                                System.out.println("Completed");
                            }
                        });
    }

    public Character save(@Valid Character character) {
        Character ch = findByName(character.getName());
        if (ch == null) {
            return Single.fromPublisher(characters.insertOne(character))
                    .map(success -> character)
                    .blockingGet();
        } else {
            // don't currently support update all fields
            return null;
        }
    }

    public UpdateResult updateMotion(Motion motion, String actorId) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return Flowable.fromPublisher(
                        characters.updateOne(
                                eq("name", actorId),
                                combine(
                                        set("motion", motion),
                                        set("updatedAt", now),
                                        set("isOnline", true))))
                .firstElement()
                .blockingGet();
    }

    public Character createNew(@Valid Character character) {
        // detect if we find character
        boolean exists = findByName(character.getName()) != null;

        if (exists) {
            // change to another error
            // this way we can keep the interface of .blockingGet and avoid nullptr ex
            throw new CharacterException("Character name already exists");
        }

        return save(character);
    }

    public Character findByName(String name) {
        // TODO: Ignore case
        try {
            return Single.fromPublisher(characters.find(eq("name", name))).blockingGet();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public List<Character> findByName(List<String> names) {
        return Flowable.fromPublisher(characters.find(in("name", names))).toList().blockingGet();
    }

    public Single<List<Character>> findByNames(Set<String> actorIds) {
        return Flowable.fromPublisher(characters.find(in("name", actorIds))).toList();
    }

    public List<Character> findByAccount(String accountName) {
        // TODO: Ignore case
        return Flowable.fromPublisher(characters.find(eq("accountName", accountName)))
                .toList()
                .blockingGet();
    }

    public DeleteResult deleteByActorId(String name) {
        // TODO: should be deleteOne, but sometimes tests can flake
        return Single.fromPublisher(characters.deleteMany(eq("name", name))).blockingGet();
    }

    public List<Character> getPlayersNear(String actorId) {
        // gets players near <player name>
        // require to add additional filters such as filter by location/town/etc

        return Flowable.fromPublisher(
                        characters.find(and(ne("name", actorId), eq("isOnline", true))))
                .toList()
                .blockingGet();
    }

    public UpdateResult checkAndUpdateUserOnline() {
        LocalDateTime logoutTime = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5);

        // if is online and not updated in the last 5 mins, set to logged out.
        return Flowable.fromPublisher(
                        characters.updateMany(
                                combine(eq("isOnline", true), lt("updatedAt", logoutTime)),
                                set("isOnline", false)))
                .firstElement()
                .blockingGet();
    }

    private MongoCollection<Character> getCollection() {
        return mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getPlayerCharacterCollection(), Character.class);
    }
}
