package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.attribute.Attribute;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

final class PullSymbolsSpecial implements CreatureSpecial {
    private static final double SLOWDOWN_PER_SYMBOL = 0.75;

    private final Map<HuntingCreatureMob, int[]> symbols = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<HuntingCreatureMob, Double> baseSpeed = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public boolean onNetHit(SkyBlockPlayer player, HuntingCreatureMob mob, CreatureSpec.NetTier tier, double distance) {
        double pull = Math.max(tier.pull(), player.getStatistics().allStatistics().getOverall(ItemStatistic.PULL));
        int[] count = symbols.computeIfAbsent(mob, key -> new int[1]);
        count[0] = PullSymbols.total(count[0], pull);
        if (count[0] >= PullSymbols.GOAL) {
            SpecialMessages.send(player, mob.spec(), "exhausted");
            symbols.remove(mob);
            mob.setHeader("");
            mob.capture(player);
            return true;
        }
        SpecialMessages.send(player, mob.spec(), "symbol");
        mob.setHeader("<9>" + "⁂".repeat(count[0]));
        var speed = mob.getAttribute(Attribute.MOVEMENT_SPEED);
        double base = baseSpeed.computeIfAbsent(mob, key -> speed.getBaseValue());
        speed.setBaseValue(base * Math.pow(SLOWDOWN_PER_SYMBOL, count[0]));
        return true;
    }
}
