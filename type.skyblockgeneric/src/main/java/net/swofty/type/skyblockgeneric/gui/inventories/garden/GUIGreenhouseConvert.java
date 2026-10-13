package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIGreenhouseConvert extends StatelessView {
    private final int plot;

    public GUIGreenhouseConvert(int plot) {
        this.plot = plot;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Confirm"), InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, (_, _) -> ItemStacks.item(Material.GREEN_TERRACOTTA, Text.of("<a><l>Convert Plot - " + plot), List.of(
                        Text.of("<c>This will turn the plot into a Greenhouse"),
                        Text.of("<c>and replace everything on it."),
                        Text.of("<c><l>This cannot be undone!"),
                        Text.empty(),
                        Text.of("<e>Click to convert!"))),
                (_, c) -> {
                    c.player().closeInventory();
                    GreenhouseService.convert((SkyBlockPlayer) c.player(), plot);
                });
        layout.slot(15, (_, _) -> ItemStacks.item(Material.RED_TERRACOTTA, Text.of("<c>Cancel"), List.of()),
                (_, c) -> c.player().closeInventory());
    }
}
