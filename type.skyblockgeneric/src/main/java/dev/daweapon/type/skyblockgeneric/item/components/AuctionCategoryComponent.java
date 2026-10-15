package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.commons.skyblock.auctions.AuctionCategories;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

public class AuctionCategoryComponent extends SkyBlockItemComponent {
    @Getter
    private final AuctionCategories category;

    public AuctionCategoryComponent(String category) {
        this.category = AuctionCategories.valueOf(category);
    }
}