package net.swofty.type.skyblockgeneric.gui.inventories.islandsettings;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.island.IslandPrivacy;

// Lets a player pick their island's visitation tier without typing /islandprivacy - same three
// tiers, same underlying datapoint (see IslandPrivacyCommand).
public class GUIIslandSettingsPrivacy extends StatelessView {

    private record Option(int slot, IslandPrivacy tier, Material material, String description) {
    }

    private static final Option[] OPTIONS = new Option[]{
            new Option(11, IslandPrivacy.PUBLIC, Material.LIME_DYE, "<7>Anyone can /visit your island."),
            new Option(13, IslandPrivacy.FRIENDS_ONLY, Material.YELLOW_DYE, "<7>Only friends can /visit your island."),
            new Option(15, IslandPrivacy.PRIVATE, Material.RED_DYE, "<7>Nobody can /visit your island\n<7>unless directly /invite-d."),
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Island Privacy", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        for (Option option : OPTIONS) {
            layout.slot(option.slot(), (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                boolean selected = currentTier(player) == option.tier();
                String status = selected ? "\n\n<a>SELECTED" : "\n\n<7>Click to select!";
                return ItemStacks.item(option.material(), "<a>" + GUIIslandSettings.label(option.tier())
                        + "\n" + option.description() + status);
            }, (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                SkyBlockDataHandler handler = player.getSkyblockDataHandler();
                handler.get(SkyBlockDataHandler.Data.ISLAND_PRIVACY, DatapointString.class).setValue(option.tier().name());
                handler.saveToApi();
                player.sendMessage("<e>Your island is now set to <b>{}<e>!", option.tier().name());
                player.openView(new GUIIslandSettingsPrivacy());
            });
        }

        Components.backOrClose(layout, 22, ctx);
    }

    private static IslandPrivacy currentTier(SkyBlockPlayer player) {
        return IslandPrivacy.fromStored(player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.ISLAND_PRIVACY, DatapointString.class).getValue());
    }
}
