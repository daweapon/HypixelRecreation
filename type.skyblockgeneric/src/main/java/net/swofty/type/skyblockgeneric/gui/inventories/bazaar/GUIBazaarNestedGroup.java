package net.swofty.type.skyblockgeneric.gui.inventories.bazaar;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.bazaar.BazaarItemSet;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.ItemModelComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Sub-menu shown when a nested-group tile (e.g. Mining -> "Gemstones") is clicked: lists the
// individual item sets that were collapsed into that one tile, each opening its own
// GUIBazaarItemSet as normal. Real Hypixel groups several bazaar categories this way purely to
// fit within the category grid's limited slot count - see BazaarItemSet.nestedGroup.
public class GUIBazaarNestedGroup extends HypixelInventoryGUI {
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
    };

    public GUIBazaarNestedGroup(BazaarCategories category, String groupName, List<BazaarItemSet> sets) {
        super(Text.of("{}", groupName), InventoryType.CHEST_4_ROW);

        set(GUIClickableItem.getCloseItem(31));
        set(GUIClickableItem.getGoBackItem(30, new GUIBazaar(category)));

        int i = 0;
        for (BazaarItemSet set : sets) {
            if (i >= SLOTS.length) break;
            int slot = SLOTS[i];
            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    new GUIBazaarItemSet(category, set).open(p);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    ItemStack.Builder builder = ItemStacks.item(
                            set.displayMaterial.material,
                            1,
                            Text.parse(category.getColor() + set.displayName),
                            List.of(
                                    Text.key("gui_bazaar.main.item_set_products", set.items.size()),
                                    Text.literal(" "),
                                    Text.key("gui_bazaar.main.item_set_click")
                            )
                    );

                    SkyBlockItem displayItem = new SkyBlockItem(set.displayMaterial);
                    if (displayItem.hasComponent(ItemModelComponent.class)) {
                        builder = builder.set(DataComponents.ITEM_MODEL,
                                displayItem.getComponent(ItemModelComponent.class).getItemModel());
                    }

                    return builder;
                }
            });
            i++;
        }
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
