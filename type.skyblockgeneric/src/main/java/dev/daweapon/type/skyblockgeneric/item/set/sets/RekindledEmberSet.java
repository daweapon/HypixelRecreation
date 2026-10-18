package dev.daweapon.type.skyblockgeneric.item.set.sets;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.set.impl.TieredArmorSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.BurningDamage;

import java.util.List;

public class RekindledEmberSet implements TieredArmorSet {
    private static final String[] BURNING_BONUS = {"200%", "400%", "600%"};
    private static final String[] PER_SECOND = {"1.2%", "2.5%", "5%"};
    private static final String[] CAP = {"50%", "100%", "200%"};
    private static final double[] BURNING_FRACTION = {2, 4, 6};
    private static final double[] PER_SECOND_FRACTION = {0.012, 0.025, 0.05};
    private static final double[] CAP_FRACTION = {0.5, 1, 2};
    private static final long IGNITED_MELEE_BURN_MILLIS = 3000;

    public static double multiplier(int tier, double secondsOnFire) {
        int index = Math.max(0, Math.min(tier, BURNING_FRACTION.length - 1));
        double perSecond = Math.min(CAP_FRACTION[index], PER_SECOND_FRACTION[index] * Math.max(0, secondsOnFire));
        return 1 + BURNING_FRACTION[index] + perSecond;
    }

    public static double burningMultiplier(SkyBlockPlayer player) {
        RekindledEmberSet set = new RekindledEmberSet();
        int worn = set.getWornPieceCount(player);
        if (worn < set.minimumPieces()) return 1;
        return multiplier(TieredArmorSet.tierIndex(worn, set.minimumPieces()), BurningDamage.secondsOnFire(player));
    }

    public static void onMobMelee(SkyBlockMob mob, SkyBlockPlayer player) {
        if (!BurningDamage.isIgnited(mob)) return;
        RekindledEmberSet set = new RekindledEmberSet();
        if (set.getWornPieceCount(player) >= set.minimumPieces())
            BurningDamage.setPlayerOnFire(player, IGNITED_MELEE_BURN_MILLIS);
    }

    @Override
    public String getName() {
        return "Rekindle";
    }

    @Override
    public int minimumPieces() {
        return 2;
    }

    @Override
    public List<String> getDescription(int wornPieces) {
        int tier = TieredArmorSet.tierIndex(wornPieces, minimumPieces());
        return List.of(
                "<7>Increase all of your outgoing",
                "<7>burning damage by <c>" + BURNING_BONUS[tier] + "<7>. For each",
                "<7>second you are on fire additionally",
                "<7>increase all burning damage by <c>" + PER_SECOND[tier],
                "<7>up to <c>" + CAP[tier] + "<7>. When wearing 2 or more",
                "<7>armor pieces of this set, melee",
                "<7>attacks from ignited mobs will set you",
                "<7>on fire!");
    }
}
