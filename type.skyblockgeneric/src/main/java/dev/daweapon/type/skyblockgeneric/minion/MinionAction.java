package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.type.skyblockgeneric.achievement.SkyBlockItemAchievements;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.item.ItemQuantifiable;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.crafting.FurnaceRecipeRegistry;
import dev.daweapon.type.skyblockgeneric.item.components.MinionShippingComponent;
import dev.daweapon.type.skyblockgeneric.item.components.MinionFuelComponent;
import dev.daweapon.type.skyblockgeneric.item.components.SellableComponent;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.minion.extension.MinionExtensionData;
import dev.daweapon.type.skyblockgeneric.minion.extension.MinionExtensions;
import dev.daweapon.type.skyblockgeneric.minion.extension.extensions.MinionShippingExtension;

import java.util.List;

// Base type for a minion's per-tick behaviour (mining, farming, fishing, etc.), producing items and checking fuel/materials.
public abstract class MinionAction {
    private static final double KRAMPUS_RED_GIFT_CHANCE_PERCENT = 0.0045;

    public abstract @NonNull List<SkyBlockItem> onAction(MinionActionEvent event, IslandMinionData.IslandMinion minion, Instance island);
    public abstract boolean checkMaterials(IslandMinionData.IslandMinion minion, Instance island);

    @Getter
    @Setter
    public static class MinionActionEvent {
        private Pos toLook;
        private Runnable action;
    }

    protected static boolean isStorageFull(IslandMinionData.IslandMinion minion) {
        int capacity = minion.getMinion().asSkyBlockMinion().getTiers().get(minion.getTier() - 1).storage();
        return minion.getItemsInMinion().stream().mapToInt(ItemQuantifiable::getAmount).sum() >= capacity;
    }

    public static void onMinionIteration(IslandMinionData.IslandMinion islandMinion,
                                             SkyBlockMinion minion,
                                             List<SkyBlockItem> items) {
        MinionExtensionData extensionData = islandMinion.getExtensionData();
        SkyBlockItem fuel = extensionData.getFuel();
        double fuelMultiplier = fuel == null ? 1.0
                : fuel.getComponent(MinionFuelComponent.class).getOutputMultiplier();
        double outputMultiplier = fuelMultiplier * ElectionPerks.factor(SkyBlockMayor.Perk.TURBO_MINIONS);

        boolean flintShovel = extensionData.hasMinionUpgrade(ItemType.FLINT_SHOVEL);
        for (SkyBlockItem item : items) {
            item = MinionUpgradeEffects.convert(item, minion, flintShovel);
            if (extensionData.hasMinionUpgrade(ItemType.AUTO_SMELTER) || extensionData.hasMinionUpgrade(ItemType.DWARVEN_COMPACTOR)) {
                item = FurnaceRecipeRegistry.smelt(item).orElse(item);
            }
            if (outputMultiplier > 1.0) {
                item.setAmount((int) Math.floor(item.getAmount() * outputMultiplier));
            }
            boolean hasAdded = islandMinion.addItem(item);
            double sellAmount = !item.hasComponent(SellableComponent.class) ? 0 :
                    item.getComponent(SellableComponent.class).getSellValue();

            if (!hasAdded && sellAmount > 0) {
                MinionShippingExtension shippingExtension = (MinionShippingExtension)
                        extensionData.getOfType(MinionShippingExtension.class);
                if (shippingExtension.getItemTypePassedIn() == null)
                    return;

                SkyBlockItem shippingItem = new SkyBlockItem(shippingExtension.getItemTypePassedIn());
                double percentage = shippingItem.getComponent(MinionShippingComponent.class).getPercentageOfOriginalPrice();
                double sellValue = sellAmount * item.getAmount() * (percentage / 100);
                shippingExtension.addCoins(sellValue, item.getAmount());
                SkyBlockItemAchievements.onMinionSold(islandMinion, shippingExtension.getItemTypePassedIn());

                extensionData.setData(MinionExtensions.SHIPPING_SLOT.getSlots()[0], shippingExtension);
            }
        }

        if (extensionData.hasMinionUpgrade(ItemType.SUPER_COMPACTOR_3000) || extensionData.hasMinionUpgrade(ItemType.DWARVEN_COMPACTOR)) {
            MinionCompaction.compact(islandMinion);
        }

        if (extensionData.hasMinionUpgrade(ItemType.COMPACTOR)) {
            MinionBlockCompaction.compact(islandMinion);
        }

        if (extensionData.hasMinionUpgrade(ItemType.DIAMOND_SPREADING)) {
            int baseDrops = items.stream().mapToInt(SkyBlockItem::getAmount).sum();
            int diamonds = 0;
            for (int i = 0; i < baseDrops; i++) {
                if (Math.random() < 0.1) diamonds++;
            }
            if (diamonds > 0) islandMinion.addItem(new SkyBlockItem(ItemType.DIAMOND, diamonds));
        }

        if (extensionData.hasMinionUpgrade(ItemType.KRAMPUS_HELMET)) {
            int generated = items.stream().mapToInt(SkyBlockItem::getAmount).sum();
            int redGifts = 0;
            for (int i = 0; i < generated; i++) {
                if (LootRoll.roll(null, ItemType.RED_GIFT, KRAMPUS_RED_GIFT_CHANCE_PERCENT)) redGifts++;
            }
            if (redGifts > 0) islandMinion.addItem(new SkyBlockItem(ItemType.RED_GIFT, redGifts));
        }
    }

}
