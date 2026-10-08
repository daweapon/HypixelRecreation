package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.fishing.bestiary.FishingBestiaryFamily;
import net.swofty.type.skyblockgeneric.fishing.bestiary.FishingBestiarySection;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIBestiaryFishing extends StatelessView {
    private static final List<String> DESCRIPTION = List.of(
            "<7>View all of the <3>Sea Creatures <7>that",
            "<7>you've killed while fishing.");

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Bestiary ➡ Fishing", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<Text> lore = new ArrayList<>(DESCRIPTION.stream().map(Text::of).toList());
            lore.add(Text.empty());
            lore.addAll(BestiaryFamilyProgress.of(player.getBestiaryData(), Arrays.asList(FishingBestiaryFamily.values())).lore());
            return ItemStacks.of(new GUIMaterial(Material.FISHING_ROD), 1, Text.of("<3>Fishing"), lore);
        });

        for (FishingBestiarySection section : FishingBestiarySection.values()) {
            BestiaryCategories category = BestiaryCategories.of(section);
            layout.slot(section.getSlot(), (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                List<Text> lore = new ArrayList<>(section.getDescription().stream().map(Text::of).toList());
                lore.add(Text.empty());
                lore.addAll(BestiaryFamilyProgress.of(player.getBestiaryData(), category.getEntries()).lore());
                lore.add(Text.empty());
                lore.add(Text.of("<e>Click to view!"));
                return ItemStacks.of(section.getIcon(), 1, Text.of(section.getDisplayName()), lore);
            }, (click, c) -> c.push(new GUIBestiaryIsland(category)));
        }
    }
}
