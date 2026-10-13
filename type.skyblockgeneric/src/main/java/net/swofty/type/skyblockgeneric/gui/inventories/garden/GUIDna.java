package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.analyzer.AnalyzerService;
import net.swofty.type.skyblockgeneric.garden.analyzer.DnaBoard;
import net.swofty.type.skyblockgeneric.garden.greenhouse.MutationCatalogue;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Random;

public class GUIDna extends StatelessView {
    private static final Material[] STRINGS = {Material.BLUE_TERRACOTTA, Material.RED_TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.GREEN_TERRACOTTA};
    private static final Material[] CONNECTORS = {Material.BLUE_STAINED_GLASS, Material.RED_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.GREEN_STAINED_GLASS};
    private static final String[] NAMES = {"<9>Blue", "<c>Red", "<e>Yellow", "<a>Green"};

    private final MutationCatalogue.Mutation mutation;
    private final DnaBoard board = DnaBoard.generate(new Random());
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private boolean finished;

    public GUIDna(MutationCatalogue.Mutation mutation) {
        this.mutation = mutation;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of(mutation.name() + " DNA"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        for (int row = 0; row < DnaBoard.COLOURS; row++) {
            int r = row;
            layout.slot((row + 1) * 9, (_, _) -> connector(board.left(r)));
            layout.slot((row + 1) * 9 + 8, (_, _) -> connector(board.right(r)));
            for (int col = 0; col < DnaBoard.COLUMNS; col++) {
                int c = col;
                layout.slot((row + 1) * 9 + col + 1, (_, _) -> string(r, c), (_, click) -> select(click, r, c));
            }
        }
        Components.close(layout, 49);
        layout.slot(50, (_, _) -> ItemStacks.item(Material.REDSTONE_TORCH, Text.of("<a>DNA Sequencing"), List.of(
                Text.of("<7>Connect DNA Connectors using DNA Strings"),
                Text.of("<7>to perform DNA Sequencing! You can do this"),
                Text.of("<7>by clicking on a DNA String to select it,"),
                Text.of("<7>then click another DNA String above or"),
                Text.of("<7>below to swap their locations!"))));
        layout.slot(45, (_, c) -> ((SkyBlockPlayer) c.player()).getRank().isStaff()
                ? ItemStacks.item(Material.COMMAND_BLOCK, Text.of("<c>Auto-Complete <8>(Admin)"), List.of(Text.of("<7>Solves the board for you."), Text.of("<e>Click to solve!")))
                : ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE), (_, click) -> {
            if (!((SkyBlockPlayer) click.player()).getRank().isStaff()) return;
            board.solve();
            finish(click);
        });
    }

    private ItemStack.Builder connector(int colour) {
        return ItemStacks.item(CONNECTORS[colour], Text.of(NAMES[colour] + " <f>DNA Connector"), List.of(Text.of("<7>Connect the DNA Strings here!")));
    }

    private ItemStack.Builder string(int row, int col) {
        int colour = board.get(row, col);
        boolean selected = row == selectedRow && col == selectedColumn;
        return ItemStacks.item(STRINGS[colour], Text.of(NAMES[colour] + " <f>DNA String"), List.of(
                Text.of("<7>Connect the DNA Strings with matching"),
                Text.of("<7>color Connectors!"),
                Text.empty(),
                Text.of(selected ? "<e>Click to unselect!" : selectedRow >= 0 && selectedColumn == col && Math.abs(selectedRow - row) == 1 ? "<e>Click to swap!" : "<e>Click to move!")));
    }

    private void select(ViewContext click, int row, int col) {
        if (finished) return;
        if (selectedRow == row && selectedColumn == col) {
            selectedRow = -1;
            selectedColumn = -1;
        } else if (selectedRow >= 0 && selectedColumn == col && board.swap(col, selectedRow, row)) {
            selectedRow = -1;
            selectedColumn = -1;
            if (board.solved()) {
                finish(click);
                return;
            }
        } else {
            selectedRow = row;
            selectedColumn = col;
        }
        click.session(DefaultState.class).refresh();
    }

    private void finish(ViewContext click) {
        finished = true;
        SkyBlockPlayer player = (SkyBlockPlayer) click.player();
        player.closeInventory();
        if (!AnalyzerService.complete(player, mutation)) {
            player.sendMessage(Text.of("<c>You no longer have the Mutation or the Coins to analyze it!"));
        }
    }
}
