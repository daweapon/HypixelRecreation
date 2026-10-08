package net.swofty.type.skyblockgeneric.crimson.questline;

import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import net.swofty.commons.skyblock.item.ItemType;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * State for the faction reputation side quests (Rollim/Kaus duels, Pablo, Avorius, Mollim, the
 * Alchemage/Baar gold research, Pomtair, Sirih, Suus and Igrupan's chickens), kept in
 * {@link DatapointCrimsonIsle}'s numbers map under {@code side:*}. Timestamps are epoch millis.
 */
public final class CrimsonSideQuests {
    public static final int DUEL_DAYS = 7;
    public static final int ALCHEMIST_STEPS = 8;
    public static final String PABLO_LAST = "pablo:last";
    public static final String PABLO_EVER = "pablo:ever";
    public static final String AVORIUS_LAST = "avorius:last";
    public static final String AVORIUS_EVER = "avorius:ever";
    public static final String MOLLIM_DONE = "mollim:done";
    public static final String MOLLIM_GRAND = "mollim:grand";
    public static final String ALCHEMIST_STAGE = "alchemist:stage";
    public static final String ALCHEMIST_STARTED = "alchemist:started";
    public static final String POMTAIR_LAST = "pomtair:last";
    public static final String POMTAIR_EVER = "pomtair:ever";
    public static final String SIRIH_CURED = "sirih:cured";
    public static final String SIRIH_REWARDED = "sirih:rewarded";
    public static final String SUUS_LAST = "suus:last";
    public static final String SUUS_TOY = "suus:toy";
    public static final String SUUS_EVER = "suus:ever";
    public static final String CHICKZILLA_DONE = "chickzilla:done";
    public static final String CHICKEN = "igrupan:chicken:";
    public static final String MET = "met:";
    public static final String SUUS_MOB = "suus:mob";
    public static final List<Set<String>> SUUS_MOBS = List.of(CrimsonQuestMobs.MAGMA_CUBE_RIDERS,
            CrimsonQuestMobs.FLAMING_SPIDERS, Set.of("MAGMA_CUBE"));
    public static final List<String> SUUS_MOB_NAMES = List.of("Magma Cube Rider", "Flaming Spider", "Magma Cube");

    private static final String PREFIX = "side:";

    private CrimsonSideQuests() {
    }

    public static long get(SkyBlockPlayer player, String key) {
        return DatapointCrimsonIsle.of(player).getNumber(PREFIX + key);
    }

    public static boolean is(SkyBlockPlayer player, String key) {
        return get(player, key) > 0;
    }

    public static void set(SkyBlockPlayer player, String key, long value) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(PREFIX + key, value));
    }

    public static void flag(SkyBlockPlayer player, String key) {
        set(player, key, 1);
    }

    public static void stamp(SkyBlockPlayer player, String key) {
        set(player, key, System.currentTimeMillis());
    }

    /** True when {@code key} was never stamped or was stamped at least {@code cooldown} ago. */
    public static boolean isReady(SkyBlockPlayer player, String key, Duration cooldown) {
        long last = get(player, key);
        return last == 0 || System.currentTimeMillis() - last >= cooldown.toMillis();
    }

    /** First-time dialogue marks ("met:<npc>") so an NPC's introduction plays once per profile. */
    public static boolean firstMeeting(SkyBlockPlayer player, String npc) {
        if (is(player, MET + npc)) return false;
        flag(player, MET + npc);
        return true;
    }

    public static String duelDaysKey(CrimsonFaction faction) {
        return "duel:" + faction.name() + ":days";
    }

    public static String duelLastKey(CrimsonFaction faction) {
        return "duel:" + faction.name() + ":last";
    }

    public static int duelDays(SkyBlockPlayer player, CrimsonFaction faction) {
        return (int) get(player, duelDaysKey(faction));
    }

    /**
     * The flower Pablo wants for the current two-day request: Enchanted Dandelion 45%, Enchanted
     * Poppy 45%, End Stone Rose 10%. It is a request rather than a drop, so it is derived from the
     * profile and the last delivery instead of rolled, and stays the same until it is delivered.
     */
    public static ItemType pabloFlower(SkyBlockPlayer player) {
        long seed = player.getUuid().getMostSignificantBits() ^ get(player, PABLO_LAST);
        int roll = (int) Math.floorMod(seed * 0x9E3779B97F4A7C15L >>> 17, 100L);
        if (roll < 45) return ItemType.ENCHANTED_DANDELION;
        if (roll < 90) return ItemType.ENCHANTED_POPPY;
        return ItemType.ENDSTONE_ROSE;
    }

    /** Sirih turns back into a pig a day after the player last fed them Sulphur. */
    public static boolean isSirihPig(SkyBlockPlayer player) {
        return isReady(player, SIRIH_CURED, Duration.ofHours(24));
    }

    /** The mob that ate Turd's toy this time. Calling Suus can name a different Burning Desert mob. */
    public static Set<String> suusMob(SkyBlockPlayer player) {
        return SUUS_MOBS.get((int) Math.floorMod(get(player, SUUS_MOB), SUUS_MOBS.size()));
    }

    public static String suusMobName(SkyBlockPlayer player) {
        return SUUS_MOB_NAMES.get((int) Math.floorMod(get(player, SUUS_MOB), SUUS_MOB_NAMES.size()));
    }

    public static final Duration SUUS_COOLDOWN = Duration.ofDays(2);
    public static final double TURD_TOY_CHANCE = 10;

    /** Turd's toy (10% from the named mob) and Chickzilla (+100 Mage Reputation, once). */
    public static void onMobKilled(SkyBlockPlayer player, SkyBlockMob mob) {
        if (CrimsonReputationService.isMember(player, CrimsonFaction.BARBARIAN) && is(player, MET + "suus")
                && !is(player, SUUS_TOY) && isReady(player, SUUS_LAST, SUUS_COOLDOWN)
                && CrimsonQuestMobs.matches(mob, suusMob(player))
                && LootRoll.roll(player.getUuid(), null, TURD_TOY_CHANCE)) {
            flag(player, SUUS_TOY);
            player.sendMessage("<a>You found <d>Turd<a>'s toy! Bring it back to <d>Suus<a> in Dragontail.");
        }
        if (CrimsonQuestMobs.matches(mob, CrimsonQuestMobs.CHICKZILLA) && !is(player, CHICKZILLA_DONE)) {
            flag(player, CHICKZILLA_DONE);
            CrimsonReputationService.add(player, CrimsonFaction.MAGE, 100, "Chickzilla");
        }
    }

    /** /maxprofile: every once-per-profile side quest finished. Repeatable dailies are left alone. */
    public static void maxOut(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data -> {
            for (CrimsonFaction faction : CrimsonFaction.values()) {
                data.getNumbers().put(PREFIX + duelDaysKey(faction), (long) DUEL_DAYS);
            }
            data.getNumbers().put(PREFIX + MOLLIM_DONE, 1L);
            data.getNumbers().put(PREFIX + ALCHEMIST_STARTED, 1L);
            data.getNumbers().put(PREFIX + ALCHEMIST_STAGE, (long) ALCHEMIST_STEPS);
            data.getNumbers().put(PREFIX + CHICKZILLA_DONE, 1L);
        });
    }
}
