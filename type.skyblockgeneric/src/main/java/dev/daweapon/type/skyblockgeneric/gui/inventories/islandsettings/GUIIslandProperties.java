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
        layout.slot(14, (s, c) -> {
            SkyBlockIsland island = ((SkyBlockPlayer) c.player()).getSkyBlockIsland();
            if (IslandWeatherSettings.unlocked(island)) {
                return ItemStacks.item(Material.SUNFLOWER, "<a>Weather Node\n<7>Change the <a>weather <7>on your island!\n\n<7>Currently: <e>{}\n\n<e>Click to change!",
                        IslandWeatherSettings.current(island).label());
            }
            return ItemStacks.item(Material.LAPIS_BLOCK, "<a>Weather Node\n<7>Change the <a>weather <7>on your island!\n\n<c>Click on a Weather Node in your\n<c>inventory to unlock!");
        }, (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            SkyBlockIsland island = player.getSkyBlockIsland();
            if (!IslandWeatherSettings.unlocked(island) && !IslandWeatherSettings.consumeNode(player, island)) return;
            c.push(new GUIIslandWeather());
        });

        layout.slot(16, (s, c) -> ItemStacks.item(Material.GRASS_BLOCK, "<a>Change Island Biome\n<7>Change the biome of your entire\n<7>island!\n\n<e>Click to modify!"),
                (click, c) -> c.push(new GUIIslandBiome()));
        Components.back(layout, 30, ctx);
        Components.close(layout, 31);
    }
}
