package net.swofty.type.skyblockgeneric.crimson.faction;

import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle.CrimsonIsleData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Faction membership and reputation. Since the November 2024 rework both factions' reputation is
 * kept independently and floored at zero; the first time a faction reaches each title it grants
 * that title's SkyBlock XP, which is never taken back when reputation later drops.
 */
public final class CrimsonReputationService {
    private static final String PEAK_KEY = "reputation:peak:";

    private CrimsonReputationService() {
    }

    public static @Nullable CrimsonFaction faction(SkyBlockPlayer player) {
        return DatapointCrimsonIsle.of(player).getFaction();
    }

    public static boolean isMember(SkyBlockPlayer player, CrimsonFaction faction) {
        return faction(player) == faction;
    }

    public static void setFaction(SkyBlockPlayer player, @Nullable CrimsonFaction faction) {
        DatapointCrimsonIsle.edit(player, data -> data.setFaction(faction));
    }

    public static int get(SkyBlockPlayer player, CrimsonFaction faction) {
        return DatapointCrimsonIsle.of(player).getReputation(faction);
    }

    public static ReputationTier tier(SkyBlockPlayer player, CrimsonFaction faction) {
        return ReputationTier.of(get(player, faction));
    }

    /** Kuudra tiers accept reputation with either faction, so gates there read the better of the two. */
    public static int highest(SkyBlockPlayer player) {
        CrimsonIsleData data = DatapointCrimsonIsle.of(player);
        return Arrays.stream(CrimsonFaction.values()).mapToInt(data::getReputation).max().orElse(0);
    }

    public static ReputationTier highestTier(SkyBlockPlayer player) {
        return ReputationTier.of(highest(player));
    }

    /** The share (0 to 0.20) a faction's Emissary takes off its prices at the player's current title. */
    public static double emissaryDiscount(SkyBlockPlayer player, CrimsonFaction faction) {
        return tier(player, faction).getEmissaryDiscount();
    }

    /**
     * Changes a faction's reputation, never below zero, tells the player, and grants any newly
     * reached title's SkyBlock XP. Returns the new reputation.
     */
    public static int add(SkyBlockPlayer player, CrimsonFaction faction, int amount, @Nullable String source) {
        int before = get(player, faction);
        int after = Math.max(0, before + amount);
        if (after == before) return before;

        store(player, faction, after);
        int delta = after - before;
        String sourceSuffix = source == null || source.isBlank() ? "" : " <7>(" + source + ")";
        if (delta > 0) {
            player.sendMessage("<8>+<a>" + StringUtility.commaify(delta) + " " + faction.coloured() + " Reputation" + sourceSuffix);
        } else {
            player.sendMessage("<8>-<c>" + StringUtility.commaify(-delta) + " " + faction.coloured() + " Reputation" + sourceSuffix);
        }

        ReputationTier oldTier = ReputationTier.of(before);
        ReputationTier newTier = ReputationTier.of(after);
        if (newTier.ordinal() > oldTier.ordinal()) {
            player.sendMessage("<6><l>REPUTATION TITLE! <7>You are now " + newTier.coloured()
                    + " <7>with the " + faction.coloured() + " Faction<7>!");
        }
        return after;
    }

    /** Staff and /maxprofile path: sets reputation exactly, without chat, still granting title XP. */
    public static void set(SkyBlockPlayer player, CrimsonFaction faction, int amount) {
        store(player, faction, Math.max(0, amount));
    }

    /** /maxprofile: Hero with both factions, keeping a chosen faction or joining the Mages. */
    public static void maxOut(SkyBlockPlayer player) {
        if (faction(player) == null) setFaction(player, CrimsonFaction.MAGE);
        for (CrimsonFaction faction : CrimsonFaction.values()) {
            set(player, faction, Math.max(get(player, faction), ReputationTier.HERO.getRequiredReputation()));
        }
    }

    public static ReputationTier peakTier(SkyBlockPlayer player, CrimsonFaction faction) {
        int peak = (int) DatapointCrimsonIsle.of(player).getNumber(PEAK_KEY + faction.name());
        return ReputationTier.values()[Math.min(peak, ReputationTier.values().length - 1)];
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        for (CrimsonFaction faction : CrimsonFaction.values()) {
            player.getSkyBlockExperience().setSourceXP(faction.getXpSource(),
                    ReputationTier.cumulativeSkyBlockXP(peakTier(player, faction)));
        }
    }

    public static CrimsonFaction strongest(SkyBlockPlayer player) {
        CrimsonIsleData data = DatapointCrimsonIsle.of(player);
        return Arrays.stream(CrimsonFaction.values())
                .max(Comparator.comparingInt(data::getReputation))
                .orElse(CrimsonFaction.MAGE);
    }

    private static void store(SkyBlockPlayer player, CrimsonFaction faction, int amount) {
        ReputationTier reached = ReputationTier.of(amount);
        DatapointCrimsonIsle.edit(player, data -> {
            data.getReputation().put(faction, amount);
            String peakKey = PEAK_KEY + faction.name();
            if (reached.ordinal() > data.getNumber(peakKey)) data.getNumbers().put(peakKey, (long) reached.ordinal());
        });
        syncSkyBlockXP(player);
    }
}
