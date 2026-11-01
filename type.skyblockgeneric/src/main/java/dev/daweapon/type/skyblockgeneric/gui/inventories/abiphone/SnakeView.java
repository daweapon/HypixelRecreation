package dev.daweapon.type.skyblockgeneric.gui.inventories.abiphone;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneItemSettings;
import dev.daweapon.type.skyblockgeneric.abiphone.SnakeGame;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.Random;

public final class SnakeView implements View<SnakeView.State> {
    private static final int WIDTH = 6;
    private static final int HEIGHT = 6;
    private static final int TICKS_PER_STEP = 6;

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText((state, ctx) -> Text.literal("Snake"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(State state, ViewContext ctx) {
        ViewSession<State> session = ctx.session(State.class);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        boolean[] waited = {false};
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (session.isClosed()) return TaskSchedule.stop();
            if (!waited[0]) {
                waited[0] = true;
                return TaskSchedule.tick(TICKS_PER_STEP * 3);
            }
            SnakeGame game = state.game();
            if (!game.step()) {
                int score = game.score();
                AbiphoneItemSettings.updateGameStats(player, state.abiphone(), stats -> stats.withSnakeScore(score));
                session.refresh();
                return TaskSchedule.stop();
            }
            session.refresh();
            return TaskSchedule.tick(TICKS_PER_STEP);
        });
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        SnakeGame game = state.game();
        for (int row = 0; row < HEIGHT; row++) {
            for (int column = 0; column < WIDTH; column++) {
                int cell = row * WIDTH + column;
                ItemStack.Builder icon = cell == game.head() ? ItemStack.builder(game.isOver() ? Material.RED_CONCRETE : Material.LIME_CONCRETE)
                        : game.occupies(cell) ? ItemStack.builder(Material.GREEN_CONCRETE)
                        : cell == game.apple() ? ItemStack.builder(Material.APPLE)
                        : ItemStacks.filler(Material.GRAY_STAINED_GLASS_PANE);
                ItemStack.Builder shown = ItemStacks.name(icon, "<f>{}", cell == game.apple() ? "Apple" : " ");
                layout.slot(row * 9 + column, (s, c) -> shown);
            }
        }
        arrow(layout, 16, "Up", SnakeGame.Direction.UP, state);
        arrow(layout, 24, "Left", SnakeGame.Direction.LEFT, state);
        arrow(layout, 26, "Right", SnakeGame.Direction.RIGHT, state);
        arrow(layout, 34, "Down", SnakeGame.Direction.DOWN, state);

        layout.slot(52, (s, c) -> ItemStacks.lore(ItemStacks.name(ItemStack.builder(Material.APPLE).amount(Math.max(1, game.score())), "<c>Apples <a>{}", game.score()),
                List.of(Text.of("<7>Top score: <a>{}", Math.max(game.score(), AbiphoneItemSettings.gameStats(state.abiphone()).snakeBest())))));
        if (game.isOver()) {
            layout.slot(43, (s, c) -> ItemStacks.lore(ItemStacks.name(ItemStack.builder(Material.EMERALD), "<a>Play Again"),
                    List.of(Text.of("<7>Game over with <a>{} <7>apples.", game.score()), Text.empty(), Text.of("<e>Click to play again!"))),
                    (click, viewCtx) -> viewCtx.replace(new SnakeView(), new State(state.abiphone())));
        }
        if (!Components.back(layout, 48, ctx)) Components.close(layout, 48);
        Components.close(layout, 49);
    }

    private void arrow(ViewLayout<State> layout, int slot, String name, SnakeGame.Direction direction, State state) {
        layout.slot(slot, (s, c) -> ItemStacks.name(ItemStack.builder(Material.ARROW), "<a>{}", name),
                (click, viewCtx) -> state.game().turn(direction));
    }

    public record State(SkyBlockItem abiphone, SnakeGame game) {
        public State(SkyBlockItem abiphone) {
            this(abiphone, new SnakeGame(WIDTH, HEIGHT, new Random()));
        }
    }
}
