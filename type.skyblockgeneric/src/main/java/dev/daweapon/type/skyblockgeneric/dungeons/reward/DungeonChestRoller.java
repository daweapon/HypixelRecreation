package dev.daweapon.type.skyblockgeneric.dungeons.reward;

import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * Wiki "Loot Rolling Process": the chest's quality is spent like currency. Weighted entries are picked one at
 * a time from those not yet rolled (essence may repeat) that still fit the leftover quality; then the
 * weightless essence filler spends what is left, the guaranteed entries are added, and rare extras such as
 * Dyes roll last. Every pick goes through {@link LootRoll}.
 */
public final class DungeonChestRoller {
    private DungeonChestRoller() {
    }

    public record Rolled(List<DungeonReward> rewards, long addedCost) {
    }

    public static Rolled roll(@Nullable UUID player, List<DungeonLootEntry> entries, int quality, RandomGenerator random) {
        List<DungeonLootEntry> picked = new ArrayList<>();
        Map<String, Long> essence = new LinkedHashMap<>();
        int left = quality;
        while (true) {
            int budget = left;
            List<DungeonLootEntry> eligible = entries.stream()
                    .filter(DungeonLootEntry::isRolled)
                    .filter(entry -> entry.quality() <= budget)
                    .filter(entry -> entry.isEssence() || !picked.contains(entry))
                    .toList();
            if (eligible.isEmpty()) break;
            DungeonLootEntry won = LootRoll.pick(player, eligible, DungeonLootEntry::itemType, DungeonLootEntry::weight, random);
            left -= won.quality();
            if (won.isEssence()) essence.merge(won.essence(), (long) won.amount(), Long::sum);
            else picked.add(won);
            if (won.quality() <= 0) break;
        }
        for (DungeonLootEntry filler : entries) {
            if (!filler.isFiller() || !filler.isEssence() || left < filler.quality()) continue;
            int times = left / filler.quality();
            left -= times * filler.quality();
            essence.merge(filler.essence(), (long) filler.amount() * times, Long::sum);
        }
        List<DungeonReward> rewards = new ArrayList<>();
        long cost = 0;
        for (DungeonLootEntry entry : picked) {
            rewards.add(entry.reward(1));
            cost += entry.cost();
        }
        for (DungeonLootEntry guaranteed : entries) {
            if (!guaranteed.isGuaranteed()) continue;
            if (guaranteed.isEssence()) essence.merge(guaranteed.essence(), (long) guaranteed.amount(), Long::sum);
            else rewards.add(guaranteed.reward(1));
        }
        for (DungeonLootEntry extra : entries) {
            if (extra.isChanceRoll() && LootRoll.roll(player, extra.itemType(), extra.chance(), random)) {
                rewards.add(extra.reward(1));
            }
        }
        essence.forEach((type, amount) -> rewards.add(DungeonReward.essence(type, amount)));
        return new Rolled(List.copyOf(rewards), cost);
    }
}
