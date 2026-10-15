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
        layout.slot(11, (s, _) -> ItemStacks.item(CatacombsFloorInfo.material(s.floor()), """
                <a>Select Floor
                <7>The Catacombs floor your group will run.

                <7>Currently: <e>%s

                <e>Left-Click to cycle!
                <e>Right-Click to cycle back!""".formatted(s.floor().displayName())), (click, c) -> {
            int step = click.click() instanceof Click.Right ? -1 : 1;
            List<CatacombsFloor> floors = CatacombsAvailability.PLAYABLE_FLOORS.stream().sorted().toList();
            c.session(State.class).update(s -> new State(floors.get(Math.floorMod(floors.indexOf(s.floor()) + step, floors.size())),
                    s.note(), s.minimumLevel()));
        });
        layout.slot(13, (s, _) -> ItemStacks.item(Material.WRITABLE_BOOK, """
                <a>Group Note
                <7>Tell other players what your group
                <7>is looking for.

                <7>Currently: <f>%s

                <e>Click to edit!""".formatted(s.note().isEmpty() ? "None!" : s.note())), (click, c) -> {
            State current = click.state();
            ask(c.player(), "Group note", input -> new State(current.floor(), CatacombsPartyFinder.cleanNote(input), current.minimumLevel()));
        });
        layout.slot(15, (s, _) -> ItemStacks.item(Material.IRON_SWORD, """
                <a>Catacombs Level Required
                <7>The lowest Catacombs level a player
                <7>needs to join your group.

                <7>Currently: <3>%d

                <e>Click to edit!""".formatted(s.minimumLevel())), (click, c) -> {
            State current = click.state();
            ask(c.player(), "Catacombs level", input -> new State(current.floor(), current.note(), parseLevel(input, current.minimumLevel())));
        });
        Components.back(layout, 30, ctx);
        layout.slot(31, ItemStacks.item(Material.EMERALD_BLOCK, """
                <a>Confirm Group
                <7>List your party in the Party Finder
                <7>so other players can join it.

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
