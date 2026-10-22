package dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.IslandBiomes;

import java.util.List;

public class GUIIslandBiome extends StatelessView {
    private static final int[] GRID_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Change Island Biome", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        List<IslandBiomes.Choice> choices = IslandBiomes.choices();
        for (int i = 0; i < choices.size() && i < GRID_SLOTS.length; i++) {
            IslandBiomes.Choice choice = choices.get(i);
            layout.slot(GRID_SLOTS[i], (s, c) -> ItemStacks.item(iconFor(choice.name()),
                    "<a>{}\n<7>Change the biome of your entire\n<7>Private Island to <b>{}<7>.\n\n<e>Click to change!", choice.name(), choice.name()),
                    (click, c) -> change((SkyBlockPlayer) c.player(), choice));
        }
        Components.backOrClose(layout, 40, ctx);
    }

    private static void change(SkyBlockPlayer player, IslandBiomes.Choice choice) {
        player.closeInventory();
        var island = player.getSkyBlockIsland();
        if (island == null || island.getIslandInstance() == null || player.getInstance() != island.getIslandInstance()) {
            player.sendMessage("<c>You can only change the biome on your own Private Island!");
            return;
        }
        IslandBiomes.apply(island.getIslandInstance(), choice.key());
        player.sendMessage("<7>Changed the biome of your island to <b>{}<7>!", choice.name());
    }

    private static Material iconFor(String name) {
        return switch (name) {
            case "Birch Forest" -> Material.BIRCH_LEAVES;
            case "Deep Ocean" -> Material.INK_SAC;
            case "Desert" -> Material.SAND;
            case "End" -> Material.END_STONE;
            case "Forest" -> Material.OAK_LEAVES;
            case "Jungle" -> Material.JUNGLE_LEAVES;
            case "Mesa" -> Material.RED_SAND;
            case "Mushroom" -> Material.MYCELIUM;
            case "Nether" -> Material.NETHERRACK;
            case "Roofed Forest" -> Material.DARK_OAK_LEAVES;
            case "Savanna" -> Material.ACACIA_LEAVES;
            case "Taiga" -> Material.SPRUCE_LEAVES;
            default -> Material.GRASS_BLOCK;
        };
    }
}
