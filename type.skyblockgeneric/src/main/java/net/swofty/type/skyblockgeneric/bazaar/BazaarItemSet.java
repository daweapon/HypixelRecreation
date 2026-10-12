package net.swofty.type.skyblockgeneric.bazaar;

import net.swofty.commons.skyblock.item.ItemType;

import java.util.List;

// A named group of related ItemTypes displayed together under one icon in a bazaar category.
// Sets sharing a non-null `nestedGroup` are collapsed into one tile in the category grid
// (e.g. the Crimson Isle sets under one "Crimson Isle" tile) and open as a sub-menu of sets.
// A set with a single item is shown as a product tile with its prices instead of a group tile.
public class BazaarItemSet {
    public static final int NO_POSITION = -1;

    public List<ItemType> items;
    public ItemType displayMaterial;
    public String displayName;
    public String nestedGroup;
    public String descriptionKey;
    public int gridPosition = NO_POSITION;

    public BazaarItemSet(ItemType displayMaterial, String displayName, ItemType... items) {
        this(displayMaterial, displayName, null, items);
    }

    public BazaarItemSet(ItemType displayMaterial, String displayName, String nestedGroup, ItemType... items) {
        this.displayMaterial = displayMaterial;
        this.displayName = displayName;
        this.nestedGroup = nestedGroup;
        this.items = List.of(items);
    }

    public BazaarItemSet(ItemType product) {
        this(product, (String) null, (String) null, product);
    }

    public static BazaarItemSet product(ItemType product, String nestedGroup) {
        return new BazaarItemSet(product, null, nestedGroup, product);
    }

    public String name() {
        return displayName != null ? displayName : BazaarNames.of(displayMaterial);
    }

    public BazaarItemSet described(String descriptionKey) {
        this.descriptionKey = descriptionKey;
        return this;
    }

    public BazaarItemSet at(int gridPosition) {
        this.gridPosition = gridPosition;
        return this;
    }

    public boolean isSingleProduct() {
        return items.size() == 1;
    }
}
