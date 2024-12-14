package server.attribute.status.service;

import static org.awaitility.Awaitility.await;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.stats.types.StatsTypes;
import server.attribute.status.helpers.StatusTestHelper;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.model.derived.Burning;
import server.attribute.status.model.derived.Dead;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatusServiceTest {

    @Inject StatusTestHelper statusTestHelper;

    @Inject StatusService statusService;

    @Inject StatsService statsService;

    private static final String TEST_ACTOR = "actor1";

    @BeforeEach
    void reset() {
        statusTestHelper.resetStatuses(List.of(TEST_ACTOR));
        statsService.deleteStatsFor(TEST_ACTOR).blockingSubscribe();
        statsService.initializePlayerStats(TEST_ACTOR).blockingSubscribe();
    }

    // Test case class for parameterized testing
    static class TestCase {
        String actorId;
        ActorStatus initialStatus;
        ActorStatus expectedStatus;

        public TestCase(String actorId, ActorStatus initialStatus, ActorStatus expectedStatus) {
            this.actorId = actorId;
            this.initialStatus = initialStatus;
            this.expectedStatus = expectedStatus;
        }
    }

    static Stream<TestCase> inputs() {
        ActorStatus initialStatus = new ActorStatus("actor1", new HashSet<>(), false, Set.of());

        return Stream.of(new TestCase("actor1", initialStatus, initialStatus));
    }

    @ParameterizedTest
    @MethodSource("inputs")
    @DisplayName("Test addStatusToActor")
    void testAddStatusToActor(TestCase testCase) {
        statusService.addStatusToActor(testCase.initialStatus, Set.of(new Dead()));

        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(2, ChronoUnit.SECONDS))
                .until(
                        () -> {
                            ActorStatus actorStatus =
                                    statusService.getActorStatus(testCase.actorId).blockingGet();

                            Set<String> statusCategories =
                                    actorStatus.getActorStatuses().stream()
                                            .map(Status::getCategory)
                                            .collect(Collectors.toSet());

                            return statusCategories.contains("DEAD");
                        });
    }

    @Test
    void addBurningStateToActor() throws IOException {
        //        Instant expiration, String sourceId, Double damage
        Instant expiration = Instant.now().plusMillis(950);
        String source = "actor2";
        Double damage = 40.0;
        Status burning = new Burning(expiration, source, damage);

        Stats initialStats = statsService.getStatsFor(TEST_ACTOR).blockingGet();
        Double initialHp = initialStats.getDerived(StatsTypes.CURRENT_HP);
        String out = String.format("Initial HP: %s", initialHp);
        System.out.println(out);

        // Add padding to damage due to HP regen
        Double expectedLife =
                initialStats.getDerived(StatsTypes.CURRENT_HP) - (damage * 3) + (damage * 0.7);

        ActorStatus initialStatus = new ActorStatus(TEST_ACTOR, new HashSet<>(), false, Set.of());

        // when
        statusService.addStatusToActor(initialStatus, Set.of(burning));

        // then
        await().pollDelay(300, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(3, ChronoUnit.SECONDS))
                .until(
                        () -> {
                            ActorStatus actorStatus =
                                    statusService.getActorStatus(TEST_ACTOR).blockingGet();
                            actorStatus.aggregateStatusEffects();
                            return actorStatus.getStatusEffects().contains(burning.getCategory());
                        });

        await().pollDelay(200, TimeUnit.MILLISECONDS)
                .timeout(Duration.of(4, ChronoUnit.SECONDS))
                .until(
                        () -> {
                            Stats actorStats = statsService.getStatsFor(TEST_ACTOR).blockingGet();
                            Double currentHp = actorStats.getDerived(StatsTypes.CURRENT_HP);
                            Double diff = initialHp - currentHp;
                            String out2 = String.format("HP now: %s, diff: %s", currentHp, diff);
                            System.out.println(out2);

                            return actorStats.getDerived(StatsTypes.CURRENT_HP) < expectedLife;
                        });
    }
}
