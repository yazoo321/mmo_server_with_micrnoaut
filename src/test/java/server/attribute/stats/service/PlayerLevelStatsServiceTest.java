package server.attribute.stats.service;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import server.attribute.stats.helpers.StatsTestHelper;
import server.attribute.stats.model.Stats;
import server.attribute.stats.model.types.ClassTypes;
import server.attribute.stats.types.StatsTypes;

import static org.awaitility.Awaitility.await;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerLevelStatsServiceTest {

    @Inject PlayerLevelStatsService playerLevelStatsService;

    @Inject StatsTestHelper statsTestHelper;

    @Inject StatsService statsService;

    private static final String ACTOR_ID = "TEST_PLAYER";

    private static Stream<Arguments> availableClasses() {
        return Stream.of(
                Arguments.of(ClassTypes.CLERIC.getType()),
                Arguments.of(ClassTypes.FIGHTER.getType()),
                Arguments.of(ClassTypes.MAGE.getType()),
                Arguments.of(ClassTypes.RANGER.getType()));
    }

    @BeforeEach
    void cleanup() {
        statsTestHelper.deleteAllAttributeData();
    }

    @AfterAll
    void clean() {
        statsTestHelper.deleteAllAttributeData();
    }

    @ParameterizedTest
    @MethodSource("availableClasses")
    void whenCreatingNewCharacterOfClassTheDataIsSetupAsExpected(String characterClass) {
        // given
        // user has to pre-initialize
        statsService.initializePlayerStats(ACTOR_ID).blockingSubscribe();
        Map<String, Integer> expectedTags =
                buildBaseExpectedNumTagsForCharacterClass(characterClass, 1);

        // when
        playerLevelStatsService
                .initializeCharacterClass(ACTOR_ID, characterClass)
                .blockingSubscribe();

        // then
        Stats stats = statsService.getStatsFor(ACTOR_ID).blockingGet();
        Map<String, Integer> actualBaseAttr = stats.getBaseStats();

        Assertions.assertThat(actualBaseAttr).containsAllEntriesOf(expectedTags);
    }

    @ParameterizedTest
    @MethodSource("availableClasses")
    void handleLevelUp(String characterClass) {
        // given
        statsService.initializePlayerStats(ACTOR_ID).blockingSubscribe();
        playerLevelStatsService
                .initializeCharacterClass(ACTOR_ID, characterClass)
                .blockingSubscribe();

        // you need enough XP to level:
        playerLevelStatsService.addPlayerXp(ACTOR_ID, 1_000_000).blockingSubscribe();

        // when
        playerLevelStatsService.handleLevelUp(ACTOR_ID, characterClass).blockingSubscribe();
        Map<String, Integer> expectedTags =
                buildBaseExpectedNumTagsForCharacterClass(characterClass, 2);

        // then
        Stats stats = statsService.getStatsFor(ACTOR_ID).blockingGet();
        Map<String, Integer> actualBaseAttr = stats.getBaseStats();

        Assertions.assertThat(actualBaseAttr).containsAllEntriesOf(expectedTags);
    }

    @Test
    void addXpToCharacter() {
        // given
        statsService.initializePlayerStats(ACTOR_ID).blockingSubscribe();
        playerLevelStatsService
                .initializeCharacterClass(ACTOR_ID, ClassTypes.FIGHTER.getType())
                .blockingSubscribe();

        Integer expectedXp = 700;

        // when
        playerLevelStatsService.addPlayerXp(ACTOR_ID, 500).blockingSubscribe();
        playerLevelStatsService.addPlayerXp(ACTOR_ID, 200).blockingSubscribe();

        // then

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(2, ChronoUnit.SECONDS))
                .until(() -> {
                    Stats stats = statsService.getStatsFor(ACTOR_ID).blockingGet();

                    Integer actualXp = stats.getBaseStat(StatsTypes.XP);
                    return actualXp.equals(expectedXp);
                });

        Stats stats = statsService.getStatsFor(ACTOR_ID).blockingGet();
        Integer actualXp = stats.getBaseStat(StatsTypes.XP);
        Assertions.assertThat(actualXp).isEqualTo(expectedXp);
    }

    private Map<String, Integer> buildBaseExpectedNumTagsForCharacterClass(
            String characterClass, Integer expectedLevel) {
        List<String> classesAvailable = PlayerLevelStatsService.AVAILABLE_CLASSES;
        Map<String, Integer> expectedTags = new HashMap<>();
        classesAvailable.forEach(
                c -> expectedTags.put(c, c.equals(characterClass) ? expectedLevel : 0));

        return expectedTags;
    }
}
