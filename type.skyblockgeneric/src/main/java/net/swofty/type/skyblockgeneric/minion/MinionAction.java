package net.swofty.type.skyblockgeneric.minion;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.crafting.FurnaceRecipeRegistry;
import net.swofty.type.skyblockgeneric.item.components.MinionShippingComponent;
import net.swofty.type.skyblockgeneric.item.components.MinionFuelComponent;
import net.swofty.type.skyblockgeneric.item.components.SellableComponent;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.minion.extension.MinionExtensionData;
import net.swofty.type.skyblockgeneric.minion.extension.MinionExtensions;
import net.swofty.type.skyblockgeneric.minion.extension.extensions.MinionShippingExtension;

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

    public static void onMinionIteration(IslandMinionData.IslandMinion islandMinion,
                                             SkyBlockMinion minion,
                                             List<SkyBlockItem> items) {
        MinionExtensionData extensionData = islandMinion.getExtensionData();
        SkyBlockItem fuel = extensionData.getFuel();
        double fuelMultiplier = fuel == null ? 1.0
                : fuel.getComponent(MinionFuelComponent.class).getOutputMultiplier();
        double outputMultiplier = fuelMultiplier * ElectionPerks.factor(SkyBlockMayor.Perk.TURBO_MINIONS);

        for (SkyBlockItem item : items) {
            if (extensionData.hasMinionUpgrade(ItemType.AUTO_SMELTER)) {
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

                extensionData.setData(MinionExtensions.SHIPPING_SLOT.getSlots()[0], shippingExtension);
            }
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
