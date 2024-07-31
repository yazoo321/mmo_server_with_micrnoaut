package server.attribute.stats.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatsTypes {

    // Base stats
    STR("STR"),
    DEX("DEX"),
    INT("INT"),
    STA("STA"),

    AVAILABLE_PTS("AVAILABLE_PTS"),

    // Derived stats
    MAX_HP("MAX_HP"),
    MAX_MP("MAX_MP"),
    CURRENT_HP("CURRENT_HP"),
    CURRENT_MP("CURRENT_MP"),

    PHY_AMP("PHY_AMP"),
    MAG_AMP("MAG_AMP"),

    BASE_HP_REGEN("BASE_HP_REGEN"),
    HP_REGEN("HP_REGEN"),

    BASE_MP_REGEN("BASE_MP_REGEN"),
    MP_REGEN("MP_REGEN"),

    DEF("DEF"),
    MAG_DEF("MAG_DEF"),

    ATTACK_SPEED("ATTACK_SPEED"),
    CAST_SPEED("CAST_SPEED"),

    BASE_ATTACK_SPEED("BASE_ATTACK_SPEED"),
    ATTACK_DISTANCE("ATTACK_DISTANCE"),

    PHY_CRIT("PHY_CRIT"),
    MGC_CRIT("MGC_CRIT"),

    WEAPON_DAMAGE("WEAPON_DAMAGE"),
    MAGIC_DAMAGE("MAGIC_DAMAGE"),

    PHY_REDUCTION("PHY_REDUCTION"),
    MGC_REDUCTION("MGC_REDUCTION"),

    // Level stats
    XP("XP"),
    XP_TO_NEXT_LEVEL("XP_TO_NEXT_LEVEL"),
    LEVEL("LEVEL"),
    CAN_LEVEL("CAN_LEVEL"),

    // Mob specific stats
    MAIN_HAND_ATTACK_SPEED("MAIN_HAND_ATTACK_SPEED"),
    OFF_HAND_ATTACK_SPEED("OFF_HAND_ATTACK_SPEED");

    public final String type;
}
