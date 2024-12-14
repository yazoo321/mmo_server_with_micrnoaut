package server.attribute.status.model.derived;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.micronaut.serde.annotation.Serdeable;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import server.attribute.stats.model.Stats;
import server.attribute.stats.service.StatsService;
import server.attribute.stats.types.DamageTypes;
import server.attribute.status.model.ActorStatus;
import server.attribute.status.model.Status;
import server.attribute.status.service.StatusService;
import server.attribute.status.types.StatusTypes;

@Data
@Slf4j
@Serdeable
@JsonTypeName("BLEEDING")
@EqualsAndHashCode(callSuper = false)
public class Bleeding extends Status {

    public Bleeding(Instant expiration, String sourceId, Double damage) {
        this.setId(UUID.randomUUID().toString());
        this.setDerivedEffects(new HashMap<>(Map.of(DamageTypes.PHYSICAL.getType(), damage)));
        this.setStatusEffects(new HashSet<>());
        this.setExpiration(expiration);
        this.setCanStack(true);
        this.setOrigin(sourceId);
        this.setCategory(StatusTypes.BLEEDING.getType());
    }

    @Override
    public boolean requiresDamageApply() {
        return true;
    }

    @JsonIgnore
    private BiConsumer<StatsService, Stats> applyBleed() {
        return (statsService, stats) -> {
            try {
                Map<DamageTypes, Double> damageMap = new HashMap<>();
                DamageTypes dmgType = DamageTypes.PHYSICAL;

                damageMap.put(dmgType, this.getDerivedEffects().get(dmgType.getType()));
                statsService.takeDamage(stats, damageMap, this.getOrigin());
            } catch (Exception e) {
                log.error("Error applying bleed effect, check the value maps");
                throw e;
            }
        };
    }

    @Override
    public Single<Boolean> apply(
            String actorId, StatsService statsService, StatusService statusService) {
        return baseApply(actorId, statsService, statusService, applyBleed());
    }
}
