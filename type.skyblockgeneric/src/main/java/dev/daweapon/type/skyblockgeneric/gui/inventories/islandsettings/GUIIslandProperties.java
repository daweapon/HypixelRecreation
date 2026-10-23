package dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import dev.daweapon.type.skyblockgeneric.user.island.weather.IslandWeatherSettings;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

public class GUIIslandProperties extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Island Properties", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, (s, c) -> {
            SkyBlockIsland island = ((SkyBlockPlayer) c.player()).getSkyBlockIsland();
            if (IslandWeatherSettings.unlocked(island)) {
                return ItemStacks.item(Material.SUNFLOWER, "<a>Island Weather\n<7>Change the weather on your island.\n\n<7>Currently: <e>{}\n\n<e>Click to change!",
                        IslandWeatherSettings.current(island).label());
            }
            return ItemStacks.item(Material.LAPIS_BLOCK, "<c>Island Weather\n<7>Control the weather on your island.\n\n<8>Locked: needs a <b>Weather Node<8>.\n\n<e>Click with a Weather Node\n<e>in your inventory to unlock!");
        }, (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            SkyBlockIsland island = player.getSkyBlockIsland();
            if (!IslandWeatherSettings.unlocked(island) && !IslandWeatherSettings.consumeNode(player, island)) return;
            c.push(new GUIIslandWeather());
        });

        layout.slot(15, (s, c) -> ItemStacks.item(Material.GRASS_BLOCK, "<a>Change Island Biome\n<7>Change the biome of your whole\n<7>Private Island.\n\n<e>Click to choose!"),
                (click, c) -> c.push(new GUIIslandBiome()));
        Components.backOrClose(layout, 31, ctx);
    }
}
