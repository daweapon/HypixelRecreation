package dev.daweapon.type.skyblockgeneric.crimson.questline;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoGrade;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTest;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Locale;

/**
 * Progress through the Crimson Isle Main Questline: the shared arrival part, then the Mage and
 * Barbarian lines. Stages live in {@link DatapointCrimsonIsle}'s questline map; per-stage counters
 * and the "talked to this villager" marks live in its numbers map under {@code questline:*}.
 */
public final class CrimsonQuestlines {
    public static final String UNIVERSAL = "UNIVERSAL";
    public static final String MAGE = "MAGE";
    public static final String BARBARIAN = "BARBARIAN";
    public static final int VILLAGERS_PER_FACTION = 4;
    public static final int INTRO_BLAZES = 3;
    public static final List<DojoTest> UGO_TRAINING = List.of(DojoTest.FORCE, DojoTest.STAMINA, DojoTest.MASTERY);

    private static final String COUNTER = "questline:";
    private static final String VILLAGER = "questline:villager:";
    private static final String SOURCE = "Faction Questline";

    private CrimsonQuestlines() {
    }

    public static UniversalStep universal(SkyBlockPlayer player) {
        return step(player, UNIVERSAL, UniversalStep.values());
    }

    public static MageStep mage(SkyBlockPlayer player) {
        return step(player, MAGE, MageStep.values());
    }

    public static BarbarianStep barbarian(SkyBlockPlayer player) {
        return step(player, BARBARIAN, BarbarianStep.values());
    }

    private static <T extends QuestlineStep> T step(SkyBlockPlayer player, String key, T[] values) {
        int stage = DatapointCrimsonIsle.of(player).getQuestlineStage(key);
        return values[Math.max(0, Math.min(stage, values.length - 1))];
    }

    public static void set(SkyBlockPlayer player, UniversalStep step) {
        store(player, UNIVERSAL, step);
    }

    public static void set(SkyBlockPlayer player, MageStep step) {
        store(player, MAGE, step);
    }

    public static void set(SkyBlockPlayer player, BarbarianStep step) {
        store(player, BARBARIAN, step);
    }

    /** Moves a Mage stage on only if the player is still on {@code from}, so a repeated click can't skip ahead. */
    public static boolean advance(SkyBlockPlayer player, MageStep from, MageStep to, int mageReputation) {
        if (mage(player) != from) return false;
        set(player, to);
        if (mageReputation > 0) CrimsonReputationService.add(player, CrimsonFaction.MAGE, mageReputation, SOURCE);
        return true;
    }

    public static boolean advance(SkyBlockPlayer player, BarbarianStep from, BarbarianStep to, int barbarianReputation) {
        if (barbarian(player) != from) return false;
        set(player, to);
        if (barbarianReputation > 0) CrimsonReputationService.add(player, CrimsonFaction.BARBARIAN, barbarianReputation, SOURCE);
        return true;
    }

    public static boolean advance(SkyBlockPlayer player, UniversalStep from, UniversalStep to) {
        if (universal(player) != from) return false;
        set(player, to);
        return true;
    }

    private static void store(SkyBlockPlayer player, String key, QuestlineStep step) {
        DatapointCrimsonIsle.edit(player, data -> data.getQuestline().put(key, step.ordinal()));
        if (!step.name().equals("COMPLETE") && !step.name().equals("NOT_STARTED")) {
            player.sendMessage("<6>➜ <e>" + step.objective());
        }
    }

    public static boolean isStarted(SkyBlockPlayer player, CrimsonFaction faction) {
        return faction == CrimsonFaction.MAGE ? mage(player) != MageStep.NOT_STARTED : barbarian(player) != BarbarianStep.NOT_STARTED;
    }

    public static boolean isComplete(SkyBlockPlayer player, CrimsonFaction faction) {
        return faction == CrimsonFaction.MAGE ? mage(player) == MageStep.COMPLETE : barbarian(player) == BarbarianStep.COMPLETE;
    }

    public static boolean isOnForgottenSkullMeeting(SkyBlockPlayer player) {
        return mage(player) == MageStep.MEET_AT_FORGOTTEN_SKULL || barbarian(player) == BarbarianStep.MEET_AT_FORGOTTEN_SKULL;
    }

    /** Crag's "Complete main quest" check: a faction line has reached its first-Kuudra step. */
    public static boolean isReadyForKuudra(SkyBlockPlayer player) {
        return mage(player).ordinal() >= MageStep.DEFEAT_KUUDRA.ordinal()
                || barbarian(player).ordinal() >= BarbarianStep.DEFEAT_KUUDRA.ordinal();
    }

    public static boolean isAwaitingKuudra(SkyBlockPlayer player) {
        return mage(player) == MageStep.DEFEAT_KUUDRA || barbarian(player) == BarbarianStep.DEFEAT_KUUDRA;
    }

    public static long counter(SkyBlockPlayer player, String name) {
        return DatapointCrimsonIsle.of(player).getNumber(COUNTER + name);
    }

    public static void setCounter(SkyBlockPlayer player, String name, long value) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(COUNTER + name, value));
    }

    public static long increment(SkyBlockPlayer player, String name) {
        long value = counter(player, name) + 1;
        setCounter(player, name, value);
        return value;
    }

    public static boolean hasSpokenTo(SkyBlockPlayer player, CrimsonFaction faction, String villager) {
        return DatapointCrimsonIsle.of(player).getNumber(villagerKey(faction, villager)) > 0;
    }

    /** Records a faction villager as talked to and returns how many of that faction the player has met (1-based). */
    public static int markSpokenTo(SkyBlockPlayer player, CrimsonFaction faction, String villager) {
        if (!hasSpokenTo(player, faction, villager)) {
            DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(villagerKey(faction, villager), 1L));
        }
        return villagersSpokenTo(player, faction);
    }

    public static int villagersSpokenTo(SkyBlockPlayer player, CrimsonFaction faction) {
        String prefix = VILLAGER + faction.name() + ":";
        return (int) DatapointCrimsonIsle.of(player).getNumbers().keySet().stream().filter(key -> key.startsWith(prefix)).count();
    }

    private static String villagerKey(CrimsonFaction faction, String villager) {
        return VILLAGER + faction.name() + ":" + villager.toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    public static boolean hasMetVillagers(SkyBlockPlayer player) {
        for (CrimsonFaction faction : CrimsonFaction.values()) {
            if (villagersSpokenTo(player, faction) < VILLAGERS_PER_FACTION) return false;
        }
        return true;
    }

    /** Counts the kills the questlines care about. Called for every Crimson Isle mob kill. */
    public static void onMobKilled(SkyBlockPlayer player, SkyBlockMob mob) {
        if (universal(player) == UniversalStep.KILL_BLAZES && CrimsonQuestMobs.matches(mob, CrimsonQuestMobs.BLAZES)) {
            long killed = increment(player, "intro_blazes");
            if (killed >= INTRO_BLAZES) {
                set(player, UniversalStep.HELP_ELLE);
            } else {
                player.sendMessage("<e>Blazes killed for Elle: <a>" + killed + "<7>/<a>" + INTRO_BLAZES);
            }
        }
        if (barbarian(player) == BarbarianStep.KILL_CORRUPTED_MONSTER && CrimsonQuestMobs.matches(mob, CrimsonQuestMobs.FLAMING_SPIDERS)) {
            player.addAndUpdateItem(ItemType.SULPHUR, 1);
            player.sendMessage("<a>The Flaming Spider dropped <f>Sulphur<a>!");
            set(player, BarbarianStep.FIND_THE_NOISE);
        }
    }

    /** Ugo's training needs a Mage Outlaw and then the Magma Boss (fed from {@code CrimsonMinibossKilledEvent}). */
    public static void onMinibossKilled(SkyBlockPlayer player, String minibossId) {
        if (CrimsonQuestMobs.matches(minibossId, CrimsonQuestMobs.MAGE_OUTLAW)) {
            advance(player, BarbarianStep.KILL_MAGE_OUTLAW, BarbarianStep.REPORT_MAGE_OUTLAW, 0);
        }
        if (CrimsonQuestMobs.matches(minibossId, CrimsonQuestMobs.MAGMA_BOSS)) {
            advance(player, BarbarianStep.KILL_MAGMA_BOSS, BarbarianStep.REPORT_MAGMA_BOSS, 0);
        }
    }

    /** Ugo's "Barbarian Training": Force, Stamina and Mastery, each passed with at least a D. */
    public static void onDojoTestCompleted(SkyBlockPlayer player, DojoTest test, DojoGrade grade) {
        if (barbarian(player) != BarbarianStep.DOJO_TRAINING || !UGO_TRAINING.contains(test) || !grade.isPassing()) return;
        setCounter(player, "ugo_training:" + test.name(), 1);
        boolean allPassed = UGO_TRAINING.stream().allMatch(each -> counter(player, "ugo_training:" + each.name()) > 0);
        if (allPassed) set(player, BarbarianStep.REPORT_DOJO);
    }

    /**
     * Kuudra hook: the first Kuudra win (Basic tier, with Elle) finishes the "Recover Sulphur" step
     * of whichever faction line is waiting on it. Until the fight exists this is never called.
     */
    public static void onFirstKuudraDefeated(SkyBlockPlayer player) {
        advance(player, MageStep.DEFEAT_KUUDRA, MageStep.TALK_TO_DEAN_FINAL, 200);
        advance(player, BarbarianStep.DEFEAT_KUUDRA, BarbarianStep.TALK_TO_CHIEF_FINAL, 200);
    }

    /** /maxprofile: the arrival part and both faction lines finished. Reputation is maxed separately. */
    public static void maxOut(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data -> {
            data.getQuestline().put(UNIVERSAL, UniversalStep.COMPLETE.ordinal());
            data.getQuestline().put(MAGE, MageStep.COMPLETE.ordinal());
            data.getQuestline().put(BARBARIAN, BarbarianStep.COMPLETE.ordinal());
        });
    }
}
