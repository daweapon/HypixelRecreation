package dev.daweapon.type.skyblockgeneric.gui.inventories.moonglade;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.BeaconCooldowns;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.BeaconPane;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.BeaconRules;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.BeaconTune;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.MoongladeBeacon;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GUIBeaconTuning implements StatefulView<GUIBeaconTuning.State> {
    public static final int FIG_LOGS_REWARD = 8;
    public static final int MANGROVE_LOGS_REWARD = 8;
    private static final long TICK_MILLIS = 100;
    private static final int INFO_SLOT = 4;
    private static final int TARGET_ROW = 9;
    private static final int PLAYER_ROW = 18;
    private static final int PAUSE_SLOT = 37;
    private static final int COLOR_SLOT = 39;
    private static final int SPEED_SLOT = 41;
    private static final int PITCH_SLOT = 43;
    private static final Material[] PANES = {Material.RED_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE,
            Material.CYAN_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE, Material.PINK_STAINED_GLASS_PANE};
    private static final String[] COLOR_NAMES = {"<c>Red", "<6>Orange", "<e>Yellow", "<a>Lime", "<2>Green", "<3>Cyan",
            "<b>Light Blue", "<9>Blue", "<5>Purple", "<d>Pink"};
    private static final float[] PITCHES = {0.5f, 0.7f, 1.0f, 1.4f, 2.0f};

    private final boolean enhanced;

    public GUIBeaconTuning(boolean enhanced) {
        this.enhanced = enhanced;
    }

    public record State(boolean enhanced, List<BeaconTune> tunes, int round, boolean done) {
        static State fresh(boolean enhanced) {
            long now = System.currentTimeMillis();
            return new State(enhanced, BeaconTune.sequence(BeaconRules.panels(enhanced), bound -> ThreadLocalRandom.current().nextInt(bound), now), 0, false);
        }

        BeaconTune tune() {
            return tunes.get(round);
        }

        State withPlayer(BeaconPane pane) {
            List<BeaconTune> next = new java.util.ArrayList<>(tunes);
            next.set(round, tune().withPlayer(pane));
            return new State(enhanced, List.copyOf(next), round, done);
        }

        State nextRound(long now) {
            List<BeaconTune> next = new java.util.ArrayList<>(tunes);
            next.set(round + 1, BeaconTune.random(bound -> ThreadLocalRandom.current().nextInt(bound), now));
            return new State(enhanced, List.copyOf(next), round + 1, done);
        }

        State finished() {
            return new State(enhanced, tunes, round, true);
        }
    }

    @Override
    public State initialState() {
        return State.fresh(enhanced);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>(enhanced ? "Signal Enhancer Tuning" : "Tune Frequency", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        layout.autoUpdating(INFO_SLOT, (s, c) -> tick(s, c), Duration.ofMillis(TICK_MILLIS));
        for (int column = 0; column < BeaconPane.WIDTH; column++) {
            int col = column;
            layout.autoUpdating(TARGET_ROW + col, (s, _) -> cell(s.tune().target(), col, true), Duration.ofMillis(TICK_MILLIS));
            layout.autoUpdating(PLAYER_ROW + col, (s, _) -> cell(s.tune().player(), col, false), Duration.ofMillis(TICK_MILLIS));
        }
        layout.slot(PAUSE_SLOT, (s, _) -> ItemStacks.item(s.tune().player().paused() ? Material.LIME_DYE : Material.RED_DYE, """
                <e>{}
                <7>Freezes or releases your pane so
                <7>you can line it up with the signal.

                <e>Click to toggle!""", s.tune().player().paused() ? "Resume" : "Pause"), (_, c) -> c.session(State.class).update(s ->
                s.withPlayer(s.tune().player().toggledPause(System.currentTimeMillis()))));
        layout.slot(COLOR_SLOT, (s, _) -> control(PANES[s.tune().player().color()], "<a>Color",
                COLOR_NAMES[s.tune().player().color()]), (click, c) -> adjust(click, c, Control.COLOR));
        layout.slot(SPEED_SLOT, (s, _) -> control(Material.CLOCK, "<a>Speed",
                "<f>" + s.tune().player().speed()), (click, c) -> adjust(click, c, Control.SPEED));
        layout.slot(PITCH_SLOT, (s, _) -> control(Material.NOTE_BLOCK, "<a>Pitch",
                "<f>" + s.tune().player().pitch()), (click, c) -> adjust(click, c, Control.PITCH));
    }

    private enum Control { COLOR, SPEED, PITCH }

    private static ItemStack.Builder control(Material material, String name, String value) {
        return ItemStacks.item(material, name + "\n<7>Current: " + value + "\n\n<e>Left click to raise\n<e>Right click to lower");
    }

    private static void adjust(ClickContext<State> click, ViewContext ctx, Control control) {
        int delta = click.click() instanceof Click.Right ? -1 : 1;
        long now = System.currentTimeMillis();
        ctx.session(State.class).update(s -> {
            BeaconPane pane = s.tune().player();
            return s.withPlayer(switch (control) {
                case COLOR -> pane.withColor(pane.color() + delta);
                case SPEED -> pane.withSpeed(pane.speed() + delta, now);
                case PITCH -> pane.withPitch(pane.pitch() + delta);
            });
        });
        BeaconPane pane = click.state().tune().player();
        if (control == Control.PITCH) ctx.player().playSound(note(Math.max(BeaconPane.MIN_LEVEL, Math.min(BeaconPane.MAX_LEVEL, pane.pitch() + delta))));
    }

    private static Sound note(int pitch) {
        return Sound.sound(Key.key("block.note_block.harp"), Sound.Source.PLAYER, 1f, PITCHES[pitch - 1]);
    }

    private static ItemStack.Builder cell(BeaconPane pane, int column, boolean target) {
        long now = System.currentTimeMillis();
        if (pane.position(now) != column) return ItemStacks.item(Material.BLACK_STAINED_GLASS_PANE, " ");
        return ItemStacks.item(PANES[pane.color()], (target ? "<f>Signal" : "<f>Your pane") + (pane.paused() ? " <7>(paused)" : ""));
    }

    private static ItemStack.Builder tick(State state, ViewContext ctx) {
        long now = System.currentTimeMillis();
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        BeaconTune tune = state.tune();
        if (!state.done()) {
            if (tune.target().arrivedAtEnd(now, TICK_MILLIS)) player.playSound(note(tune.target().pitch()));
            if (tune.player().arrivedAtEnd(now, TICK_MILLIS)) player.playSound(note(tune.player().pitch()));
            if (tune.solved(now)) solve(state, ctx, player, now);
        }
        return ItemStacks.item(Material.BOOK, """
                <9>{}
                <7>Match the pane on the bottom row to the signal on
                <7>the top row: same <a>Color<7>, <a>Speed <7>and <a>Pitch<7>,
                <7>then use <e>Pause <7>to line the two panes up.

                <7>Round: <b>{}<7>/<b>{}""", state.enhanced() ? "Signal Enhancer Tuning" : "Tune Frequency",
                state.round() + 1, state.tunes().size());
    }

    private static void solve(State state, ViewContext ctx, SkyBlockPlayer player, long now) {
        if (state.round() + 1 < state.tunes().size()) {
            ctx.session(State.class).update(s -> s.nextRound(now));
            player.sendMessage("<a>Panel tuned! <7>One more to go.");
            return;
        }
        ctx.session(State.class).update(State::finished);
        if (state.enhanced()) MoongladeBeacon.completeUpgrade(player);
        else {
            MoongladeBeacon.completeTuning(player);
            BeaconCooldowns.mark(player.getUuid(), now);
            player.addAndUpdateItem(new SkyBlockItem(ItemType.FIG_LOG, FIG_LOGS_REWARD));
            player.addAndUpdateItem(new SkyBlockItem(ItemType.MANGROVE_LOG, MANGROVE_LOGS_REWARD));
        }
        ctx.backOrClose();
    }
}
