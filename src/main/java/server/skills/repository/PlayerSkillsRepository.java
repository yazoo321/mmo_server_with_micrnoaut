package server.skills.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.rxjava3.core.Single;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import server.common.configuration.MongoConfiguration;
import server.session.SessionParamHelper;
import server.skills.model.ActorSkills;
import server.skills.model.Skill;

@Slf4j
@Singleton
public class PlayerSkillsRepository {

    MongoConfiguration configuration;
    MongoClient mongoClient;
    MongoCollection<ActorSkills> playerSkillsMongoCollection;

    @Inject SessionParamHelper sessionParamHelper;

    public PlayerSkillsRepository(MongoConfiguration configuration, MongoClient mongoClient) {
        this.configuration = configuration;
        this.mongoClient = mongoClient;
        prepareCollections();
    }

    public Single<ActorSkills> getActorSkills(String actorId) {
        return Single.fromPublisher(playerSkillsMongoCollection.find(eq("actorId", actorId)))
                .doOnError((exception) -> log.error("actor skills not found for {}", actorId));
    }

    public Single<ActorSkills> setActorSkills(String actorId, List<Skill> skills) {
        return Single.fromPublisher(
                playerSkillsMongoCollection.findOneAndUpdate(
                        eq("actorId", actorId), set("skills", skills)));
    }

    //
    //    public Map<String, Instant>  getActorSkillsInUse(String actorId) {
    //        CombatData combatData = sessionParamHelper.getSharedActorCombatData(actorId);
    //        Map<String, Instant> activatedSkills = combatData.getActivatedSkills();
    //
    //        return activatedSkills;
    //    }

    private void prepareCollections() {
        this.playerSkillsMongoCollection =
                mongoClient
                        .getDatabase(configuration.getDatabaseName())
                        .getCollection(configuration.getPlayerSkills(), ActorSkills.class);
    }
}
