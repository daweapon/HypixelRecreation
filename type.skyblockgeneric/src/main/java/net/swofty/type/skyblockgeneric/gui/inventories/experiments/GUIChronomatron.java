package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.experiments.ChronomatronGame;
import net.swofty.type.skyblockgeneric.experiments.ChronomatronStake;
import net.swofty.type.skyblockgeneric.experiments.ExperimentService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIChronomatron implements StatefulView<GUIChronomatron.Screen> {
    private static final long NOTE_MILLIS = 600;
    private static final long NOTE_GAP_MILLIS = 250;
    private static final long ROUND_PAUSE_MILLIS = 800;

    public enum Phase { SHOWING, INPUT, OVER }

    public static final class Screen {
        private final ChronomatronGame game;
        private Phase phase = Phase.SHOWING;
        private int lit = -1;
        private boolean closed;
        private boolean settled;
        private int clicksEarned;

        public Screen(ChronomatronGame game) {
            this.game = game;
        }
    }

    @Override
    public Screen initialState() {
        return null;
    }

    @Override
    public ViewConfiguration<Screen> configuration() {
        return ViewConfiguration.withString((screen, ctx) -> "Chronomatron (" + screen.game.stake().displayName() + ")", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<Screen> layout, Screen screen, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        ChronomatronStake stake = screen.game.stake();
        layout.slot(4, (s, c) -> ExperimentGuis.item(Material.BOOKSHELF, "<a>Round: <f>" + s.game.round()));
        layout.slot(49, (s, c) -> status(s));
        int[] slots = stake.slots();
        for (int color = 0; color < slots.length; color++) {
            int index = color;
            layout.slot(slots[color], (s, c) -> tile(s, stake, index), (click, c) -> press(s(c), index, c));
        }
        layout.slot(11, (s, c) -> s.phase == Phase.OVER ? claimButton(s) : blank(), (click, c) -> claim(s(c), c));
        layout.slot(15, (s, c) -> s.phase == Phase.OVER ? ExperimentGuis.item(Material.REDSTONE_BLOCK, "<c>Decline",
                "<7>Play one more time today!", "<7>Subsequent attempts today cost", "<7>Experience Levels.", "", "<e>Click to decline!") : blank(),
                (click, c) -> decline(s(c), c));
    }

    private net.minestom.server.item.ItemStack.Builder blank() {
        return ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE);
    }

    private Screen s(ViewContext ctx) {
        return ctx.session(Screen.class).state();
    }

    private net.minestom.server.item.ItemStack.Builder status(Screen screen) {
        return switch (screen.phase) {
            case SHOWING -> ExperimentGuis.item(Material.GLOWSTONE, "<a>Remember the pattern!");
            case INPUT -> ExperimentGuis.item(Material.CLOCK, "<a>Repeat the pattern!");
            case OVER -> ExperimentGuis.item(Material.BARRIER, "<c>Experiment Over");
        };
    }

    private net.minestom.server.item.ItemStack.Builder tile(Screen screen, ChronomatronStake stake, int color) {
        if (screen.phase == Phase.OVER) return ExperimentGuis.item(stake.dimMaterial(color), " ");
        boolean lit = screen.lit == color;
        net.minestom.server.item.ItemStack.Builder builder = ExperimentGuis.item(lit ? stake.litMaterial(color) : stake.dimMaterial(color), " ");
        return lit ? ExperimentGuis.glowing(builder) : builder;
    }

    private net.minestom.server.item.ItemStack.Builder claimButton(Screen screen) {
        ChronomatronStake stake = screen.game.stake();
        int chain = screen.game.longestChain();
        return ExperimentGuis.item(Material.LIME_DYE, "<a>Claim Rewards",
                "<7>Longest Chain: <e>" + chain,
                "<7>Rewards:", "<8> +<b>" + ExperimentGuis.commas(stake.xpFor(chain)) + " Enchanting Exp",
                "<8> +<e>" + stake.clickBonus().clicksFor(chain, 0) + " <8>Superpairs clicks", "", "<e>Click to claim!");
    }

    @Override
    public void onOpen(Screen screen, ViewContext ctx) {
        startRound(screen, ctx);
    }

    @Override
    public void onClose(Screen screen, ViewContext ctx, ViewSession.CloseReason reason) {
        screen.closed = true;
        screen.game.abandon();
    }

    private void startRound(Screen screen, ViewContext ctx) {
        screen.game.nextRound();
        screen.phase = Phase.SHOWING;
        screen.lit = -1;
        refresh(ctx);
        List<Integer> sequence = screen.game.sequence();
        long time = ROUND_PAUSE_MILLIS;
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        for (int color : sequence) {
            ExperimentGuis.later(time, () -> !screen.closed, () -> {
                screen.lit = color;
                ExperimentGuis.sound(player, "block.note_block.harp", 0.6f + color * 0.12f);
                refresh(ctx);
            });
            ExperimentGuis.later(time + NOTE_MILLIS, () -> !screen.closed, () -> {
                screen.lit = -1;
                refresh(ctx);
            });
            time += NOTE_MILLIS + NOTE_GAP_MILLIS;
        }
        ExperimentGuis.later(time, () -> !screen.closed, () -> {
            screen.phase = Phase.INPUT;
            refresh(ctx);
        });
    }

    private void press(Screen screen, int color, ViewContext ctx) {
        if (screen.phase != Phase.INPUT) return;
        ExperimentGuis.sound((SkyBlockPlayer) ctx.player(), "block.note_block.harp", 0.6f + color * 0.12f);
        screen.lit = color;
        ChronomatronGame.Result result = screen.game.press(color);
        refresh(ctx);
        ExperimentGuis.later(200, () -> !screen.closed, () -> {
            screen.lit = -1;
            refresh(ctx);
        });
        switch (result) {
            case ROUND_COMPLETE -> {
                screen.phase = Phase.SHOWING;
                ExperimentGuis.later(400, () -> !screen.closed, () -> startRound(screen, ctx));
            }
            case FAILED, FINISHED -> {
                screen.phase = Phase.OVER;
                refresh(ctx);
            }
            default -> {
            }
        }
    }

    private void claim(Screen screen, ViewContext ctx) {
        if (screen.phase != Phase.OVER || screen.settled) return;
        screen.settled = true;
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int clicks = ExperimentService.claimChronomatron(player, screen.game);
        player.sendMessage("<a>You claimed <b>{} Enchanting Exp <a>and <e>{} <a>extra Superpairs clicks!",
                ExperimentGuis.commas(screen.game.stake().xpFor(screen.game.longestChain())), clicks);
        ctx.backOrClose();
    }

    private void decline(Screen screen, ViewContext ctx) {
        if (screen.phase != Phase.OVER || screen.settled) return;
        screen.settled = true;
        ctx.backOrClose();
    }

    private void refresh(ViewContext ctx) {
        ViewSession<Screen> session = ctx.session(Screen.class);
        session.setState(session.state());
    }
}
