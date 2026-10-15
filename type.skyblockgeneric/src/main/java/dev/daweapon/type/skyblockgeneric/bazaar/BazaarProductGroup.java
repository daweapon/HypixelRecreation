package dev.daweapon.type.skyblockgeneric.bazaar;

import java.util.List;

// The levels of one enchantment sold as one tile, labelled the way the real Bazaar does: "Drain 4-5", "Cultivating 1".
public record BazaarProductGroup(String name, int firstLevel, int lastLevel, List<BazaarProduct> products) {
    public String label() {
        return firstLevel == lastLevel ? name + " " + firstLevel : name + " " + firstLevel + "-" + lastLevel;
    }

    public boolean isSingle() {
        return products.size() == 1;
    }

    public BazaarProduct first() {
        return products.getFirst();
    }
}
