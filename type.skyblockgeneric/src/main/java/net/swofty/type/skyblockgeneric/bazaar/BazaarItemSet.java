package net.swofty.type.skyblockgeneric.bazaar;

import net.swofty.commons.skyblock.item.ItemType;

import java.util.List;

// A named group of related ItemTypes displayed together under one icon in a bazaar category.
// Sets sharing a non-null `nestedGroup` are collapsed into one tile in the category grid
// (e.g. all 12 gemstone types under one "Gemstones" tile) since a category can only show so
// many tiles at once - opening that tile shows the individual sets as a sub-menu.
public class BazaarItemSet {
    public List<ItemType> items;
    public ItemType displayMaterial;
    public String displayName;
    public String nestedGroup;

    public BazaarItemSet(ItemType displayMaterial, String displayName, ItemType... items) {
        this(displayMaterial, displayName, null, items);
    }

    public BazaarItemSet(ItemType displayMaterial, String displayName, String nestedGroup, ItemType... items) {
        this.displayMaterial = displayMaterial;
        this.displayName = displayName;
        this.nestedGroup = nestedGroup;
        this.items = List.of(items);
    }
}
