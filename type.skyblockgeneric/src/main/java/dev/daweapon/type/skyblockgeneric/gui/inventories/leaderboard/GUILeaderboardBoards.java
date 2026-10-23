package dev.daweapon.type.skyblockgeneric.gui.inventories.leaderboard;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulPaginatedView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.leaderboard.BoardCategory;
import dev.daweapon.type.skyblockgeneric.leaderboard.SkyBlockBoard;
import dev.daweapon.type.skyblockgeneric.leaderboard.SkyBlockBoards;
import dev.daweapon.type.skyblockgeneric.leaderboard.SkyBlockLeaderboards;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GUILeaderboardBoards extends StatefulPaginatedView<SkyBlockBoard, GUILeaderboardBoards.State> {
    private final BoardCategory category;
    private final List<SkyBlockBoard> boards;

    public GUILeaderboardBoards(BoardCategory category, List<SkyBlockBoard> boards) {
        this.category = category;
        this.boards = boards;
    }

    public static void open(SkyBlockPlayer player, BoardCategory category) {
        Thread.startVirtualThread(() -> {
            List<SkyBlockBoard> boards = new ArrayList<>(SkyBlockBoards.fixed().stream().filter(board -> board.category() == category).toList());
            if (category == BoardCategory.COLLECTIONS) {
                for (String id : SkyBlockLeaderboards.collectionBoardIds()) SkyBlockBoards.byId(id).ifPresent(boards::add);
            }
            player.scheduler().scheduleNextTick(() -> player.openView(new GUILeaderboardBoards(category, boards)));
        });
    }

    @Override
    public State initialState() {
        return new State(boards, 0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((_, _) -> category.getDisplayName() + " Leaderboards", InventoryType.CHEST_6_ROW);
    }

    @Override
    protected int[] getPaginatedSlots() {
        return DEFAULT_SLOTS;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    @Override
    protected ItemStack.Builder renderItem(SkyBlockBoard board, int index, HypixelPlayer player) {
        return ItemStacks.item(board.icon(), Text.of(category.getColour() + "{}", board.name()), List.of(
                Text.of("<7>See the top profiles by {}.", board.unit()),
                Text.empty(),
                Text.of("<e>Click to view!")));
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, SkyBlockBoard board, int index) {
        GUILeaderboardView.open((SkyBlockPlayer) click.player(), board);
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, SkyBlockBoard board) {
        return false;
    }

    public record State(List<SkyBlockBoard> items, int page) implements PaginatedState<SkyBlockBoard> {
        @Override
        public PaginatedState<SkyBlockBoard> withPage(int page) {
            return new State(items, page);
        }

        @Override
        public PaginatedState<SkyBlockBoard> withItems(List<SkyBlockBoard> items) {
            return new State(items, page);
        }
    }
}
