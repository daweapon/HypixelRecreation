package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.random.RandomGenerator;

public final class SuspiciousScrap {
    public static final double BASE_ONE_IN = 200;
    public static final String FOUND_MESSAGE = "<6><l>EXCAVATOR! <r><f>You found a <9>Suspicious Scrap<f>!";

    private SuspiciousScrap() {
    }

    public static double chancePercent(int found, double... bonusPercents) {
        double chance = 100 / (BASE_ONE_IN * Math.pow(2, Math.max(0, found)));
        for (double bonus : bonusPercents) chance *= 1 + bonus / 100;
        return Math.min(100, chance);
    }

    public static boolean roll(@Nullable UUID player, int found, RandomGenerator random, double... bonusPercents) {
        return LootRoll.roll(player, ItemType.SUSPICIOUS_SCRAP, chancePercent(found, bonusPercents), random);
    }

    public static void award(SkyBlockPlayer player) {
        player.addAndUpdateItem(new dev.daweapon.type.skyblockgeneric.item.SkyBlockItem(ItemType.SUSPICIOUS_SCRAP, 1));
        player.sendMessage(FOUND_MESSAGE);
    }
}
