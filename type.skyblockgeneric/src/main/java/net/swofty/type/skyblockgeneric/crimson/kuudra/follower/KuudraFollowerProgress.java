package net.swofty.type.skyblockgeneric.crimson.kuudra.follower;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Per-profile Kuudra Follower questline state, kept in {@link DatapointCrimsonIsle}'s numbers. */
public final class KuudraFollowerProgress {
    private static final String STEP = "kuudra_follower:";
    private static final String BLESSED_UNTIL = "kuudra_follower:blessed_until";
    public static final long BLESSING_MILLIS = 30 * 60 * 1000L;

    private static final List<FollowerStep> DECAY_STEPS = List.of(FollowerStep.DECAY_ARCHEOLOGIST,
            FollowerStep.DECAY_ARCH, FollowerStep.DECAY_BELIEVER, FollowerStep.DECAY_LOREMASTER);

    private KuudraFollowerProgress() {
    }

    public static boolean has(SkyBlockPlayer player, FollowerStep step) {
        return DatapointCrimsonIsle.of(player).getNumber(STEP + step.name()) > 0;
    }

    public static void complete(SkyBlockPlayer player, FollowerStep step) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(STEP + step.name(), 1L));
    }

    /** A 1,000,000+ coin offering blesses the player for Kuudra's Hollow for 30 minutes. */
    public static void bless(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data ->
                data.getNumbers().put(BLESSED_UNTIL, System.currentTimeMillis() + BLESSING_MILLIS));
    }

    public static boolean isBlessed(SkyBlockPlayer player) {
        return DatapointCrimsonIsle.of(player).getNumber(BLESSED_UNTIL) > System.currentTimeMillis();
    }

    public static @Nullable TeethPlaqueStage plaqueStage(SkyBlockPlayer player) {
        if (!has(player, FollowerStep.PLAQUE_RECEIVED)) return null;
        if (has(player, FollowerStep.RELIC_UPGRADED)) return TeethPlaqueStage.FINAL;
        int decays = (int) DECAY_STEPS.stream().filter(step -> has(player, step)).count();
        return TeethPlaqueStage.values()[decays];
    }

    /**
     * Lowers the plaque one rarity, once per helper. Returns false when the helper already decayed
     * it or there is nothing left to decay.
     */
    public static boolean decay(SkyBlockPlayer player, FollowerStep helper) {
        if (!DECAY_STEPS.contains(helper)) throw new IllegalArgumentException(helper + " is not a decay step");
        TeethPlaqueStage stage = plaqueStage(player);
        if (stage == null || stage == TeethPlaqueStage.DECAYING || stage == TeethPlaqueStage.FINAL) return false;
        if (has(player, helper)) return false;
        complete(player, helper);
        return true;
    }

    /** /maxprofile: everything the player learns along the way; earned items and claims stay unclaimed. */
    public static void maxOut(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data -> {
            for (FollowerStep step : FollowerStep.values()) {
                if (step.isKnowledge()) data.getNumbers().put(STEP + step.name(), 1L);
            }
        });
    }
}
