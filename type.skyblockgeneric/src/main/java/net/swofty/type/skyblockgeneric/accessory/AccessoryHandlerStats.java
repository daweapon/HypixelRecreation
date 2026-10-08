package net.swofty.type.skyblockgeneric.accessory;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.accessory.effect.AccessoryEffect;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stats that a {@code HANDLER} accessory effect grants only while its Java condition holds. The
 * effect's {@code statistics} and {@code percent} come from the accessory YAML.
 */
public final class AccessoryHandlerStats {
    public static final String WHILE_BURNING = "WHILE_BURNING";
    public static final String HASTE = "HASTE";
    public static final String GRAVITY = "GRAVITY_TALISMAN";
    public static final String MAGIC_8_BALL = "MAGIC_8_BALL";
    public static final String TOTAL_INTELLIGENCE = "TOTAL_INTELLIGENCE";

    private static final double GRAVITY_MAX = 10;
    private static final double GRAVITY_BLOCKS_PER_POINT = 10;

    private static final List<ItemStatistics> MAGIC_8_BALL_BONUSES = List.of(
            bonus(ItemStatistic.FARMING_FORTUNE, ItemStatistic.FARMING_WISDOM),
            bonus(ItemStatistic.FORAGING_FORTUNE, ItemStatistic.FORAGING_WISDOM),
            bonus(ItemStatistic.MINING_FORTUNE, ItemStatistic.MINING_WISDOM),
            bonus(ItemStatistic.FISHING_SPEED, ItemStatistic.FISHING_WISDOM),
            bonus(ItemStatistic.STRENGTH, ItemStatistic.COMBAT_WISDOM));

    private interface StatHandler {
        ItemStatistics statistics(SkyBlockPlayer player, AccessoryEffect effect);
    }

    private static final Map<String, StatHandler> HANDLERS = Map.of(
            WHILE_BURNING, (player, effect) -> player.getFireTicks() > 0 ? effect.statistics() : ItemStatistics.empty(),
            HASTE, (player, effect) -> effect.statistics(),
            GRAVITY, (player, effect) -> gravity(player),
            MAGIC_8_BALL, (player, effect) -> MAGIC_8_BALL_BONUSES.get(magic8BallIndex(player)),
            TOTAL_INTELLIGENCE, (player, effect) -> ItemStatistics.builder()
                    .withMultiplicativePercentage(ItemStatistic.INTELLIGENCE, effect.percent()).build());

    private AccessoryHandlerStats() {
    }

    public static boolean isStatHandler(String handler) {
        return HANDLERS.containsKey(handler);
    }

    public static ItemStatistics of(SkyBlockPlayer player, AccessoryEffect effect) {
        StatHandler handler = effect.handler() == null ? null : HANDLERS.get(effect.handler());
        if (handler == null || !effect.appliesTo(player)) return ItemStatistics.empty();
        return handler.statistics(player, effect);
    }

    public static double gravityBonus(double distanceFromSpawn) {
        double points = GRAVITY_MAX - Math.floor(distanceFromSpawn / GRAVITY_BLOCKS_PER_POINT);
        return Math.max(1, Math.min(GRAVITY_MAX, points));
    }

    public static int magic8BallIndex(Object seed, int year, int season) {
        return Math.floorMod(Objects.hash(seed, year, season), MAGIC_8_BALL_BONUSES.size());
    }

    public static ItemStatistics magic8BallBonus(int index) {
        return MAGIC_8_BALL_BONUSES.get(index);
    }

    private static int magic8BallIndex(SkyBlockPlayer player) {
        return magic8BallIndex(player.getUuid(), SkyBlockCalendar.getYear(), (SkyBlockCalendar.getMonth() - 1) / 3);
    }

    private static ItemStatistics gravity(SkyBlockPlayer player) {
        Pos spawn = player.getRespawnPoint();
        if (spawn == null || player.getInstance() == null) return ItemStatistics.empty();
        double bonus = gravityBonus(player.getPosition().distance(spawn));
        return ItemStatistics.builder()
                .withBase(ItemStatistic.STRENGTH, bonus)
                .withBase(ItemStatistic.DEFENSE, bonus).build();
    }

    private static ItemStatistics bonus(ItemStatistic main, ItemStatistic wisdom) {
        return ItemStatistics.builder().withBase(main, 25D).withBase(wisdom, 1D).build();
    }
}
