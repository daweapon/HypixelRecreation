package dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar;

import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarCategories;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarConnector;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarItemSet;
import dev.daweapon.type.skyblockgeneric.bazaar.BazaarProductGroup;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.List;

// Menu of the enchantment groups of one set (Normal or Ultimate Enchantments): one tile per enchantment,
// holding its levels. A tile with one level shows its prices and opens the product directly.
public class GUIBazaarEnchantments extends GUIBazaarMenu {
    private static final String PARENT = "Enchantments";

    private final BazaarItemSet itemSet;
    private final List<BazaarProductGroup> groups;

    public GUIBazaarEnchantments(BazaarCategories category, BazaarItemSet itemSet) {
        this(category, itemSet, 0);
    }

    public GUIBazaarEnchantments(BazaarCategories category, BazaarItemSet itemSet, int page) {
        super(category, PARENT, itemSet.name(), new BazaarSetLayout(itemSet.groups().size()), page, PARENT,
                () -> new GUIBazaarNestedGroup(category, PARENT, category.nestedSets(PARENT)));
        this.itemSet = itemSet;
        this.groups = itemSet.groups();

        int[] slots = layout.slots(this.page);
        int first = layout.firstIndex(this.page);
        for (int i = 0; i < slots.length; i++) {
            setGroupTile(slots[i], groups.get(first + i), null);
        }
    }

    private void setGroupTile(int slot, BazaarProductGroup group, BazaarConnector.BazaarStatistics stats) {
        setProductGroupTile(slot, group, stats, () -> new GUIBazaarItemSet(category,
                BazaarItemSet.ofProducts(group.first(), group.label(), null, group.products()), 0,
                itemSet.name(), itemSet.name(), () -> new GUIBazaarEnchantments(category, itemSet, page)));
    }

    @Override
    protected HypixelInventoryGUI openPage(int target) {
        return new GUIBazaarEnchantments(category, itemSet, target);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        refreshState(player, true);
        int[] slots = layout.slots(page);
        int first = layout.firstIndex(page);
        for (int i = 0; i < slots.length; i++) {
            BazaarProductGroup group = groups.get(first + i);
            if (!group.isSingle()) {
                continue;
            }
            int slot = slots[i];
            player.getBazaarConnector().getItemStatistics(group.first())
                    .thenAccept((BazaarConnector.BazaarStatistics stats) -> {
                        setGroupTile(slot, group, stats);
                        updateItemStacks(getInventory(), getPlayer());
                    })
                    .exceptionally(throwable -> {
                        Logger.error(throwable, "Failed to fetch bazaar data for {}", group.label());
                        return null;
                    });
        }
    }
}
