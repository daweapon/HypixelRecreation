package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryData;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// GUI listing bestiary entries for mobs within one region category
public class GUIBestiaryIsland extends StatelessView {

    private static final int[] DISPLAY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43,
    };

    private final BestiaryCategories category;

    public GUIBestiaryIsland(BestiaryCategories category) {
        this.category = category;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("{} ➡ {}", category.getParentTitle(), Text.of(category.getDisplayName()).plain()), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        BestiaryData bestiaryData = new BestiaryData();

        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<Text> lore = new ArrayList<>(category.getDescription().stream().map(Text::of).toList());
            lore.add(Text.empty());
            lore.addAll(BestiaryFamilyProgress.of(player.getBestiaryData(), category.getEntries()).lore());
            return ItemStacks.of(category.getIcon(), 1, Text.of(category.getDisplayName()), lore);
        });

        // Display all mobs
        BestiaryEntry[] bestiaryEntries = category.getEntries();
        for (int i = 0; i < DISPLAY_SLOTS.length && i < bestiaryEntries.length; i++) {
            BestiaryEntry bestiaryEntry = bestiaryEntries[i];
            int slot = DISPLAY_SLOTS[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                int kills = player.getBestiaryData().getAmount(bestiaryEntry.getMobs());

                if (kills > 0) {
                    BestiaryMob mob = bestiaryEntry.getMobs().getFirst();
                    int tier = bestiaryData.getCurrentBestiaryTier(mob, kills);
                    ArrayList<String> rendered = new ArrayList<>();
                    GUIMaterial guiMaterial = bestiaryEntry.getGuiMaterial();

                    player.getBestiaryData().getMobDisplay(rendered, kills, mob, bestiaryEntry);

                    List<Text> lore = new ArrayList<>(rendered.stream().map(Text::parse).toList());
                    lore.add(Text.of("<e>Click to view!"));

                    return ItemStacks.of(guiMaterial, 1,
                            Text.of("<a>{} {:roman}", Text.parse(bestiaryEntry.getName()), tier), lore);
                } else {
                    return ItemStacks.item(Material.GRAY_DYE, 1, """
                            <c>{}
                            <7>Kill a mob belonging to this Family to
                            <7>unlock it in your Bestiary!""", Text.parse(bestiaryEntry.getName()).plain());
                }
            }, (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                int kills = player.getBestiaryData().getAmount(bestiaryEntry.getMobs());
                if (kills > 0) {
                    c.push(new GUIBestiaryMob(category, bestiaryEntry));
                }
            });
        }
    }
}
