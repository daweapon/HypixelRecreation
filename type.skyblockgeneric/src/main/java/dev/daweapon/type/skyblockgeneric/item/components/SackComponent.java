package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bags.GUISack;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class SackComponent extends SkyBlockItemComponent {
    @Getter
    private final List<ItemType> validItems;
    @Getter
    private final int maxCapacity;

    public SackComponent(List<String> items, int maxCapacity) {
        this.validItems = items.stream()
                .map(ItemType::valueOf)
                .toList();
        this.maxCapacity = maxCapacity;
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new LoreUpdateComponent(new LoreConfig(
                (item, player) -> upgradeLore(item), null, LoreConfig.LoreConfigLocation.AFTER_ABILITY), false));
        addInheritedComponent(new InteractableComponent(this::onInteract, null, null));
    }

    public static final int MAX_UPGRADES = 3;
    private static final int UPGRADE_CAPACITY_MULTIPLIER = 2;

    public int capacityOf(SkyBlockItem sack) {
        return maxCapacity * (1 + UPGRADE_CAPACITY_MULTIPLIER * sack.getAttributeHandler().getSackUpgrades());
    }

    private static List<String> upgradeLore(SkyBlockItem sack) {
        int upgrades = sack.getAttributeHandler().getSackUpgrades();
        if (upgrades == 0) return List.of();
        return List.of("<7>Capacity upgrades: <c>+" + upgrades * UPGRADE_CAPACITY_MULTIPLIER * 100 + "% <8>(" + upgrades + "/" + MAX_UPGRADES + ")");
    }

    private void onInteract(SkyBlockPlayer player, SkyBlockItem item) {
        player.openView(new GUISack(item.getItemType(), false));
    }
}
