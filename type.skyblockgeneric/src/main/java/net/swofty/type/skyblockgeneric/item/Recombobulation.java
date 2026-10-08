package net.swofty.type.skyblockgeneric.item;

import net.swofty.type.skyblockgeneric.item.components.AccessoryComponent;
import net.swofty.type.skyblockgeneric.item.components.ArmorComponent;
import net.swofty.type.skyblockgeneric.item.components.FishingRodPartComponent;
import net.swofty.type.skyblockgeneric.item.components.PetItemComponent;
import net.swofty.type.skyblockgeneric.item.components.ReforgableComponent;
import net.swofty.type.skyblockgeneric.item.components.ReforgeStoneComponent;
import net.swofty.type.skyblockgeneric.item.components.StandardItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;

/**
 * The Recombobulator 3000: one rarity tier up, once per item. It works on armour, weapons, tools and
 * accessories, never on pets, pet items, rod parts or reforge stones, nor on accessories Hypixel
 * marks as unrecombobulatable, nor at the top of a rarity ladder.
 */
public final class Recombobulation {
    public static final String HANDLER_ID = "RECOMBOBULATOR_3000";

    private Recombobulation() {
    }

    public static boolean canRecombobulate(SkyBlockItem item) {
        if (item.isNA() || item.isAir()) return false;
        ItemAttributeHandler handler = item.getAttributeHandler();
        if (handler.isRecombobulated() || handler.isPet()) return false;
        if (item.hasComponent(PetItemComponent.class) || item.hasComponent(FishingRodPartComponent.class)
                || item.hasComponent(ReforgeStoneComponent.class)) return false;
        if (!handler.getBaseRarity().canUpgrade()) return false;
        if (item.hasComponent(AccessoryComponent.class)) return item.getComponent(AccessoryComponent.class).isRecombobulatable();
        return item.hasComponent(ReforgableComponent.class) || item.hasComponent(StandardItemComponent.class)
                || item.hasComponent(ArmorComponent.class);
    }

    public static void registerAnvilCombine() {
        AnvilCombineRegistry.register(HANDLER_ID, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> upgradeItem.getAttributeHandler().setRecombobulated(true),
                (player, upgradeItem, sacrificeItem) -> canRecombobulate(upgradeItem),
                (upgradeItem, sacrificeItem, player) -> 0,
                null));
    }
}
