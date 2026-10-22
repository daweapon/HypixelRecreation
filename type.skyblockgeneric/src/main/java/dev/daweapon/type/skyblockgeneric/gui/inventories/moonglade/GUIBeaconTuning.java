package dev.daweapon.type.skyblockgeneric.gui.inventories.moonglade;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.BeaconBeat;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.BeaconRules;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.MoongladeBeacon;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GUIBeaconTuning implements StatefulView<GUIBeaconTuning.State> {
    public static final long PLAY_MILLIS = 3000;
    private static final int INFO_SLOT = 4;
    private static final int FLASH_SLOT = 13;
    private static final int PLAY_SLOT = 22;
    private static final int SUBMIT_SLOT = 31;
    private static final int[] COLOR_SLOTS = {19, 20, 21, 23, 24, 25};
    private static final int[] SPEED_SLOTS = {37, 38, 39};
    private static final int[] PITCH_SLOTS = {41, 42, 43};
    private static final Material[] PANES = {Material.RED_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE};
    private static final String[] COLOR_NAMES = {"<c>Red", "<6>Orange", "<e>Yellow", "<a>Lime", "<b>Light Blue", "<5>Purple"};
    private static final String[] SPEED_NAMES = {"Slow", "Medium", "Fast"};
    private static final String[] PITCH_NAMES = {"Low", "Medium", "High"};

    private final boolean enhanced;

    public GUIBeaconTuning(boolean enhanced) {
        this.enhanced = enhanced;
    }

    public record State(boolean enhanced, List<BeaconBeat> beats, int panel, int color, int speed, int pitch,
                        long playStartedAt, int mistakes, boolean done) {
        static State fresh(boolean enhanced) {
            return new State(enhanced, BeaconBeat.sequence(BeaconRules.panels(enhanced), bound -> ThreadLocalRandom.current().nextInt(bound)),
                    0, -1, -1, -1, 0, 0, false);
        }

        BeaconBeat current() {
            return beats.get(panel);
        }

        boolean complete() {
            return color >= 0 && speed >= 0 && pitch >= 0;
        }

        boolean correct() {
            return complete() && current().matches(BeaconBeat.Color.values()[color], BeaconBeat.Speed.values()[speed],
                    BeaconBeat.Pitch.values()[pitch]);
        }

        State withColor(int value) {
            return new State(enhanced, beats, panel, value, speed, pitch, playStartedAt, mistakes, done);
        }

        State withSpeed(int value) {
            return new State(enhanced, beats, panel, color, value, pitch, playStartedAt, mistakes, done);
        }

        State withPitch(int value) {
            return new State(enhanced, beats, panel, color, speed, value, playStartedAt, mistakes, done);
        }

        State playing(long at) {
            return new State(enhanced, beats, panel, color, speed, pitch, at, mistakes, done);
        }

        State mistaken() {
            return new State(enhanced, beats, panel, color, speed, pitch, playStartedAt, mistakes + 1, done);
        }

        State nextPanel() {
            return new State(enhanced, beats, panel + 1, -1, -1, -1, 0, mistakes, done);
        }

        State finished() {
            return new State(enhanced, beats, panel, color, speed, pitch, playStartedAt, mistakes, true);
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
        layout.slot(INFO_SLOT, ItemStacks.item(Material.BOOK, """
                <9>{}
                <7>Listen to the beat and watch the pane.
                <7>Then match its <a>Color<7>, <a>Speed <7>and <a>Pitch<7>.

                <7>Panel: <b>{}<7>/<b>{}
                <7>Mistakes: <c>{}""", enhanced ? "Signal Enhancer Tuning" : "Tune Frequency",
                state.panel() + 1, state.beats().size(), state.mistakes()));
        layout.autoUpdating(FLASH_SLOT, (s, _) -> flash(s), Duration.ofMillis(100));
        layout.slot(PLAY_SLOT, ItemStacks.item(Material.NOTE_BLOCK, """
                <e>Play the beat
                <7>The pane flashes at the beat's speed
                <7>and plays its pitch.

                <e>Click to listen!"""), (click, c) -> play(click, c));
        for (int i = 0; i < COLOR_SLOTS.length; i++) {
            int index = i;
            layout.slot(COLOR_SLOTS[i], selectable(PANES[i], COLOR_NAMES[i], state.color() == i, 1),
                    (_, c) -> c.session(State.class).update(s -> s.withColor(index)));
        }
        for (int i = 0; i < SPEED_SLOTS.length; i++) {
            int index = i;
            layout.slot(SPEED_SLOTS[i], selectable(Material.CLOCK, "<a>" + SPEED_NAMES[i] + " <7>Speed", state.speed() == i, i + 1),
                    (_, c) -> c.session(State.class).update(s -> s.withSpeed(index)));
        }
        for (int i = 0; i < PITCH_SLOTS.length; i++) {
            int index = i;
            layout.slot(PITCH_SLOTS[i], selectable(Material.NOTE_BLOCK, "<a>" + PITCH_NAMES[i] + " <7>Pitch", state.pitch() == i, i + 1),
                    (_, c) -> c.session(State.class).update(s -> s.withPitch(index)));
        }
        layout.slot(SUBMIT_SLOT, ItemStacks.item(Material.EMERALD, """
                <a>Tune!
                <7>Submit the selected color, speed
                <7>and pitch.

                <e>Click to submit!"""), (click, c) -> submit(click, c));
    }

    private static ItemStack.Builder selectable(Material material, String name, boolean selected, int amount) {
        ItemStack.Builder item = ItemStacks.item(material, amount, name + "\n\n" + (selected ? "<a>Selected!" : "<e>Click to select!"));
        return selected ? ItemStacks.enchanted(item) : item;
    }

    private static ItemStack.Builder flash(State state) {
        long elapsed = System.currentTimeMillis() - state.playStartedAt();
        if (state.playStartedAt() > 0 && elapsed < PLAY_MILLIS) {
            BeaconBeat beat = state.current();
            Material pane = beat.lit(elapsed) ? PANES[beat.color().ordinal()] : Material.BLACK_STAINED_GLASS_PANE;
            return ItemStacks.item(pane, "<f>Listen...");
        }
        return ItemStacks.item(Material.GRAY_STAINED_GLASS_PANE, "<7>Press <e>Play the beat <7>to see the signal.");
    }

    private static void play(ClickContext<State> click, ViewContext ctx) {
        BeaconBeat beat = click.state().current();
        ctx.session(State.class).update(s -> s.playing(System.currentTimeMillis()));
        HypixelPlayer player = ctx.player();
        int cycleTicks = beat.speed().halfPeriodTicks() * 2;
        int cycles = (int) (PLAY_MILLIS / (cycleTicks * 50L));
        Sound sound = Sound.sound(Key.key("block.note_block.harp"), Sound.Source.PLAYER, 1f, beat.pitch().value());
        for (int cycle = 0; cycle < cycles; cycle++) ScheduleUtility.delay(() -> player.playSound(sound), cycle * cycleTicks);
    }

    private static void submit(ClickContext<State> click, ViewContext ctx) {
        State state = click.state();
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (state.done()) return;
        if (!state.complete()) {
            player.sendMessage("<c>Select a color, a speed and a pitch first!");
            return;
        }
        if (!state.correct()) {
            ctx.session(State.class).update(State::mistaken);
            player.sendMessage("<c>The frequency is off! Listen to the beat again.");
            return;
        }
        if (state.panel() + 1 < state.beats().size()) {
            ctx.session(State.class).update(State::nextPanel);
            player.sendMessage("<a>Panel tuned! <7>One more to go.");
            return;
        }
        ctx.session(State.class).update(State::finished);
        if (state.enhanced()) MoongladeBeacon.completeUpgrade(player);
        else MoongladeBeacon.completeTuning(player);
        ctx.backOrClose();
    }
}
