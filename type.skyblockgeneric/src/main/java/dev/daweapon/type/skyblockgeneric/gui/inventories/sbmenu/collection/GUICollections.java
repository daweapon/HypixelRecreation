package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.collection;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategory;
import dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra.GUIKuudraCollection;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// GUI hub for browsing item collection categories and overall progress
public class GUICollections extends StatelessView {
    private static final int[] DISPLAY_SLOTS = {
            20, 21, 22, 23, 24,
            31
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Collections", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<String> rendered = new ArrayList<>();
            player.getCollection().getDisplay(rendered);

            List<Text> lore = new ArrayList<>();
            lore.add(Text.of("<7>View all of the items available in"));
            lore.add(Text.of("<7>SkyBlock. Collect more of an item to"));
            lore.add(Text.of("<7>unlock rewards on your way to"));
            lore.add(Text.of("<7>becoming a master of SkyBlock!"));
            lore.add(Text.empty());
            lore.addAll(rendered.stream().map(Text::parse).toList());
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to view!"));

            return ItemStacks.item(Material.PAINTING, 1, Text.of("<a>Collections"), lore);
        });

        layout.slot(50, (s, c) -> ItemStacks.head(
                        "ebcc099f3a00ece0e5c4b31d31c828e52b06348d0a4eac11f3fcbef3c05cb407", """
                                <a>Crafted Minions
                                <7>View all the unique minions that you
                                <7>have crafted.

                                <e>Click to view!"""),
                (click, c) -> {
                    c.player().openView(new GUICraftedMinions(), GUICraftedMinions.createInitialState());
                });

        ArrayList<CollectionCategory> allCategories = CollectionCategories.getCategories();
        for (int i = 0; i < DISPLAY_SLOTS.length && i < allCategories.size(); i++) {
            CollectionCategory category = allCategories.get(i);
            int slot = DISPLAY_SLOTS[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                ArrayList<String> display = new ArrayList<>();
                player.getCollection().getDisplay(display, category);

                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<7>View your {} Collections!", category.getName()));
                lore.add(Text.empty());
                lore.addAll(display.stream().map(Text::parse).toList());

                return ItemStacks.item(category.getDisplayIcon(), 1,
                        Text.of("<a>{} Collections", category.getName()), lore);
            }, (_, c) -> {
                if (category == CollectionCategories.BOSS.getCategory()) {
                    c.push(new GUIKuudraCollection());
                    return;
                }
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                ArrayList<String> display = new ArrayList<>();
                player.getCollection().getDisplay(display, category);

                player.openView(new GUICollectionCategory(category, display));
            });
        }
    }
}
