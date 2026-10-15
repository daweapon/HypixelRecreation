package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseLayout;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.SnakeGame;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Random;

public class GUISnake extends StatelessView {
    private final GreenhouseLayout.Spot spot;
    private SnakeGame game = new SnakeGame(new Random());

    public GUISnake(GreenhouseLayout.Spot spot) {
        this.spot = spot;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("PlantBoy Advance"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        for (int cell = 0; cell < SnakeGame.ROWS * SnakeGame.COLUMNS; cell++) {
            int c = cell;
            layout.slot(cell, (_, _) -> tile(c));
        }
        layout.slot(45, (_, _) -> ItemStacks.item(Material.PAPER, Text.of("<a>Snake"), List.of(
                Text.of("<7>Collect <a>{} <7>apples to win!", SnakeGame.GOAL),
                Text.of("<7>Apples: <a>{}<7>/<a>{}", game.apples(), SnakeGame.GOAL),
                Text.empty(),
                Text.of("<7>Use the arrows to move one tile."),
                Text.of("<7>Don't hit the walls or yourself!"))));
        layout.slot(47, (_, _) -> arrow("<a>Left"), (_, click) -> move(click, SnakeGame.Direction.LEFT));
        layout.slot(48, (_, _) -> arrow("<a>Up"), (_, click) -> move(click, SnakeGame.Direction.UP));
        layout.slot(49, (_, _) -> arrow("<a>Down"), (_, click) -> move(click, SnakeGame.Direction.DOWN));
        layout.slot(50, (_, _) -> arrow("<a>Right"), (_, click) -> move(click, SnakeGame.Direction.RIGHT));
        Components.close(layout, 53);
    }

    private ItemStack.Builder arrow(String name) {
        return ItemStacks.item(Material.ARROW, Text.of(name), List.of(Text.of("<e>Click to move!")));
    }

    private ItemStack.Builder tile(int cell) {
        if (cell == game.head()) return ItemStacks.item(Material.LIME_CONCRETE, Text.of("<a>Snake"), List.of());
        if (game.occupies(cell)) return ItemStacks.item(Material.GREEN_CONCRETE, Text.of("<2>Snake"), List.of());
        if (cell == game.apple()) return ItemStacks.item(Material.APPLE, Text.of("<c>Apple"), List.of());
        return ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE);
    }

    private void move(ViewContext click, SnakeGame.Direction direction) {
        SkyBlockPlayer player = (SkyBlockPlayer) click.player();
        switch (game.move(direction)) {
            case WON -> {
                player.closeInventory();
                player.sendMessage(Text.of("<e>[CROP] <9>PlantBoy Advance<f>: You bested me, you may harvest me now."));
                GreenhouseService.finishHarvest(player, spot);
            }
            case LOST -> {
                player.sendMessage(Text.of("<e>[CROP] <9>PlantBoy Advance<f>: Game over! Try again."));
                game = new SnakeGame(new Random());
                click.session(DefaultState.class).refresh();
            }
            default -> click.session(DefaultState.class).refresh();
        }
    }
}
