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
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseData;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUICarpenter extends StatelessView {
    private final int plot;

    public GUICarpenter(int plot) {
        this.plot = plot;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Carpenter"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        layout.slot(10, (_, _) -> ItemStacks.item(Material.OAK_SAPLING, Text.of("<a>Greenhouse Upgrades"), List.of(
                        Text.of("<7>Upgrade your Greenhouse Growth Speed,"),
                        Text.of("<7>Plant Yield and Plot Limit."),
                        Text.empty(),
                        Text.of("<e>Click to view!"))),
                (_, c) -> c.push(new GUIGreenhouseUpgrades()));
        layout.slot(12, (_, c) -> slots((SkyBlockPlayer) c.player()));
        layout.slot(14, (_, _) -> ItemStacks.item(Material.REDSTONE_TORCH, Text.of("<a>Mutation Info"), List.of(
                Text.of("<7>Mutations have a chance to spread from"),
                Text.of("<7>growing crops to nearby empty plots within"),
                Text.of("<7>the Greenhouse. Each mutation requires a"),
                Text.of("<7>specific combination of crops to spread."),
                Text.empty(),
                Text.of("<7>For detailed requirements and spread"),
                Text.of("<7>conditions, check out your unlocked"),
                Text.of("<7>mutations!"))));
        layout.slot(16, (_, _) -> ItemStacks.item(Material.WHEAT, Text.of("<a>Crop Guide"), List.of(
                        Text.of("<7>See how every base crop grows, what it"),
                        Text.of("<7>yields and which effects it gives to the"),
                        Text.of("<7>crops next to it."),
                        Text.empty(),
                        Text.of("<e>Click to view!"))),
                (_, c) -> c.push(new GUICropGuide()));
    }

    private net.minestom.server.item.ItemStack.Builder slots(SkyBlockPlayer player) {
        GreenhouseData.Bed bed = GreenhouseService.data(player).bed(plot);
        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>Sometimes when you break a crop or"),
                Text.of("<7>mutation, it has a chance to drop an"),
                Text.of("<5>Ethereal Vine<7>, which can be used to"),
                Text.of("<7>unlock additional plant slots in your"),
                Text.of("<7>Greenhouse."),
                Text.empty(),
                Text.of("<7>Your Slots: <a>{}<7>/<a>{}", bed == null ? 0 : bed.unlockedCount(), GreenhouseData.SLOTS)));
        if (bed != null) {
            for (int row = 0; row < GreenhouseData.SIZE; row++) {
                StringBuilder line = new StringBuilder();
                for (int column = 0; column < GreenhouseData.SIZE; column++) {
                    line.append(bed.getUnlocked()[GreenhouseData.slot(column, row)] ? "<a>■" : "<c>■");
                }
                lore.add(Text.of(line.toString()));
            }
        }
        return ItemStacks.item(Material.DIRT, Text.of("<a>Unlocking Plant Slots"), lore);
    }
}
