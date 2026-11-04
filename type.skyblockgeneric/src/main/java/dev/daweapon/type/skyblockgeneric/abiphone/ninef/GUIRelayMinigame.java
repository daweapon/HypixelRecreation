package dev.daweapon.type.skyblockgeneric.abiphone.ninef;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;

public final class GUIRelayMinigame implements View<RelayPuzzle> {
    private static final int TIMER_SLOT = 4;
    private static final int TICK_INTERVAL = 5;

    public static RelayPuzzle start(NineFRelay relay) {
        return RelayPuzzle.create(relay, System.currentTimeMillis(), java.util.concurrent.ThreadLocalRandom.current());
    }

    @Override
    public ViewConfiguration<RelayPuzzle> configuration() {
        return ViewConfiguration.withText((state, ctx) -> Text.literal("9f™ Network Relay"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(RelayPuzzle puzzle, ViewContext ctx) {
        ViewSession<RelayPuzzle> session = ctx.session(RelayPuzzle.class);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (session.isClosed() || puzzle.isOver()) return TaskSchedule.stop();
            RelayPuzzle.Outcome outcome = puzzle.tick(System.currentTimeMillis(), java.util.concurrent.ThreadLocalRandom.current());
            if (outcome == RelayPuzzle.Outcome.EXPLODED) {
                fail(player, "<c>A bomb exploded! The relay minigame failed.", "entity.generic.explode");
                return TaskSchedule.stop();
            }
            if (outcome == RelayPuzzle.Outcome.TIMED_OUT) {
                fail(player, "<c>You ran out of time! The relay minigame failed.", "block.note_block.bass");
                return TaskSchedule.stop();
            }
            session.refresh();
            return TaskSchedule.tick(TICK_INTERVAL);
        });
    }

    @Override
    public void layout(ViewLayout<RelayPuzzle> layout, RelayPuzzle puzzle, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(TIMER_SLOT, (state, c) -> ItemStacks.item(Material.CLOCK, Math.max(1, Math.min(64, state.secondsLeft(System.currentTimeMillis()))),
                "<7>Timer: <a>" + state.secondsLeft(System.currentTimeMillis()) + "s"));
        for (int column = 0; column < RelayPuzzle.COLUMNS; column++) {
            for (int row = 0; row < RelayPuzzle.ROWS; row++) {
                int tileColumn = column;
                int tileRow = row;
                layout.slot(RelayPuzzle.tileSlot(column, row), (state, c) -> tile(state, tileColumn, tileRow),
                        (click, c) -> clickTile(click, c, tileColumn, tileRow));
            }
        }
        for (int gap = 0; gap < RelayPuzzle.COLUMNS - 1; gap++) {
            for (int row = 0; row < RelayPuzzle.ROWS; row++) {
                int slot = RelayPuzzle.gapSlot(gap, row);
                layout.slot(slot, (state, c) -> gap(state, slot), (click, c) -> clickGap(click, c, slot));
            }
        }
    }

    private ItemStack.Builder tile(RelayPuzzle puzzle, int column, int row) {
        String name = "Relay Cable #" + (column + 1);
        if (column < puzzle.column() || puzzle.isLocked(column, row)) {
            return ItemStacks.item(Material.LIME_STAINED_GLASS_PANE, "<a>" + name + "\n<7>Locked in!");
        }
        if (column > puzzle.column()) {
            return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, "<c>" + name + "\n<7>Complete left first!\n<8><m>------------------");
        }
        if (puzzle.isDisabled(System.currentTimeMillis())) {
            return ItemStacks.item(Material.GRAY_STAINED_GLASS_PANE, "<7>" + name + "\n<c>Wrong order! Disabled...");
        }
        return ItemStacks.item(Material.YELLOW_STAINED_GLASS_PANE, "<e>" + name + "\n<7>Right-Click: <e>Hear!\n<7>Left-Click: <a>Lock in!");
    }

    private ItemStack.Builder gap(RelayPuzzle puzzle, int slot) {
        if (puzzle.bombSlot() != slot) return ItemStacks.filler(Material.GRAY_STAINED_GLASS_PANE);
        long left = Math.max(0, (puzzle.bombExplodesAt() - System.currentTimeMillis() + 999) / 1000);
        return ItemStacks.item(Material.TNT, (int) Math.max(1, left), "<c><l>BOMB!\n<7>Click to disarm it before it explodes!\n<7>Exploding in: <c>" + left + "s");
    }

    private void clickTile(ClickContext<RelayPuzzle> click, ViewContext ctx, int column, int row) {
        RelayPuzzle puzzle = click.state();
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (puzzle.isOver() || column != puzzle.column() || puzzle.isLocked(column, row)) return;
        long now = System.currentTimeMillis();
        if (puzzle.isDisabled(now)) return;
        RelayPuzzle.Tone tone = puzzle.tone(column, row);
        if (click.click() instanceof Click.Right) {
            player.playSound(Sound.sound(Key.key(tone.sound()), Sound.Source.MASTER, 1f, tone.pitch()));
            return;
        }
        RelayPuzzle.Outcome outcome = puzzle.lockIn(column, row, now, java.util.concurrent.ThreadLocalRandom.current());
        switch (outcome) {
            case WRONG -> player.playSound(Sound.sound(Key.key("entity.villager.no"), Sound.Source.MASTER, 1f, 1f));
            case SOLVED -> {
                player.closeInventory();
                player.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.MASTER, 1f, 1.5f));
                NineFNetwork.complete(player, puzzle.relay());
                return;
            }
            default -> player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.MASTER, 1f, 2f));
        }
        ctx.session(RelayPuzzle.class).refresh();
    }

    private void clickGap(ClickContext<RelayPuzzle> click, ViewContext ctx, int slot) {
        RelayPuzzle puzzle = click.state();
        if (puzzle.isOver() || !puzzle.disarm(slot)) return;
        ((SkyBlockPlayer) ctx.player()).playSound(Sound.sound(Key.key("block.fire.extinguish"), Sound.Source.MASTER, 1f, 1f));
        ctx.session(RelayPuzzle.class).refresh();
    }

    private void fail(SkyBlockPlayer player, String message, String sound) {
        player.closeInventory();
        player.sendMessage(message);
        player.playSound(Sound.sound(Key.key(sound), Sound.Source.MASTER, 1f, 1f));
    }
}
