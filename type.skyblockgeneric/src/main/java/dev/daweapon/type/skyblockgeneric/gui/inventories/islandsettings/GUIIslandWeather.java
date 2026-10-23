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
import dev.daweapon.type.skyblockgeneric.user.island.weather.IslandWeather;
import dev.daweapon.type.skyblockgeneric.user.island.weather.IslandWeatherSettings;
import net.minestom.server.inventory.InventoryType;

public class GUIIslandWeather extends StatelessView {
    private static final int[] SLOTS = {11, 13, 15};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Island Weather", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        IslandWeather[] options = IslandWeather.values();
        for (int i = 0; i < options.length; i++) {
            IslandWeather option = options[i];
            layout.slot(SLOTS[i], (s, c) -> {
                SkyBlockIsland island = ((SkyBlockPlayer) c.player()).getSkyBlockIsland();
                boolean selected = IslandWeatherSettings.current(island) == option;
                var stack = ItemStacks.item(option.icon(), "<a>" + option.label() + "\n<7>Set the island weather to <b>" + option.label() + "<7>.\n\n"
                        + (selected ? "<a>Currently selected" : "<e>Click to select!"));
                return selected ? ItemStacks.enchanted(stack) : stack;
            }, (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                SkyBlockIsland island = player.getSkyBlockIsland();
                if (player.getInstance() != island.getIslandInstance()) {
                    player.sendMessage("<c>You can only change the weather while on your own island!");
                    return;
                }
                IslandWeatherSettings.set(player, island, option);
                c.session(DefaultState.class).refresh();
            });
        }
        Components.backOrClose(layout, 31, ctx);
    }
}
