package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.Nullable;

public enum AncientPigEffect {
    FAIRY_SOUL_INFUSION("Fairy Soul Infusion", "<e>Buff stats by 50% for <b>60s <e>every time a fairy soul is clicked!", null),
    THUNDER("Thunder", "<e>A storm has begun bringing along some rare empowered creatures!", null),
    DOUBLE_MOB_DROPS("Double Mob Drops", "<e>All drops from mobs are doubled!", null),
    DOUBLE_PET_STATS("Double Pet Stats", "<e>All stats received from pets are doubled!", null),
    INCREASED_RUNIC_CHANCE("Increased Runic Chance", "<e>Increased the chance for runic monsters to spawn by <b>5x<e>!", null),
    MINING_EXPERIENCE("Mining Experience", "<e>Increased Mining experience earned by <b>+25%<e>!", SkillCategories.MINING),
    FORAGING_EXPERIENCE("Foraging Experience", "<e>Increased Foraging experience earned by <b>+25%<e>!", SkillCategories.FORAGING),
    FARMING_EXPERIENCE("Farming Experience", "<e>Increased Farming experience earned by <b>+25%<e>!", SkillCategories.FARMING),
    FISHING_EXPERIENCE("Fishing Experience", "<e>Increased Fishing experience earned by <b>+25%<e>!", SkillCategories.FISHING),
    COMBAT_EXPERIENCE("Combat Experience", "<e>Increased Combat experience earned by <b>+25%<e>!", SkillCategories.COMBAT);

    public static final double SKILL_XP_BONUS_PERCENT = 25;
    public static final double RUNIC_CHANCE_FACTOR = 5;
    public static final double INFUSION_BONUS_PERCENT = 50;
    public static final long INFUSION_MILLIS = 60_000;

    private final String displayName;
    private final String description;
    private final @Nullable SkillCategories skill;

    AncientPigEffect(String displayName, String description, @Nullable SkillCategories skill) {
        this.displayName = displayName;
        this.description = description;
        this.skill = skill;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public @Nullable SkillCategories skill() {
        return skill;
    }

    public static @Nullable AncientPigEffect parse(String raw) {
        for (AncientPigEffect effect : values()) {
            if (effect.name().equalsIgnoreCase(raw)) return effect;
        }
        return null;
    }
}
