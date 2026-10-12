package net.swofty.type.skyblockgeneric.bazaar;

import java.util.List;

// One tile of a category grid: either a single item set, or a nested group collapsing several sets
// (which opens a menu of those sets).
public record BazaarTile(String name, List<BazaarItemSet> sets, boolean nested, int gridPosition) {
    public BazaarItemSet first() {
        return sets.getFirst();
    }

    public int productCount() {
        return nested ? sets.size() : first().items.size();
    }

    public boolean isSingleProduct() {
        return !nested && first().isSingleProduct();
    }

    public String descriptionKey() {
        return nested ? null : first().descriptionKey;
    }
}
