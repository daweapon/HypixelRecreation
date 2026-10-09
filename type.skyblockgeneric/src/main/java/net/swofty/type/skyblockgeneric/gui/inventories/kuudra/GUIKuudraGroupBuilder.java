package net.swofty.type.skyblockgeneric.gui.inventories.kuudra;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.partyfinder.KuudraPartyFinder;
import net.swofty.type.skyblockgeneric.crimson.kuudra.partyfinder.KuudraPartyFinderActions;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** Start a New Queue (wiki Party Finder group building): tier, note and minimum Combat level, then list the party. */
public class GUIKuudraGroupBuilder implements StatefulView<GUIKuudraGroupBuilder.State> {
    public record State(KuudraTier tier, String note, int minimumCombat) {
        public static State initial() {
            return new State(KuudraTier.BASIC, "", 0);
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
        layout.slot(11, (s, _) -> ItemStacks.head(s.tier().getKeyTexture(), """
                <a>Select Tier
                <7>The Kuudra tier your group will fight.

                <7>Currently: <e>%s Tier

                <e>Left-Click to cycle!
                <e>Right-Click to cycle back!""".formatted(s.tier().getDisplayName())), (click, c) -> {
            int step = click.click() instanceof Click.Right ? -1 : 1;
            KuudraTier[] tiers = KuudraTier.values();
            c.session(State.class).update(s -> new State(tiers[Math.floorMod(s.tier().ordinal() + step, tiers.length)],
                    s.note(), s.minimumCombat()));
        });
        layout.slot(13, (s, _) -> ItemStacks.item(Material.WRITABLE_BOOK, """
                <a>Group Note
                <7>Tell other players what your group
                <7>is looking for.

                <7>Currently: <f>%s

                <e>Click to edit!""".formatted(s.note().isEmpty() ? "None!" : s.note())), (click, c) -> {
            State current = click.state();
            ask(c.player(), "Group note", input -> new State(current.tier(), KuudraPartyFinder.cleanNote(input), current.minimumCombat()));
        });
        layout.slot(15, (s, _) -> ItemStacks.item(Material.IRON_SWORD, """
                <a>Combat Level Required
                <7>The lowest Combat level a player
                <7>needs to join your group.

                <7>Currently: <3>%d

                <e>Click to edit!""".formatted(s.minimumCombat())), (click, c) -> {
            State current = click.state();
            ask(c.player(), "Combat level", input -> new State(current.tier(), current.note(), parseLevel(input, current.minimumCombat())));
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
            KuudraPartyFinderActions.list(player, current.tier(), current.note(), current.minimumCombat(),
                    () -> GUIKuudraPartyFinder.open(player));
        });
    }

    static int parseLevel(String input, int fallback) {
        try {
            return KuudraPartyFinder.clampLevel(Integer.parseInt(input == null ? "" : input.trim()));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static void ask(net.swofty.type.generic.user.HypixelPlayer player, String prompt,
                            java.util.function.Function<String, State> next) {
        player.closeInventory();
        new HypixelSignGUI(player).open(new String[]{prompt, ""}).thenAccept(input -> MinecraftServer.getSchedulerManager()
                .scheduleNextTick(() -> {
                    player.openView(new GUIKuudraPartyFinder());
                    player.openView(new GUIKuudraGroupBuilder(), next.apply(input));
                }));
    }
}
