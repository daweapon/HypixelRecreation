package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.bazaar.BazaarConnector;
import net.swofty.type.skyblockgeneric.bazaar.BazaarItemSet;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.function.Supplier;

// Menu of the products of one item set, arranged for the set's size (compact, or paged past ten products)
public class GUIBazaarItemSet extends GUIBazaarMenu {
    private final BazaarItemSet itemSet;
    private final String titleParent;
    private final String backLabel;
    private final Supplier<HypixelInventoryGUI> back;

    public GUIBazaarItemSet(BazaarCategories category, BazaarItemSet itemSet) {
        this(category, itemSet, 0);
    }

    public GUIBazaarItemSet(BazaarCategories category, BazaarItemSet itemSet, int page) {
        this(category, itemSet, page, itemSet.nestedGroup == null ? category.displayName() : itemSet.nestedGroup,
                itemSet.nestedGroup == null ? "Bazaar" : itemSet.nestedGroup,
                () -> parentOf(category, itemSet));
    }

    public GUIBazaarItemSet(BazaarCategories category, BazaarItemSet itemSet, int page, String titleParent,
                            String backLabel, Supplier<HypixelInventoryGUI> back) {
        super(category, titleParent, itemSet.name(), new BazaarSetLayout(itemSet.items.size()), page, backLabel, back);
        this.itemSet = itemSet;
        this.titleParent = titleParent;
        this.backLabel = backLabel;
        this.back = back;
    }

    static HypixelInventoryGUI parentOf(BazaarCategories category, BazaarItemSet itemSet) {
        if (itemSet.nestedGroup == null) {
            return new GUIBazaar(category);
        }
        return new GUIBazaarNestedGroup(category, itemSet.nestedGroup, category.nestedSets(itemSet.nestedGroup));
    }

    @Override
    protected HypixelInventoryGUI openPage(int target) {
        return new GUIBazaarItemSet(category, itemSet, target, titleParent, backLabel, back);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        refreshState(player, true);
        int[] slots = layout.slots(page);
        int first = layout.firstIndex(page);
        for (int i = 0; i < slots.length; i++) {
            ItemType itemType = itemSet.items.get(first + i);
            int slot = slots[i];
            setProductTile(slot, itemType, null);
            player.getBazaarConnector().getItemStatistics(itemType)
                    .thenAccept((BazaarConnector.BazaarStatistics stats) -> {
                        setProductTile(slot, itemType, stats);
                        updateItemStacks(getInventory(), getPlayer());
                    })
                    .exceptionally(throwable -> {
                        Logger.error(throwable, "Failed to fetch bazaar data for {}", itemType.name());
                        return null;
                    });
        }
        updateItemStacks(getInventory(), getPlayer());
    }
}
