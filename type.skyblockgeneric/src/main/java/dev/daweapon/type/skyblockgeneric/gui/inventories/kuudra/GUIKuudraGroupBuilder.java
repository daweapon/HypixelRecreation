package dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.partyfinder.KuudraPartyFinder;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.partyfinder.KuudraPartyFinderActions;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
        layout.slot(13, (s, _) -> ItemStacks.item(Material.PAPER, """
                <a>Set Group Note
                <7>Set a note to let everyone know
                <7>what your group plans to do!

                <a>Current Note:
                <f>%s

                <e>Click to change!""".formatted(s.note().isEmpty() ? "None!" : s.note())), (click, c) -> {
            State current = click.state();
            ask(c.player(), "Group note", input -> new State(current.tier(), KuudraPartyFinder.cleanNote(input), current.minimumCombat()));
        });
        layout.slot(15, (s, _) -> ItemStacks.item(Material.IRON_SWORD, """
                <a>Set Combat Level Required
                <7>Add a Combat level requirement to
                <7>the group so only players that
                <7>have the desired level will be able
                <7>to join.

                <a>Current Level Required:
                <f>%s

                <e>Click to change!""".formatted(s.minimumCombat() <= 0 ? "None!" : String.valueOf(s.minimumCombat()))), (click, c) -> {
            State current = click.state();
            ask(c.player(), "Combat level", input -> new State(current.tier(), current.note(), parseLevel(input, current.minimumCombat())));
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

    private static void ask(dev.daweapon.type.generic.user.HypixelPlayer player, String prompt,
                            java.util.function.Function<String, State> next) {
        player.closeInventory();
        new HypixelSignGUI(player).open(new String[]{prompt, ""}).thenAccept(input -> MinecraftServer.getSchedulerManager()
                .scheduleNextTick(() -> {
                    player.openView(new GUIKuudraPartyFinder());
                    player.openView(new GUIKuudraGroupBuilder(), next.apply(input));
                }));
    }
}
