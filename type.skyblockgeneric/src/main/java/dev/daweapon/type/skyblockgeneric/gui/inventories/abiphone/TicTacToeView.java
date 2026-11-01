package dev.daweapon.type.skyblockgeneric.gui.inventories.abiphone;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneItemSettings;
import dev.daweapon.type.skyblockgeneric.abiphone.TicTacToeBoard;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public final class TicTacToeView implements View<TicTacToeView.State> {
    private static final int[] SLOTS = {12, 13, 14, 21, 22, 23, 30, 31, 32};

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText((state, ctx) -> Text.literal("Tic Tac Toe"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        TicTacToeBoard board = state.board();
        for (int i = 0; i < 9; i++) {
            int cell = i;
            char mark = board.cell(i);
            Material material = mark == TicTacToeBoard.PLAYER ? Material.LIME_WOOL : mark == TicTacToeBoard.AI ? Material.RED_WOOL : Material.WHITE_WOOL;
            String label = mark == TicTacToeBoard.PLAYER ? "<a>You" : mark == TicTacToeBoard.AI ? "<c>Abiphone" : "<f>Empty";
            List<Text> cellLore = mark == TicTacToeBoard.PLAYER || mark == TicTacToeBoard.AI ? List.of() : List.of(Text.of("<e>Click to select!"));
            layout.slot(SLOTS[i], (s, c) -> ItemStacks.lore(ItemStacks.name(ItemStack.builder(material), label), cellLore), (click, viewCtx) -> play(state, cell, viewCtx));
        }
        if (board.finished()) {
            layout.slot(40, (s, c) -> ItemStacks.lore(ItemStacks.name(ItemStack.builder(Material.EMERALD), "<a>Play Again"), List.of(
                    Text.of(result(board)), Text.empty(), Text.of("<e>Click to play again!"))),
                    (click, viewCtx) -> viewCtx.replace(new TicTacToeView(), new State(state.abiphone())));
        }
        layout.slot(52, (s, c) -> {
            var stats = AbiphoneItemSettings.gameStats(state.abiphone());
            return ItemStacks.lore(ItemStacks.name(ItemStack.builder(Material.BOOK), "<a>Results"),
                    List.of(Text.of("<7>Wins: <a>{}", stats.wins()), Text.of("<7>Losses: <c>{}", stats.losses()), Text.of("<7>Draws: <e>{}", stats.draws())));
        });
        if (!Components.back(layout, 48, ctx)) Components.close(layout, 48);
        Components.close(layout, 49);
    }

    private static String result(TicTacToeBoard board) {
        return switch (board.winner()) {
            case TicTacToeBoard.PLAYER -> "<a>You won!";
            case TicTacToeBoard.AI -> "<c>The Abiphone won!";
            default -> "<e>It's a draw!";
        };
    }

    private void play(State state, int cell, ViewContext ctx) {
        TicTacToeBoard board = state.board();
        if (!board.play(cell, TicTacToeBoard.PLAYER)) return;
        if (!board.finished()) board.play(board.aiMove(), TicTacToeBoard.AI);
        if (board.finished()) {
            SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
            AbiphoneItemSettings.updateGameStats(player, state.abiphone(), stats -> switch (board.winner()) {
                case TicTacToeBoard.PLAYER -> stats.withWin();
                case TicTacToeBoard.AI -> stats.withLoss();
                default -> stats.withDraw();
            });
        }
        ctx.session(State.class).refresh();
    }

    public record State(SkyBlockItem abiphone, TicTacToeBoard board) {
        public State(SkyBlockItem abiphone) {
            this(abiphone, new TicTacToeBoard());
        }
    }
}
