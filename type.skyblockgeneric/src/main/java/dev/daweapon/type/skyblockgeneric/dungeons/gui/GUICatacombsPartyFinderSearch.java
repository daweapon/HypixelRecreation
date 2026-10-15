package dev.daweapon.type.skyblockgeneric.dungeons.gui;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import dev.daweapon.dungeons.partyfinder.PartyFinderSearch;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.skyblockgeneric.dungeons.partyfinder.CatacombsPartyFinder;

import java.util.UUID;

/** The Catacombs Party Finder's search settings (wiki Party Finder): floor, search text, Catacombs level range and sort. */
public class GUICatacombsPartyFinderSearch extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Search Settings", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        UUID uuid = ctx.player().getUuid();
        layout.slot(10, (_, _) -> {
            CatacombsFloor floor = GUICatacombsPartyFinder.search(uuid).key();
            return ItemStacks.item(CatacombsFloorInfo.material(floor), """
                    <a>Floor
                    <7>Only show groups for this floor.

                    <7>Currently: <b>%s

                    <e>Left-Click to cycle!
                    <e>Right-Click to cycle back!""".formatted(floor == null ? "Any" : floor.displayName()));
        }, (click, c) -> {
            PartyFinderSearch<CatacombsFloor> search = GUICatacombsPartyFinder.search(uuid);
            GUICatacombsPartyFinder.setSearch(uuid, search.withKey(cycle(search.key(), click.click() instanceof Click.Right)));
            c.session(DefaultState.class).refresh();
        });
        layout.slot(12, (_, _) -> {
            String text = GUICatacombsPartyFinder.search(uuid).text();
            return ItemStacks.item(Material.OAK_SIGN, """
                    <a>Search Text
                    <7>Only show groups whose note or
                    <7>leader contains this text.

                    <7>Currently: <f>%s

                    <e>Click to edit!""".formatted(text.isEmpty() ? "None!" : "\"" + text + "\""));
        }, (_, c) -> ask(c.player(), "Search text", input ->
                GUICatacombsPartyFinder.setSearch(uuid, GUICatacombsPartyFinder.search(uuid).withText(CatacombsPartyFinder.cleanNote(input)))));
        layout.slot(14, (_, _) -> {
            PartyFinderSearch<CatacombsFloor> search = GUICatacombsPartyFinder.search(uuid);
            return ItemStacks.item(Material.IRON_SWORD, """
                    <a>Catacombs Level
                    <7>Only show groups whose Catacombs Level
                    <7>requirement is in this range.

                    <7>Currently: <f>%d-%d

                    <e>Click to edit!""".formatted(search.minimumLevel(), search.maximumLevel()));
        }, (_, c) -> ask(c.player(), "Level min-max", input -> {
            int[] range = parseRange(input);
            if (range != null) GUICatacombsPartyFinder.setSearch(uuid, GUICatacombsPartyFinder.search(uuid).withLevels(range[0], range[1]));
        }));
        layout.slot(16, (_, _) -> ItemStacks.item(Material.HOPPER, """
                <a>Sort
                <7>Change the order groups are listed in.

                <7>Currently: <b>%s

                <e>Click to cycle!""".formatted(GUICatacombsPartyFinder.search(uuid).sort().displayName())), (_, c) -> {
            PartyFinderSearch<CatacombsFloor> search = GUICatacombsPartyFinder.search(uuid);
            GUICatacombsPartyFinder.setSearch(uuid, search.withSort(search.sort().next()));
            c.session(DefaultState.class).refresh();
        });
        Components.back(layout, 31, ctx);
    }

    static CatacombsFloor cycle(CatacombsFloor current, boolean backwards) {
        CatacombsFloor[] floors = CatacombsFloor.values();
        int index = current == null ? floors.length : current.ordinal();
        int next = Math.floorMod(index + (backwards ? -1 : 1), floors.length + 1);
        return next == floors.length ? null : floors[next];
    }

    /** "20-45", "20" (from 20 up) or "" (everything), clamped to Catacombs 0-50. */
    static int[] parseRange(String input) {
        String trimmed = input == null ? "" : input.trim();
        try {
            if (trimmed.isEmpty()) return new int[]{0, CatacombsPartyFinder.MAX_CATACOMBS_LEVEL};
            String[] parts = trimmed.split("-", 2);
            int low = CatacombsPartyFinder.clampLevel(Integer.parseInt(parts[0].trim()));
            int high = parts.length > 1 ? CatacombsPartyFinder.clampLevel(Integer.parseInt(parts[1].trim())) : CatacombsPartyFinder.MAX_CATACOMBS_LEVEL;
            return new int[]{Math.min(low, high), Math.max(low, high)};
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static void ask(HypixelPlayer player, String prompt, java.util.function.Consumer<String> then) {
        player.closeInventory();
        new HypixelSignGUI(player).open(new String[]{prompt, ""}).thenAccept(input -> MinecraftServer.getSchedulerManager()
                .scheduleNextTick(() -> {
                    then.accept(input);
                    player.openView(new GUICatacombsPartyFinder());
                    player.openView(new GUICatacombsPartyFinderSearch());
                }));
    }
}
