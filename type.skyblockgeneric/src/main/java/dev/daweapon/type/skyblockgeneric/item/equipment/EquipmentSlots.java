package dev.daweapon.type.skyblockgeneric.item.equipment;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.StandardItemComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockInventory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum EquipmentSlots {
    NECKLACE(SkyBlockInventory::getNecklace, SkyBlockInventory::setNecklace),
    CLOAK(SkyBlockInventory::getCloak, SkyBlockInventory::setCloak),
    BELT(SkyBlockInventory::getBelt, SkyBlockInventory::setBelt),
    GLOVES(SkyBlockInventory::getGloves, SkyBlockInventory::setGloves);

    private final Function<SkyBlockInventory, UnderstandableSkyBlockItem> getter;
    private final BiConsumer<SkyBlockInventory, UnderstandableSkyBlockItem> setter;

    EquipmentSlots(Function<SkyBlockInventory, UnderstandableSkyBlockItem> getter,
                   BiConsumer<SkyBlockInventory, UnderstandableSkyBlockItem> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public static EquipmentSlots forItem(ItemStack stack) {
        SkyBlockItem item = new SkyBlockItem(stack);
        if (item.isNA() || item.isAir() || !item.hasComponent(StandardItemComponent.class)) return null;
        return switch (item.getComponent(StandardItemComponent.class).getType()) {
            case NECKLACE -> NECKLACE;
            case CLOAK -> CLOAK;
            case BELT -> BELT;
            case GLOVES, BRACELET -> GLOVES;
            default -> null;
        };
    }

    public SkyBlockItem equipped(SkyBlockPlayer player) {
        return new SkyBlockItem(getter.apply(storedInventory(player)));
    }

    public boolean isEmpty(SkyBlockPlayer player) {
        return equipped(player).isAir();
    }

    public void equip(SkyBlockPlayer player, ItemStack stack) {
        setter.accept(storedInventory(player), new SkyBlockItem(stack).toUnderstandable());
    }

    public ItemStack unequip(SkyBlockPlayer player) {
        SkyBlockItem current = equipped(player);
        setter.accept(storedInventory(player), new SkyBlockItem(Material.AIR).toUnderstandable());
        return current.isAir() ? ItemStack.AIR : current.getItemStack();
    }

    private static SkyBlockInventory storedInventory(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class)
                .getValue();
    }
}
