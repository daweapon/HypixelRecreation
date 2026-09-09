package net.swofty.type.skyblockgeneric.auction;

import net.swofty.commons.skyblock.auctions.AuctionCategories;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.*;

public final class AuctionCategoryResolver {
    private AuctionCategoryResolver() {}

    public static AuctionCategories resolve(SkyBlockItem item) {
        if (item.hasComponent(AuctionCategoryComponent.class))
            return item.getComponent(AuctionCategoryComponent.class).getCategory();
        if (item.hasComponent(StandardItemComponent.class)) {
            var type = item.getComponent(StandardItemComponent.class).getType();
            if (type == StandardItemComponent.StandardItemType.SWORD || type == StandardItemComponent.StandardItemType.BOW)
                return AuctionCategories.WEAPONS;
            if (type.isArmor()) return AuctionCategories.ARMOR;
            if (type.isEquipment()) return AuctionCategories.ACCESSORIES;
        }
        if (item.hasComponent(AccessoryComponent.class) || item.hasComponent(PowerStoneComponent.class))
            return AuctionCategories.ACCESSORIES;
        if (item.hasComponent(PotionDataComponent.class) || item.hasComponent(BrewingIngredientComponent.class)
                || item.hasComponent(FishingBaitComponent.class) || item.hasComponent(ArrowComponent.class))
            return AuctionCategories.CONSUMABLES;
        String material = item.getMaterial().name();
        if (material.endsWith("_SWORD") || material.equals("BOW") || material.equals("CROSSBOW"))
            return AuctionCategories.WEAPONS;
        if (material.endsWith("_HELMET") || material.endsWith("_CHESTPLATE")
                || material.endsWith("_LEGGINGS") || material.endsWith("_BOOTS"))
            return AuctionCategories.ARMOR;
        if (item.hasComponent(PlaceableComponent.class)) return AuctionCategories.BLOCKS;
        return AuctionCategories.TOOLS;
    }
}
