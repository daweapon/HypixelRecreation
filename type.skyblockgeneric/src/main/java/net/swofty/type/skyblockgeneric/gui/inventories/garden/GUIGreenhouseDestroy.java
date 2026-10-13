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
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseLayout;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIGreenhouseDestroy extends StatelessView {
    private final GreenhouseLayout.Spot spot;

    public GUIGreenhouseDestroy(GreenhouseLayout.Spot spot) {
        this.spot = spot;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Confirm"), InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, (_, _) -> ItemStacks.item(Material.GREEN_TERRACOTTA, Text.of("<c><l>WARNING!"), List.of(
                        Text.of("<c>This crop has not fully grown, so"),
                        Text.of("<c>destroying it will yield nothing!"),
                        Text.empty(),
                        Text.of("<e>Click to destroy this crop anyway!"))),
                (_, c) -> {
                    GreenhouseService.destroyUngrown((SkyBlockPlayer) c.player(), spot);
                    c.player().closeInventory();
                });
        layout.slot(15, (_, _) -> ItemStacks.item(Material.RED_TERRACOTTA, Text.of("<c>Cancel"), List.of()),
                (_, c) -> c.player().closeInventory());
    }
}
