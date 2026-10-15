package dev.daweapon.type.skyblockgeneric.bazaar;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

// A named group of related products displayed together under one icon in a bazaar category.
// Sets sharing a non-null `nestedGroup` are collapsed into one tile in the category grid
// (e.g. the Crimson Isle sets under one "Crimson Isle" tile) and open as a sub-menu of sets.
// A set with a single product is shown as a product tile with its prices instead of a group tile.
// Most sets hold ItemTypes; essence sets hold other products, and enchantment sets hold groups of levels.
public class BazaarItemSet {
    public static final int NO_POSITION = -1;

    public List<ItemType> items;
    public ItemType displayMaterial;
    public String displayName;
    public String nestedGroup;
    public String descriptionKey;
    public int gridPosition = NO_POSITION;
    public Rarity arrowRarity;

    private final BazaarProduct icon;
    private final Supplier<List<BazaarProduct>> products;
    private final Supplier<List<BazaarProductGroup>> groups;
    private volatile List<BazaarProduct> resolvedProducts;
    private volatile List<BazaarProductGroup> resolvedGroups;

    public BazaarItemSet(ItemType displayMaterial, String displayName, ItemType... items) {
        this(displayMaterial, displayName, null, items);
    }

    public BazaarItemSet(ItemType displayMaterial, String displayName, String nestedGroup, ItemType... items) {
        this.displayMaterial = displayMaterial;
        this.displayName = displayName;
        this.nestedGroup = nestedGroup;
        this.items = List.of(items);
        this.icon = BazaarProducts.of(displayMaterial);
        this.products = () -> Arrays.stream(items).map(item -> (BazaarProduct) BazaarProducts.of(item)).toList();
        this.groups = null;
    }

    public BazaarItemSet(ItemType product) {
        this(product, (String) null, (String) null, product);
    }

    private BazaarItemSet(BazaarProduct icon, String displayName, String nestedGroup, Supplier<List<BazaarProduct>> products,
                          Supplier<List<BazaarProductGroup>> groups) {
        this.icon = icon;
        this.displayName = displayName;
        this.nestedGroup = nestedGroup;
        this.items = List.of();
        this.products = products;
        this.groups = groups;
    }

    public static BazaarItemSet product(ItemType product, String nestedGroup) {
        return new BazaarItemSet(product, null, nestedGroup, product);
    }

    public static BazaarItemSet ofProducts(BazaarProduct icon, String displayName, String nestedGroup, List<? extends BazaarProduct> products) {
        return new BazaarItemSet(icon, displayName, nestedGroup, () -> List.copyOf(products), null);
    }

    public static BazaarItemSet ofGroups(BazaarProduct icon, String displayName, String nestedGroup, Supplier<List<BazaarProductGroup>> groups) {
        return new BazaarItemSet(icon, displayName, nestedGroup, null, groups);
    }

    public String name() {
        return displayName != null ? displayName : icon.name();
    }

    public BazaarProduct icon() {
        return icon;
    }

    public List<BazaarProduct> products() {
        if (resolvedProducts == null) {
            resolvedProducts = products != null ? products.get() : groups().stream().flatMap(group -> group.products().stream()).toList();
        }
        return resolvedProducts;
    }

    public List<BazaarProductGroup> groups() {
        if (groups == null) {
            return List.of();
        }
        if (resolvedGroups == null) {
            resolvedGroups = groups.get();
        }
        return resolvedGroups;
    }

    public boolean isGrouped() {
        return groups != null;
    }

    public int productCount() {
        return isGrouped() ? groups().size() : products().size();
    }

    public Rarity arrowRarity() {
        return arrowRarity != null ? arrowRarity : icon.rarity();
    }

    public BazaarItemSet described(String descriptionKey) {
        this.descriptionKey = descriptionKey;
        return this;
    }

    public BazaarItemSet at(int gridPosition) {
        this.gridPosition = gridPosition;
        return this;
    }

    public BazaarItemSet arrow(Rarity rarity) {
        this.arrowRarity = rarity;
        return this;
    }

    public boolean isSingleProduct() {
        return !isGrouped() && products().size() == 1;
    }
}
