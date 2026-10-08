package net.swofty.type.skyblockgeneric.slayer;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rolls a slayer boss's drops the way Hypixel does: the token is guaranteed, then one weighted pick
 * from the main pool, one from the extra pool, then each separate drop on its own. Rows locked by the
 * player's slayer level leave the pool, so the "nothing" weight inflates the odds of what remains.
 */
public final class SlayerDropTable {
    private static final double MAGIC_FIND_THRESHOLD_PERCENT = 5;

    private SlayerDropTable() {
    }

    public static void award(SkyBlockPlayer player, SkyBlockMob boss, SlayerDefinition definition, SlayerTier tier,
                             int slayerLevel, DatapointSlayer.SlayerProgress progress) {
        double magicFind = player.getStatistics().allStatistics(player, boss).getOverall(ItemStatistic.MAGIC_FIND);
        boolean meterActive = SlayerRngMeter.unlocked(progress);
        SlayerDrop selected = meterActive ? SlayerRngMeter.selected(definition, progress) : null;
        boolean guaranteed = selected != null && selected.rollable(tier, slayerLevel) && SlayerRngMeter.full(progress, selected);

        List<SlayerDrop> won = new ArrayList<>();
        for (SlayerDrop drop : definition.drops()) {
            if (drop.pool() == SlayerDrop.Pool.TOKEN && drop.rollable(tier, slayerLevel)) won.add(drop);
        }
        for (SlayerDrop.Pool pool : List.of(SlayerDrop.Pool.MAIN, SlayerDrop.Pool.EXTRA)) {
            if (guaranteed && selected.pool() == pool) {
                won.add(selected);
                continue;
            }
            pickFromPool(player, definition, tier, slayerLevel, pool, progress, selected, magicFind).ifPresent(won::add);
        }
        for (SlayerDrop drop : definition.drops()) {
            if (drop.pool() != SlayerDrop.Pool.SEPARATE || !drop.rollable(tier, slayerLevel)) continue;
            if (guaranteed && drop == selected) {
                won.add(drop);
                continue;
            }
            double chance = adjusted(drop.odds(tier).chancePercent() * multiplier(progress, drop, selected), magicFind);
            if (LootRoll.roll(player.getUuid(), drop.item(), chance)) won.add(drop);
        }

        for (SlayerDrop drop : won) {
            if (selected != null && drop == selected) {
                boolean wasFull = SlayerRngMeter.full(progress, drop);
                SlayerRngMeter.onObtained(progress, drop);
                if (wasFull) player.sendMessage(Text.of("<d><l>RNG METER!</l> <7>Your meter guaranteed {}<7>!", displayName(drop)));
            }
            SlayerDrop.TierOdds odds = drop.odds(tier);
            SkyBlockItem item = create(drop, odds.rollAmount());
            boolean rare = drop.pool() != SlayerDrop.Pool.TOKEN && odds.chancePercent() < MAGIC_FIND_THRESHOLD_PERCENT;
            CrimsonMobLoot.deliver(player, boss, item, rare);
        }
    }

    private static Optional<SlayerDrop> pickFromPool(SkyBlockPlayer player, SlayerDefinition definition, SlayerTier tier,
                                                     int slayerLevel, SlayerDrop.Pool pool,
                                                     DatapointSlayer.SlayerProgress progress,
                                                     @Nullable SlayerDrop selected, double magicFind) {
        double fullPool = 0;
        List<Weighted> entries = new ArrayList<>();
        for (SlayerDrop drop : definition.drops()) {
            if (drop.pool() != pool || !drop.dropsAt(tier)) continue;
            double chance = drop.odds(tier).chancePercent();
            fullPool += chance;
            if (drop.rollable(tier, slayerLevel)) {
                entries.add(new Weighted(drop, adjusted(chance * multiplier(progress, drop, selected), magicFind)));
            }
        }
        if (entries.isEmpty()) return Optional.empty();
        entries.add(new Weighted(null, Math.max(0, 100 - fullPool)));
        Weighted picked = LootRoll.pick(player.getUuid(), entries,
            entry -> entry.drop() == null ? null : entry.drop().item(), Weighted::weight, ThreadLocalRandom.current());
        return Optional.ofNullable(picked.drop());
    }

    private static double multiplier(DatapointSlayer.SlayerProgress progress, SlayerDrop drop, @Nullable SlayerDrop selected) {
        return selected == drop ? SlayerRngMeter.weightMultiplier(progress, drop) : 1;
    }

    private static double adjusted(double chance, double magicFind) {
        return chance < MAGIC_FIND_THRESHOLD_PERCENT ? chance * (1 + magicFind / 100D) : chance;
    }

    public static SkyBlockItem create(SlayerDrop drop, int amount) {
        SkyBlockItem item = new SkyBlockItem(drop.item(), amount);
        if (drop.enchantment() != null) {
            item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(drop.enchantment(), drop.enchantmentLevel()));
        }
        return item;
    }

    public static Text displayName(SlayerDrop drop) {
        if (drop.name() != null) return Text.of(drop.name());
        return create(drop, 1).getDisplayNameText();
    }

    private record Weighted(@Nullable SlayerDrop drop, double weight) {
    }
}
