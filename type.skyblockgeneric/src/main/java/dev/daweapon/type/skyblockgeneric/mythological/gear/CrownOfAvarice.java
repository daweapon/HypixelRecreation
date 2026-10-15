package dev.daweapon.type.skyblockgeneric.mythological.gear;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Overindulgence: 5x Coins from Griffin Burrows and Mythological mobs, all of which the crown eats.
 * Each digit of the coins eaten grants +2.5 Magic Find and +0.015x damage against Mythological
 * mobs. Once it has eaten 1B it stops eating and pays 2x instead.
 */
public final class CrownOfAvarice {
    public static final long CAPACITY = 1_000_000_000L;
    private static final int HUNGRY_MULTIPLIER = 5;
    private static final int SATIATED_MULTIPLIER = 2;
    private static final double MAGIC_FIND_PER_DIGIT = 2.5;
    private static final double DAMAGE_PER_DIGIT = 0.015;
    private static final List<Milestone> MILESTONES = List.of(
            new Milestone(100_000L, "<a><l>HUNGRY! <f>Your <6>Crown of Avarice <f>seems to want more coins."),
            new Milestone(1_000_000L, "<e><l>STARVING! <f>You can feel your <6>Crown of Avarice<f>'s cravings getting stronger."),
            new Milestone(10_000_000L, "<6><l>FAMISHED! <f>Your <6>Crown of Avarice <f>starts growling at you for more coins. <7><i>What have you gotten yourself into...?"),
            new Milestone(100_000_000L, "<c><l>RAVENOUS! <f>Your <6>Crown of Avarice<f> is barking at you. If you don't keep feeding it coins, it might eat your head instead!"),
            new Milestone(CAPACITY, "<b><l>SATIATED! <f>Your <6>Crown of Avarice<f> is stuffed to the brim, and appears to be in a food coma."));

    private record Milestone(long coins, String message) {
    }

    private CrownOfAvarice() {
    }

    public static boolean isWorn(SkyBlockPlayer player) {
        if (player.getHelmet().isAir()) return false;
        return new SkyBlockItem(player.getHelmet()).getAttributeHandler().getPotentialType() == ItemType.CROWN_OF_AVARICE;
    }

    public static int digits(SkyBlockItem crown) {
        long consumed = crown.getAttributeHandler().getCoinsConsumed();
        return consumed <= 0 ? 0 : String.valueOf(Math.min(consumed, CAPACITY)).length();
    }

    public static double magicFind(SkyBlockItem crown) {
        return digits(crown) * MAGIC_FIND_PER_DIGIT;
    }

    public static double damageMultiplier(SkyBlockItem crown) {
        return 1 + digits(crown) * DAMAGE_PER_DIGIT;
    }

    /**
     * Feeds coins earned while the crown is worn. Returns how many coins the player keeps.
     */
    public static long feed(SkyBlockPlayer player, long coins) {
        SkyBlockItem crown = new SkyBlockItem(player.getHelmet());
        long consumed = crown.getAttributeHandler().getCoinsConsumed();
        if (consumed >= CAPACITY) return coins * SATIATED_MULTIPLIER;

        long eaten = Math.min(coins * HUNGRY_MULTIPLIER, CAPACITY - consumed);
        long after = consumed + eaten;
        player.updateItem(PlayerItemOrigin.HELMET, item -> item.getAttributeHandler().setCoinsConsumed(after));
        for (Milestone milestone : MILESTONES) {
            if (consumed < milestone.coins() && after >= milestone.coins()) player.sendMessage(milestone.message());
        }
        return 0;
    }
}
