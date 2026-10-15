package dev.daweapon.type.skyblockgeneric.dungeons.gui;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.partyfinder.PartyFinderGroup;
import dev.daweapon.dungeons.partyfinder.PartyFinderMember;
import dev.daweapon.dungeons.partyfinder.PartyFinderSearch;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsRequirements;
import dev.daweapon.type.skyblockgeneric.dungeons.partyfinder.CatacombsPartyFinder;
import dev.daweapon.type.skyblockgeneric.dungeons.partyfinder.CatacombsPartyFinderActions;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Catacombs Party Finder (laid out like the Kuudra one): open groups in rows 2-4, then Start a New Queue, Refresh, Go Back,
 * Close and Search Settings. Groups live in Redis, so every server lists the same ones.
 */
public class GUICatacombsPartyFinder implements StatefulView<GUICatacombsPartyFinder.State> {
    static final int[] GROUP_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private static final Map<UUID, PartyFinderSearch<CatacombsFloor>> SEARCHES = new ConcurrentHashMap<>();

    public record State(List<PartyFinderGroup<CatacombsFloor, Void>> groups, @Nullable PartyFinderGroup<CatacombsFloor, Void> own,
                        int page) {
        State withPage(int next) {
            return new State(groups, own, next);
        }

        int pages() {
            return Math.max(1, (groups.size() + GROUP_SLOTS.length - 1) / GROUP_SLOTS.length);
        }
    }

    public static PartyFinderSearch<CatacombsFloor> search(UUID player) {
        return SEARCHES.getOrDefault(player, PartyFinderSearch.any(CatacombsPartyFinder.MAX_CATACOMBS_LEVEL));
    }

    public static void setSearch(UUID player, PartyFinderSearch<CatacombsFloor> search) {
        SEARCHES.put(player, search);
    }

    public static void resetSearch(UUID player) {
        SEARCHES.remove(player);
    }

    public static void open(SkyBlockPlayer player) {
        if (CatacombsPartyFinderActions.blocked(player)) return;
        player.openView(new GUICatacombsPartyFinder());
    }

    /** The board is read off the tick thread every time the menu opens or is returned to. */
    @Override
    public void onOpen(State state, ViewContext ctx) {
        refresh((SkyBlockPlayer) ctx.player(), ctx);
    }

    static void load(SkyBlockPlayer player, java.util.function.Consumer<State> then) {
        Thread.startVirtualThread(() -> {
            try {
                CatacombsPartyFinder.groupOf(player.getUuid()).ifPresent(CatacombsPartyFinderActions::sync);
                List<PartyFinderGroup<CatacombsFloor, Void>> groups = CatacombsPartyFinder.search(search(player.getUuid()));
                PartyFinderGroup<CatacombsFloor, Void> own = CatacombsPartyFinder.groupOf(player.getUuid()).orElse(null);
                State state = new State(groups, own, 0);
                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    if (player.isOnline()) then.accept(state);
                });
            } catch (Exception exception) {
                Logger.error(exception, "Couldn't read the Catacombs Party Finder");
                player.sendMessage("<c>The Party Finder is unavailable right now. Please try again later.");
            }
        });
    }

    @Override
    public State initialState() {
        return new State(List.of(), null, 0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Party Finder", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
        int first = state.page() * GROUP_SLOTS.length;
        for (int i = 0; i < GROUP_SLOTS.length && first + i < state.groups().size(); i++) {
            PartyFinderGroup<CatacombsFloor, Void> group = state.groups().get(first + i);
            layout.slot(GROUP_SLOTS[i], (_, _) -> groupItem(viewer, group), (_, c) -> {
                c.player().closeInventory();
                CatacombsPartyFinderActions.join((SkyBlockPlayer) c.player(), group.id());
            });
        }

        if (state.own() == null) {
            layout.slot(45, ItemStacks.item(Material.REDSTONE_BLOCK, """
                    <a>Start a New Queue
                    <7>Queue up with other players to
                    <7>enter The Catacombs.

                    <e>Click to queue!"""), (_, c) -> c.push(new GUICatacombsGroupBuilder(), GUICatacombsGroupBuilder.State.initial()));
        } else {
            boolean leading = state.own().leaderId().equals(viewer.getUuid());
            layout.slot(45, ItemStacks.item(Material.RED_TERRACOTTA, leading ? """
                    <c>De-list Group
                    <7>Take your group out of the
                    <7>Party Finder.

                    <e>Click to de-list!""" : """
                    <c>Leave Group
                    <7>Leave this Party Finder group.

                    <e>Click to leave!"""), (_, c) -> CatacombsPartyFinderActions.leave((SkyBlockPlayer) c.player(),
                    () -> refresh((SkyBlockPlayer) c.player(), ctx)));
        }
        layout.slot(46, ItemStacks.item(Material.EMERALD_BLOCK, """
                <a>Refresh
                <7>Refresh the list of active groups
                <7>matching your search.

                <e>Click to refresh!"""), (_, c) -> refresh((SkyBlockPlayer) c.player(), ctx));
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        layout.slot(50, (_, _) -> searchItem(search(viewer.getUuid())), (click, c) -> {
            if (click.click() instanceof Click.Right) {
                resetSearch(c.player().getUuid());
                refresh((SkyBlockPlayer) c.player(), ctx);
                return;
            }
            c.push(new GUICatacombsPartyFinderSearch());
        });
        if (state.page() > 0) {
            layout.slot(52, ItemStacks.item(Material.ARROW, "<a>Previous Page\n<e>Page " + state.page()),
                    (_, c) -> c.session(State.class).update(s -> s.withPage(s.page() - 1)));
        }
        if (state.page() + 1 < state.pages()) {
            layout.slot(53, ItemStacks.item(Material.ARROW, "<a>Next Page\n<e>Page " + (state.page() + 2)),
                    (_, c) -> c.session(State.class).update(s -> s.withPage(s.page() + 1)));
        }
    }

    private static void refresh(SkyBlockPlayer player, ViewContext ctx) {
        load(player, state -> {
            if (!ctx.session(State.class).isClosed()) ctx.session(State.class).setState(state);
        });
    }

    static ItemStack.Builder groupItem(SkyBlockPlayer viewer, PartyFinderGroup<CatacombsFloor, Void> group) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Floor: <b>{}", group.key().displayName()));
        if (!group.note().isEmpty()) lore.add(Text.of("<7>Note: <f>{}", group.note()));
        if (group.minimumLevel() > 0) lore.add(Text.of("<7>Catacombs Level Required: <3>{}", group.minimumLevel()));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Members: "));
        for (PartyFinderMember<Void> member : group.members()) {
            lore.add(Text.of(" <b>{}<b> (<e>{}<b>)", member.name(), member.level()));
        }
        for (int i = group.size(); i < group.capacity(); i++) lore.add(Text.of("<8> Empty"));
        lore.add(Text.empty());
        if (group.contains(viewer.getUuid())) {
            lore.add(Text.of("<a>You are in this group!"));
        } else if (CatacombsPartyFinderActions.catacombsLevel(viewer) < group.minimumLevel()) {
            lore.add(Text.of("<c>Requires Catacombs Level {}!", group.minimumLevel()));
        } else if (!CatacombsRequirements.meets(viewer, group.key())) {
            lore.add(Text.of("<c>You don't meet the requirements!"));
        } else {
            lore.add(Text.of("<e>Click to join!"));
        }
        Player leader = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(group.leaderId());
        Text name = Text.of("<a>{}'s Party", group.leader().name());
        return leader != null && leader.getSkin() != null
                ? ItemStacks.head(leader.getSkin(), name, lore)
                : ItemStacks.item(CatacombsFloorInfo.material(group.key()), name, lore);
    }

    static ItemStack.Builder searchItem(PartyFinderSearch<CatacombsFloor> search) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Change your search settings to find"));
        lore.add(Text.of("<7>groups suited to you!"));
        lore.add(Text.empty());
        lore.add(Text.of("<a>Floor: <b>{}", search.key() == null ? "Any" : search.key().displayName()));
        lore.add(search.text().isEmpty() ? Text.of("<a>Search Text: <f>None!") : Text.of("<a>Search Text: <f>{}", search.text()));
        lore.add(Text.of("<a>Catacombs Level: <f>{}-{}", search.minimumLevel(), search.maximumLevel()));
        lore.add(Text.of("<a>Sort: <b>{}", search.sort().displayName()));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Left-Click to open!"));
        lore.add(Text.of("<e>Right-Click to reset settings!"));
        return ItemStacks.item(Material.NETHER_STAR, Text.of("<a>Search Settings"), lore);
    }
}
