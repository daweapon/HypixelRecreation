package dev.daweapon.type.skyblockgeneric.rabbits;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class RabbitFactions {
    public static final int UNLOCK_FACTORY_LEVEL = 5;
    public static final int MAX_LEVEL = 5;
    public static final long SWITCH_COST = 1_000_000_000L;
    private static final long[] UPGRADE_COSTS = {1_000_000_000L, 2_500_000_000L, 5_000_000_000L, 7_500_000_000L};

    private RabbitFactions() {
    }

    public enum Result {
        SUCCESS, LOCKED, NOT_ENOUGH_CHOCOLATE, ALREADY_MEMBER, NOT_A_MEMBER, MAXED
    }

    public static boolean isUnlocked(ChocolateFactoryData data) {
        return data.getFactoryLevel() >= UNLOCK_FACTORY_LEVEL;
    }

    public static RabbitRarity maxRarity(int level) {
        return RabbitRarity.values()[Math.max(0, Math.min(MAX_LEVEL, level) - 1)];
    }

    public static long joinCost(ChocolateFactoryData data) {
        return data.getFaction() == null ? 0 : SWITCH_COST;
    }

    public static long upgradeCost(int currentLevel) {
        if (currentLevel < 1 || currentLevel >= MAX_LEVEL) return 0;
        return UPGRADE_COSTS[currentLevel - 1];
    }

    public static Result join(SkyBlockPlayer player, RabbitFaction faction) {
        ChocolateFactoryData current = ChocolateFactoryHelper.getData(player);
        if (!isUnlocked(current)) return Result.LOCKED;
        if (current.getFaction() == faction) return Result.ALREADY_MEMBER;
        long cost = joinCost(current);
        if (current.getChocolate() < cost) return Result.NOT_ENOUGH_CHOCOLATE;
        ChocolateFactoryHelper.mutateData(player, data -> {
            data.removeChocolate(cost);
            data.addChocolateSpent(cost);
            data.setRabbitFaction(faction.name());
            data.setFactionLevel(1);
        });
        return Result.SUCCESS;
    }

    public static Result upgrade(SkyBlockPlayer player) {
        ChocolateFactoryData current = ChocolateFactoryHelper.getData(player);
        if (!isUnlocked(current)) return Result.LOCKED;
        if (current.getFaction() == null) return Result.NOT_A_MEMBER;
        if (current.getFactionLevel() >= MAX_LEVEL) return Result.MAXED;
        long cost = upgradeCost(current.getFactionLevel());
        if (current.getChocolate() < cost) return Result.NOT_ENOUGH_CHOCOLATE;
        ChocolateFactoryHelper.mutateData(player, data -> {
            data.removeChocolate(cost);
            data.addChocolateSpent(cost);
            data.setFactionLevel(data.getFactionLevel() + 1);
        });
        return Result.SUCCESS;
    }

    public static List<RabbitRarity> eligibleRarities(ChocolateFactoryData data) {
        if (data.getFaction() == null || !isUnlocked(data) || data.getFactionLevel() <= 0) return List.of();
        RabbitRarity max = maxRarity(data.getFactionLevel());
        List<RabbitRarity> rarities = new ArrayList<>();
        for (RabbitRarity rarity : RabbitRarity.values()) {
            if (rarity.ordinal() <= max.ordinal()) rarities.add(rarity);
        }
        return rarities;
    }

    public static List<ItemType> eligibleItems(ChocolateFactoryData data) {
        List<RabbitRarity> rarities = eligibleRarities(data);
        if (rarities.isEmpty()) return List.of();
        List<ItemType> items = new ArrayList<>();
        for (FactionRabbit rabbit : FactionRabbit.of(data.getFaction())) {
            if (rarities.contains(rabbit.getRarity()) && rabbit.getItemType() != null) items.add(rabbit.getItemType());
        }
        return items;
    }

    public static @Nullable FactionRabbit pick(UUID player, RabbitFaction faction, RabbitRarity rarity, RandomGenerator random) {
        List<FactionRabbit> pool = FactionRabbit.of(faction, rarity);
        if (pool.isEmpty()) return null;
        return LootRoll.pick(player, pool, FactionRabbit::getItemType, ignored -> 1.0, random);
    }

    public static void give(SkyBlockPlayer player, @Nullable FactionRabbit rabbit) {
        ItemType type = rabbit == null ? null : rabbit.getItemType();
        if (type == null) return;
        player.addAndUpdateItem(new SkyBlockItem(type));
        player.sendMessage("<d><l>HOPPITY'S HUNT</l> <7>You found {} <7>the <a>" + rabbit.getFaction().getName()
                + " Faction Rabbit<7>!", rabbit.getFormattedName());
    }

    public static void registerInteractables() {
        InteractableRegistry.register("FACTION_RABBIT", InteractableItemConfig.builder()
                .rightClickHandler(RabbitFactions::addToFactory).build());
    }

    private static void addToFactory(SkyBlockPlayer player, SkyBlockItem item) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        FactionRabbit rabbit = type == null ? null : FactionRabbit.byItemId(type.name());
        if (rabbit == null) return;
        if (ChocolateFactoryHelper.getData(player).hasFactionRabbit(rabbit)) {
            player.sendMessage("<c>You have already added {} <c>to your Chocolate Factory!", rabbit.getFormattedName());
            return;
        }
        ChocolateFactoryHelper.updateProduction(player);
        ChocolateFactoryHelper.mutateData(player, data -> data.getFactionRabbits().add(rabbit.name()));
        SkyBlockItem remaining = item.clone();
        remaining.setAmount(item.getAmount() - 1);
        player.setItemInHand(remaining.getAmount() > 0 ? remaining : null);
        player.sendMessage("<d><l>NEW RABBIT! <7>{} <7>joined your Chocolate Factory! <6>" + bonusText(rabbit),
                rabbit.getFormattedName());
    }

    private static String bonusText(FactionRabbit rabbit) {
        List<String> parts = new ArrayList<>();
        if (rabbit.getChocolateBonus() > 0) parts.add("+" + rabbit.getChocolateBonus() + " Chocolate");
        if (rabbit.getMultiplierBonus() > 0) parts.add("+" + String.format("%.3fx", rabbit.getMultiplierBonus()) + " Chocolate");
        return String.join(" and ", parts) + " per second";
    }
}
