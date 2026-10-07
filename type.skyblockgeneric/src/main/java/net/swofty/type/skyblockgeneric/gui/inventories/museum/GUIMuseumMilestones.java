package net.swofty.type.skyblockgeneric.gui.inventories.museum;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import net.swofty.type.skyblockgeneric.museum.MuseumMilestoneReward;
import net.swofty.type.skyblockgeneric.museum.MuseumMilestones;
import net.swofty.type.skyblockgeneric.museum.MuseumService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIMuseumMilestones implements StatefulView<GUIMuseumMilestones.State> {
    private static final int HEADER_ROW = 4;
    private static final int HEADER_COLUMN = 1;
    private static final int[][] PATH = snakePath();
    private static final int MAX_OFFSET = PATH[MuseumMilestones.MAX_MILESTONE - 1][1] - 9;
    private static final int FAST_SCROLL = 4;

    public record State(int offset) {
        State scroll(int amount) {
            return new State(Math.clamp(offset + amount, 0, MAX_OFFSET));
        }
    }

    @Override
    public State initialState() {
        return new State(0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Museum Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        if (state.offset() == 0) {
            layout.slot(slot(HEADER_ROW, HEADER_COLUMN), (s, c) -> header(data(c)));
        }

        for (int milestone = 1; milestone <= MuseumMilestones.MAX_MILESTONE; milestone++) {
            int column = PATH[milestone - 1][1] - state.offset();
            if (column < 1 || column > 9) continue;
            int number = milestone;
            layout.slot(slot(PATH[milestone - 1][0], column), (s, c) -> milestone(data(c), number));
        }

        if (state.offset() > 0) {
            layout.slot(45, (s, c) -> scrollButton("<a>Scroll Left"),
                    (click, c) -> c.session(State.class).update(s -> s.scroll(-step(click.click()))));
        }
        if (state.offset() < MAX_OFFSET) {
            layout.slot(53, (s, c) -> scrollButton("<a>Scroll Right"),
                    (click, c) -> c.session(State.class).update(s -> s.scroll(step(click.click()))));
        }
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    private static int step(Click click) {
        return click instanceof Click.Right ? FAST_SCROLL : 1;
    }

    private static int slot(int row, int column) {
        return (row - 1) * 9 + (column - 1);
    }

    // Hypixel lays the milestones out as a snake: right one, up three, right two, down three, right two, ...
    private static int[][] snakePath() {
        int[][] path = new int[MuseumMilestones.MAX_MILESTONE][];
        int row = 4;
        int column = 2;
        path[0] = new int[]{row, column};
        int[][] moves = {{0, 1}, {-1, 0}, {-1, 0}, {-1, 0}, {0, 1}, {0, 1}, {1, 0}, {1, 0}, {1, 0}, {0, 1}};
        for (int index = 1; index < path.length; index++) {
            int[] move = moves[(index - 1) % moves.length];
            row += move[0];
            column += move[1];
            path[index] = new int[]{row, column};
        }
        return path;
    }

    private static DatapointMuseum.MuseumData data(ViewContext ctx) {
        return ((SkyBlockPlayer) ctx.player()).getMuseumData();
    }

    private static ItemStack.Builder scrollButton(String name) {
        return ItemStacks.item(Material.ARROW, 1, Text.of(name), List.of(
                Text.of("<e>Left-click to scroll!"),
                Text.of("<e>Right-click to scroll fast!")));
    }

    private static ItemStack.Builder header(DatapointMuseum.MuseumData data) {
        return ItemStacks.item(Material.GOLD_BLOCK, 1, Text.of("<6>Museum Milestones"), List.of(
                Text.of("<7>Every <b>100 SkyBlock XP <7>obtained from"),
                Text.of("<7>your Museum, <6>Eleanor <7>will reward you."),
                Text.empty(),
                Text.of("<d>Special Items <7>do not reward SkyBlock"),
                Text.of("<7>XP."),
                Text.empty(),
                Text.of("<7>Total XP: <b>{}<3>/<b>{}", StringUtility.commaify(MuseumService.museumXp(data)),
                        StringUtility.commaify(MuseumService.catalogue().totalXp())),
                Text.of("<7>Milestone: <e>{}<6>/<e>{}", MuseumService.milestone(data), MuseumMilestones.MAX_MILESTONE)));
    }

    private static ItemStack.Builder milestone(DatapointMuseum.MuseumData data, int milestone) {
        boolean reached = MuseumService.milestone(data) >= milestone;
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Required XP: <e>{}", StringUtility.commaify(MuseumMilestones.requiredXp(milestone))));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Rewards:"));
        for (MuseumMilestoneReward reward : MuseumMilestoneReward.forMilestone(milestone)) {
            lore.add(Text.of("<8>+" + reward.getDisplay()));
            if (!reward.isAvailable()) lore.add(Text.of("  <8>{}", reward.reason()));
        }
        lore.add(Text.of("<8>+<b>1% Bits Multiplier"));
        lore.add(Text.of("<8>+<6>2% Bank Interest"));
        lore.add(Text.empty());
        if (reached) {
            lore.add(Text.of("<a><l>UNLOCKED"));
        } else {
            int museumXp = MuseumService.museumXp(data);
            lore.add(Text.of("<7>Progress: <e>{}<6>/<e>{}", StringUtility.commaify(museumXp),
                    StringUtility.commaify(MuseumMilestones.requiredXp(milestone))));
        }
        return ItemStacks.item(reached ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                milestone, Text.of("<a>Museum Milestone {}", milestone), lore);
    }
}
