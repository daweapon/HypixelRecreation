package dev.daweapon.type.skyblockgeneric.crimson.kuudra.reward;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraLootTable;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.FollowerStep;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerProgress;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

/**
 * Rolls a won run's Free and Paid chests (wiki Kuudra/Loot). The Free chest holds Crimson Essence; the
 * Paid chest holds one primary and one secondary roll, the guaranteed Kuudra Teeth, the guaranteed
 * Kraken Shards and Crimson Essence. Every chance goes through {@link LootRoll}.
 */
public final class KuudraRewards {
    private KuudraRewards() {
    }

    public static KuudraRun roll(SkyBlockPlayer player, KuudraTier tier) {
        boolean teethPlaque = KuudraFollowerProgress.has(player, FollowerStep.RELIC_UPGRADED);
        KuudraRun run = roll(player.getUuid(), tier, teethPlaque, KuudraConfig.get(), ThreadLocalRandom.current(), System.currentTimeMillis());
        double extraTeeth = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("R22"));
        if (extraTeeth <= 0 || !LootRoll.roll(player.getUuid(), ItemType.KUUDRA_TEETH, extraTeeth)) return run;
        return new KuudraRun(run.id(), run.tier(), run.completedAt(), run.free(), withExtraTeeth(run.paid()), run.opened());
    }

    public static List<KuudraReward> withExtraTeeth(List<KuudraReward> paid) {
        List<KuudraReward> result = new ArrayList<>();
        boolean added = false;
        for (KuudraReward reward : paid) {
            if (!added && ItemType.KUUDRA_TEETH.name().equals(reward.itemId())) {
                result.add(KuudraReward.item(reward.itemId(), reward.amount() + 1));
                added = true;
            } else {
                result.add(reward);
            }
        }
        if (!added) result.add(KuudraReward.item(ItemType.KUUDRA_TEETH.name(), 1));
        return result;
    }

    public static KuudraRun roll(UUID player, KuudraTier tier, boolean teethPlaque, KuudraConfig config,
                                 RandomGenerator random, long now) {
        return KuudraRun.create(tier, now, free(player, tier, config.loot(), random), paid(player, tier, teethPlaque, config, random));
    }

    static List<KuudraReward> free(UUID player, KuudraTier tier, KuudraLootTable loot, RandomGenerator random) {
        List<Long> amounts = loot.freeEssence().get(tier);
        long amount = LootRoll.pick(player, amounts, _ -> null, _ -> 1, random);
        return List.of(KuudraReward.essence(amount));
    }

    static List<KuudraReward> paid(UUID player, KuudraTier tier, boolean teethPlaque, KuudraConfig config, RandomGenerator random) {
        KuudraLootTable loot = config.loot();
        List<KuudraReward> rewards = new ArrayList<>();
        rollSlot(player, tier, loot.primary(), teethPlaque, random, rewards);
        rollSlot(player, tier, loot.secondary(), teethPlaque, random, rewards);

        KuudraLootTable.Guarantee guarantee = loot.guaranteed().get(tier);
        List<Integer> teethAmounts = IntStream.rangeClosed(guarantee.teethMin(), guarantee.teethMax()).boxed().toList();
        int teeth = LootRoll.pick(player, teethAmounts, _ -> null, _ -> 1, random);
        rewards.add(KuudraReward.item(ItemType.KUUDRA_TEETH.name(), teeth));

        double shards = config.tier(tier).krakenShards();
        int krakenShards = (int) Math.floor(shards);
        double extraChance = (shards - krakenShards) * 100;
        if (extraChance > 0 && LootRoll.roll(player, ItemType.SHARD_KRAKEN, extraChance, random)) krakenShards++;
        if (krakenShards > 0) rewards.add(KuudraReward.item(ItemType.SHARD_KRAKEN.name(), krakenShards));

        rewards.add(KuudraReward.essence(guarantee.essence()));
        return rewards;
    }

    private static void rollSlot(UUID player, KuudraTier tier, List<KuudraLootTable.Entry> entries, boolean teethPlaque,
                                 RandomGenerator random, List<KuudraReward> rewards) {
        List<KuudraLootTable.Entry> candidates = KuudraLootTable.candidates(entries, tier, teethPlaque);
        if (candidates.isEmpty()) return;
        KuudraLootTable.Entry won = LootRoll.pick(player, candidates,
                entry -> entry.isEssence() ? null : entry.itemType(tier), entry -> entry.weight(tier), random);
        rewards.add(won.reward(tier));
    }
}
