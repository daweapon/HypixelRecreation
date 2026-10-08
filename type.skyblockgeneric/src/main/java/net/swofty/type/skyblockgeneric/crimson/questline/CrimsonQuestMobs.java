package net.swofty.type.skyblockgeneric.crimson.questline;

import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;

import java.util.Locale;
import java.util.Set;

/**
 * The mobs Crimson Isle quests count, by base id. A mob matches on its Bestiary id, class name or
 * display name, with any trailing level suffix dropped ({@code BLAZE_70} and "Blaze" are both
 * {@code BLAZE}), so a quest keeps working whatever exact id the mob classes use.
 */
public final class CrimsonQuestMobs {
    /** Elle's Blazes, plus any other Blaze so a player is never stuck if the intro Blazes were killed by someone else. */
    public static final Set<String> BLAZES = Set.of("INTRO_BLAZE", "BLAZE", "MUTATED_BLAZE", "BEZAL", "SMOLDERING_BLAZE",
            "MILLENNIA_AGED_BLAZE");
    public static final Set<String> FLAMING_SPIDERS = Set.of("FLAMING_SPIDER", "CORRUPTED_FLAMING_SPIDER");
    public static final Set<String> MAGMA_CUBE_RIDERS = Set.of("MAGMA_CUBE_RIDER");
    public static final Set<String> MAGE_OUTLAW = Set.of("MAGE_OUTLAW");
    public static final Set<String> MAGMA_BOSS = Set.of("MAGMA_BOSS");
    public static final Set<String> CHICKZILLA = Set.of("CHICKZILLA");

    private CrimsonQuestMobs() {
    }

    public static boolean matches(SkyBlockMob mob, Set<String> ids) {
        if (mob instanceof BestiaryMob bestiary && ids.contains(normalise(bestiary.getMobID()))) return true;
        String className = mob.getClass().getSimpleName().replaceFirst("^Mob", "");
        if (ids.contains(normalise(className.replaceAll("(?<=[a-z])(?=[A-Z])", "_")))) return true;
        String name = mob.getDisplayName();
        return name != null && ids.contains(normalise(name));
    }

    public static boolean matches(String id, Set<String> ids) {
        return id != null && ids.contains(normalise(id));
    }

    public static String normalise(String raw) {
        String plain = raw.replaceAll("<[^>]*>|§.", "").replaceAll("[()'’]", "").trim();
        return plain.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "").replaceAll("_\\d+$", "");
    }
}
