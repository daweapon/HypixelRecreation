package dev.daweapon.type.skyblockgeneric.dungeons.gui;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import java.util.List;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsAvailability;
import dev.daweapon.type.skyblockgeneric.dungeons.partyfinder.CatacombsPartyFinder;
import dev.daweapon.type.skyblockgeneric.dungeons.partyfinder.CatacombsPartyFinderActions;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Start a New Queue (wiki Party Finder group building): floor, note and minimum Catacombs level, then list the party. */
public class GUICatacombsGroupBuilder implements StatefulView<GUICatacombsGroupBuilder.State> {
    public record State(CatacombsFloor floor, String note, int minimumLevel) {
        public static State initial() {
            return new State(CatacombsFloor.ENTRANCE, "", 0);
        }
    }

    @Override
    public State initialState() {
        return State.initial();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Group Builder", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, ItemStacks.item(Material.SKELETON_SKULL, """
                <a>Select Dungeon Type
                <7>Allows you to change your
                <7>selected dungeon type.

                <a>Currently Selected: <b>The Catacombs

                <e>Click to change!"""));
        layout.slot(12, (s, _) -> ItemStacks.item(CatacombsFloorInfo.material(s.floor()), """
                <a>Select Floor
                <7>Allows you to change your
                <7>selected floor number.

                <a>Currently Selected: <b>%s

                <e>Left-Click to change!
                <e>Right-Click to change back!""".formatted(s.floor().displayName())), (click, c) -> {
            int step = click.click() instanceof Click.Right ? -1 : 1;
            List<CatacombsFloor> floors = CatacombsAvailability.PLAYABLE_FLOORS.stream().sorted().toList();
            c.session(State.class).update(s -> new State(floors.get(Math.floorMod(floors.indexOf(s.floor()) + step, floors.size())),
                    s.note(), s.minimumLevel()));
        });
        layout.slot(13, (s, _) -> ItemStacks.item(Material.PAPER, """
                <a>Set Group Note
                <7>Set a note to let everyone know
                <7>what your group plans to do!

                <a>Current Note:
                <f>%s

                <e>Click to change!""".formatted(s.note().isEmpty() ? "None!" : s.note())), (click, c) -> {
            State current = click.state();
            ask(c.player(), "Group note", input -> new State(current.floor(), CatacombsPartyFinder.cleanNote(input), current.minimumLevel()));
        });
        layout.slot(14, ItemStacks.item(Material.NETHER_STAR, """
                <a>Set Class Level Required
                <7>Add a class level requirement to
                <7>the group so only players that
                <7>have a class above the desired
                <7>level will be able to join.

                <a>Current Level Required:
                <f>None!

                <e>Click to change!"""));
        layout.slot(15, (s, _) -> ItemStacks.item(Material.IRON_SWORD, """
                <a>Set Dungeon Level Required
                <7>Add a dungeon level requirement
                <7>to the group so only players
                <7>that have the desired dungeon
                <7>level will be able to join.

                <a>Current Level Required:
                <f>%s

                <e>Click to change!""".formatted(s.minimumLevel() == 0 ? "None!" : String.valueOf(s.minimumLevel()))), (click, c) -> {
            State current = click.state();
            ask(c.player(), "Catacombs level", input -> new State(current.floor(), current.note(), parseLevel(input, current.minimumLevel())));
        });
        Components.back(layout, 31, ctx);
        layout.slot(32, ItemStacks.item(Material.EMERALD_BLOCK, """
                <a>Confirm Group
                <7>Open up your group so other
                <7>players can start joining!

                <e>Click to confirm!"""), (click, c) -> {
            State current = click.state();
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            player.closeInventory();
            CatacombsPartyFinderActions.list(player, current.floor(), current.note(), current.minimumLevel(),
                    () -> GUICatacombsPartyFinder.open(player));
        });
    }

    static int parseLevel(String input, int fallback) {
        try {
            return CatacombsPartyFinder.clampLevel(Integer.parseInt(input == null ? "" : input.trim()));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static void ask(dev.daweapon.type.generic.user.HypixelPlayer player, String prompt,
                            java.util.function.Function<String, State> next) {
        player.closeInventory();
        new HypixelSignGUI(player).open(new String[]{prompt, ""}).thenAccept(input -> MinecraftServer.getSchedulerManager()
                .scheduleNextTick(() -> {
                    player.openView(new GUICatacombsPartyFinder());
                    player.openView(new GUICatacombsGroupBuilder(), next.apply(input));
                }));
    }
}
