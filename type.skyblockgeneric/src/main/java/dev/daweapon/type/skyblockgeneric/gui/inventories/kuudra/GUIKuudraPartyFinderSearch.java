package dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra;

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
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.partyfinder.KuudraPartyFinder;

import java.util.UUID;

/** The Kuudra Party Finder's search settings (wiki Party Finder): tier, search text, Combat level range and sort. */
public class GUIKuudraPartyFinderSearch extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Search Settings", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        UUID uuid = ctx.player().getUuid();
        layout.slot(10, (_, _) -> {
            KuudraTier tier = GUIKuudraPartyFinder.search(uuid).key();
            return ItemStacks.head(tier == null ? KuudraTier.BASIC.getKeyTexture() : tier.getKeyTexture(), """
                    <a>Tier
                    <7>Only show groups for this tier.

                    <7>Currently: <b>%s

                    <e>Left-Click to cycle!
                    <e>Right-Click to cycle back!""".formatted(tier == null ? "Any" : tier.getDisplayName() + " Tier"));
        }, (click, c) -> {
            PartyFinderSearch<KuudraTier> search = GUIKuudraPartyFinder.search(uuid);
            GUIKuudraPartyFinder.setSearch(uuid, search.withKey(cycle(search.key(), click.click() instanceof Click.Right)));
            c.session(DefaultState.class).refresh();
        });
        layout.slot(12, (_, _) -> {
            String text = GUIKuudraPartyFinder.search(uuid).text();
            return ItemStacks.item(Material.OAK_SIGN, """
                    <a>Search Text
                    <7>Only show groups whose note or
                    <7>leader contains this text.

                    <7>Currently: <f>%s

                    <e>Click to edit!""".formatted(text.isEmpty() ? "None!" : "\"" + text + "\""));
        }, (_, c) -> ask(c.player(), "Search text", input ->
                GUIKuudraPartyFinder.setSearch(uuid, GUIKuudraPartyFinder.search(uuid).withText(KuudraPartyFinder.cleanNote(input)))));
        layout.slot(14, (_, _) -> {
            PartyFinderSearch<KuudraTier> search = GUIKuudraPartyFinder.search(uuid);
            return ItemStacks.item(Material.IRON_SWORD, """
                    <a>Combat Level
                    <7>Only show groups whose Combat Level
                    <7>requirement is in this range.

                    <7>Currently: <f>%d-%d

                    <e>Click to edit!""".formatted(search.minimumLevel(), search.maximumLevel()));
        }, (_, c) -> ask(c.player(), "Combat min-max", input -> {
            int[] range = parseRange(input);
            if (range != null) GUIKuudraPartyFinder.setSearch(uuid, GUIKuudraPartyFinder.search(uuid).withLevels(range[0], range[1]));
        }));
        layout.slot(16, (_, _) -> ItemStacks.item(Material.HOPPER, """
                <a>Sort
                <7>Change the order groups are listed in.

                <7>Currently: <b>%s

                <e>Click to cycle!""".formatted(GUIKuudraPartyFinder.search(uuid).sort().displayName())), (_, c) -> {
            PartyFinderSearch<KuudraTier> search = GUIKuudraPartyFinder.search(uuid);
            GUIKuudraPartyFinder.setSearch(uuid, search.withSort(search.sort().next()));
            c.session(DefaultState.class).refresh();
        });
        Components.back(layout, 31, ctx);
    }

    static KuudraTier cycle(KuudraTier current, boolean backwards) {
        KuudraTier[] tiers = KuudraTier.values();
        int index = current == null ? tiers.length : current.ordinal();
        int next = Math.floorMod(index + (backwards ? -1 : 1), tiers.length + 1);
        return next == tiers.length ? null : tiers[next];
    }

    /** "20-45", "20" (from 20 up) or "" (everything), clamped to Combat 0-60. */
    static int[] parseRange(String input) {
        String trimmed = input == null ? "" : input.trim();
        try {
            if (trimmed.isEmpty()) return new int[]{0, KuudraPartyFinder.MAX_COMBAT_LEVEL};
            String[] parts = trimmed.split("-", 2);
            int low = KuudraPartyFinder.clampLevel(Integer.parseInt(parts[0].trim()));
            int high = parts.length > 1 ? KuudraPartyFinder.clampLevel(Integer.parseInt(parts[1].trim())) : KuudraPartyFinder.MAX_COMBAT_LEVEL;
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
                    player.openView(new GUIKuudraPartyFinder());
                    player.openView(new GUIKuudraPartyFinderSearch());
                }));
    }
}
