package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.bazaar.BazaarConnector;
import net.swofty.type.skyblockgeneric.bazaar.BazaarItemSet;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.List;

// Menu of a nested group tile (e.g. Combat -> Catacombs): one tile per item set collapsed into it.
// Sets holding a single product show their prices, the others list their products.
public class GUIBazaarNestedGroup extends GUIBazaarMenu {
    private final String groupName;
    private final List<BazaarItemSet> sets;

    public GUIBazaarNestedGroup(BazaarCategories category, String groupName, List<BazaarItemSet> sets) {
        this(category, groupName, sets, 0);
    }

    public GUIBazaarNestedGroup(BazaarCategories category, String groupName, List<BazaarItemSet> sets, int page) {
        super(category, category.displayName(), groupName, new BazaarSetLayout(sets.size()), page,
                "Bazaar", () -> new GUIBazaar(category));
        this.groupName = groupName;
        this.sets = sets;

        int[] slots = layout.slots(this.page);
        int first = layout.firstIndex(this.page);
        for (int i = 0; i < slots.length; i++) {
            setGroupTile(slots[i], sets.get(first + i), null);
        }
    }

    @Override
    protected HypixelInventoryGUI openPage(int target) {
        return new GUIBazaarNestedGroup(category, groupName, sets, target);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        refreshState(player, true);
        int[] slots = layout.slots(page);
        int first = layout.firstIndex(page);
        for (int i = 0; i < slots.length; i++) {
            BazaarItemSet set = sets.get(first + i);
            if (!set.isSingleProduct()) {
                continue;
            }
            int slot = slots[i];
            player.getBazaarConnector().getItemStatistics(set.items.getFirst())
                    .thenAccept((BazaarConnector.BazaarStatistics stats) -> {
                        setGroupTile(slot, set, stats);
                        updateItemStacks(getInventory(), getPlayer());
                    })
                    .exceptionally(throwable -> {
                        Logger.error(throwable, "Failed to fetch bazaar data for {}", set.name());
                        return null;
                    });
        }
    }
}
