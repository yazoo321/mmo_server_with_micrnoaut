package server.combat.model;

import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.attribute.status.model.ActorStatus;

@Data
@AllArgsConstructor
@Serdeable
@ReflectiveAccess
@NoArgsConstructor
public class CombatData {

    private String actorId;

    Map<String, Double> derivedStats;

    ActorStatus actorStatus;

    Set<String> aggregatedStatusEffects;
    Map<String, Double> aggregatedStatusDerived;

    private Instant mainHandLastAttack;
    private Instant offhandLastAttack;

    Map<String, Boolean> attackSent;

    private Set<String> targets;

    private Instant lastHelperNotification;

    boolean isPlayer;

    public CombatData(String actorId) {
        this.setActorId(actorId);
        this.mainHandLastAttack = Instant.now().minusSeconds(20);
        this.offhandLastAttack = Instant.now().minusSeconds(20);
        this.targets = new HashSet<>();
        this.lastHelperNotification = Instant.now().minusSeconds(20);
        this.attackSent = new HashMap<>();
        this.derivedStats = new HashMap<>();
        this.aggregatedStatusDerived = new HashMap<>();
        this.aggregatedStatusEffects = new HashSet<>();

        try {
            UUID.fromString(actorId);
            isPlayer = false;
        } catch (IllegalArgumentException exception) {
            isPlayer = true;
        }
    }
}