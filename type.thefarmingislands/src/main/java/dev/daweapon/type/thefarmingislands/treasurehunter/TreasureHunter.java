package dev.daweapon.type.thefarmingislands.treasurehunter;

import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHunting;
import dev.daweapon.type.skyblockgeneric.hiker.HikerQuest;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class TreasureHunter {
    public static final long COOLDOWN_MILLIS = 3 * HikerQuest.SKYBLOCK_DAY_MILLIS;
    public static final double DIG_RADIUS = 2.5;
    public static final double DIG_HEIGHT = 3;
    private static final String NEXT_AT = "treasureHunter.nextAt";
    private static final String HINT = "treasureHunter.hint";
    private static final String TIER = "treasureHunter.tier";

    private TreasureHunter() {
    }

    private static DatapointHunting.HuntingData data(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HUNTING, DatapointHunting.class).getValue();
    }

    private static void save(SkyBlockPlayer player) {
        var datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HUNTING, DatapointHunting.class);
        datapoint.setValue(datapoint.getValue());
    }

    public static long millisOnCooldown(SkyBlockPlayer player, long now) {
        return Math.max(0, data(player).counter(NEXT_AT) - now);
    }

    public static TreasureHunterConfig.Hint activeHint(SkyBlockPlayer player) {
        long index = data(player).counter(HINT);
        List<TreasureHunterConfig.Hint> hints = TreasureHunterConfig.get().hints();
        return index <= 0 || index > hints.size() ? null : hints.get((int) index - 1);
    }

    public static int rollTierIndex(SkyBlockPlayer player) {
        List<TreasureHunterConfig.Tier> tiers = TreasureHunterConfig.get().tiers();
        TreasureHunterConfig.Tier tier = LootRoll.pick(player.getUuid(), tiers, _ -> null,
                TreasureHunterConfig.Tier::weight, ThreadLocalRandom.current());
        return tiers.indexOf(tier);
    }

    public static boolean buyHint(SkyBlockPlayer player, int tierIndex) {
        TreasureHunterConfig config = TreasureHunterConfig.get();
        TreasureHunterConfig.Tier tier = config.tiers().get(tierIndex);
        if (player.getCoins() < tier.fee()) return false;
        player.removeCoins(tier.fee());
        var data = data(player);
        data.setCounter(HINT, ThreadLocalRandom.current().nextInt(config.hints().size()) + 1);
        data.setCounter(TIER, tierIndex + 1);
        save(player);
        return true;
    }

    public static boolean isDigSpot(SkyBlockPlayer player, Point clicked) {
        TreasureHunterConfig.Hint hint = activeHint(player);
        if (hint == null) return false;
        Point target = hint.position();
        double dx = clicked.x() - target.x();
        double dz = clicked.z() - target.z();
        return dx * dx + dz * dz <= DIG_RADIUS * DIG_RADIUS && Math.abs(clicked.y() - target.y()) <= DIG_HEIGHT;
    }

    public static void dig(SkyBlockPlayer player) {
        var data = data(player);
        TreasureHunterConfig config = TreasureHunterConfig.get();
        int tierIndex = (int) Math.max(1, data.counter(TIER)) - 1;
        TreasureHunterConfig.Tier tier = config.tiers().get(tierIndex);
        TreasureHunterConfig.Reward reward = LootRoll.pick(player.getUuid(), tier.rewards(), TreasureHunterConfig.Reward::item,
                TreasureHunterConfig.Reward::weight, ThreadLocalRandom.current());
        data.setCounter(HINT, 0);
        data.setCounter(TIER, 0);
        data.setCounter(NEXT_AT, System.currentTimeMillis() + COOLDOWN_MILLIS);
        save(player);
        player.addAndUpdateItem(reward.item(), reward.amount());
        player.sendMessage("<6><l>TREASURE! <f>You dug up <a>{}x {}<f>!", reward.amount(), reward.item().getDisplayName());
        ChapterTriggers.fire(player, "treasurehunter.dug");
    }
}
