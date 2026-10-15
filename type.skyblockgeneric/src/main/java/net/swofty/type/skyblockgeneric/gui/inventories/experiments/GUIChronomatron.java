package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
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

import java.util.ArrayList;
import java.util.List;

public class GUIChronomatron implements StatefulView<GUIChronomatron.Screen> {
    private static final long NOTE_MILLIS = 600;
    private static final long NOTE_GAP_MILLIS = 250;
    private static final long ROUND_PAUSE_MILLIS = 800;
    private static final List<Material> DYES = List.of(Material.RED_DYE, Material.BLUE_DYE, Material.LIME_DYE,
            Material.YELLOW_DYE, Material.LIGHT_BLUE_DYE, Material.PINK_DYE, Material.GREEN_DYE, Material.CYAN_DYE,
            Material.ORANGE_DYE, Material.PURPLE_DYE);

    public enum Phase { SHOWING, INPUT, OVER }

    public static final class Screen {
        private final ChronomatronGame game;
        private final boolean admin;
        private final List<Integer> colorOrder = new ArrayList<>();
        private Phase phase = Phase.SHOWING;
        private int lit = -1;
        private int inputIndex;
        private boolean closed;
        private boolean settled;

        public Screen(ChronomatronGame game, boolean admin) {
            this.game = game;
            this.admin = admin;
            for (int i = 0; i < game.stake().colors(); i++) colorOrder.add(i);
            java.util.Collections.shuffle(colorOrder);
        }
    }

    @Override
    public Screen initialState() {
        return null;
    }

    @Override
    public ViewConfiguration<Screen> configuration() {
        return ViewConfiguration.withString((screen, ctx) -> screen.phase == Phase.OVER
                ? "Experiment Over" : "Chronomatron (" + screen.game.stake().displayName() + ")", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<Screen> layout, Screen screen, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        ChronomatronStake stake = screen.game.stake();
        layout.slot(4, (s, c) -> s.phase == Phase.OVER ? blank()
                : ExperimentGuis.item(Material.BOOKSHELF, Math.max(1, Math.min(64, s.game.round())), "<a>Round: <f>" + s.game.round()));
        layout.slot(49, (s, c) -> status(s));
        List<int[]> tiles = stake.tileSlots();
        for (int position = 0; position < tiles.size(); position++) {
            int color = screen.colorOrder.get(position);
            for (int slot : tiles.get(position)) {
                layout.slot(slot, (s, c) -> tile(s, stake, color), (click, c) -> press(state(c), color, c));
            }
        }
        layout.slot(11, (s, c) -> s.phase == Phase.OVER ? claimButton(s) : blank(), (click, c) -> claim(state(c), c));
        layout.slot(15, (s, c) -> s.phase == Phase.OVER ? ExperimentGuis.item(Material.REDSTONE_BLOCK, "<c>Decline",
                "<7>Do you think you can do better on", "<7>this experiment?", "",
                "<7>Decline your rewards to play one more", "<7>time today!", "",
                "<c>Note: Subsequent attempts today", "<c>will cost XP levels.", "", "<e>Click to decline rewards!") : blank(),
                (click, c) -> decline(state(c), c));
    }

    private ItemStack.Builder blank() {
        return ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE);
    }

    private Screen state(ViewContext ctx) {
        return ctx.session(Screen.class).state();
    }

    private ItemStack.Builder status(Screen screen) {
        return switch (screen.phase) {
            case SHOWING -> ExperimentGuis.item(Material.GLOWSTONE, "<a>Remember the pattern!");
            case INPUT -> ExperimentGuis.item(Material.CLOCK, "<a>Repeat the pattern!");
            case OVER -> blank();
        };
    }

    private ItemStack.Builder tile(Screen screen, ChronomatronStake stake, int color) {
        if (screen.phase == Phase.OVER) return blank();
        boolean lit = screen.lit == color;
        if (lit && screen.phase == Phase.SHOWING) return ExperimentGuis.item(DYES.get(color), " ");
        ItemStack.Builder builder = ExperimentGuis.item(stake.litMaterial(color), " ");
        if (lit) return builder;
        if (screen.admin && screen.phase == Phase.INPUT) return adminHint(screen, builder, color);
        return builder;
    }

    private ItemStack.Builder adminHint(Screen screen, ItemStack.Builder builder, int color) {
        List<Integer> sequence = screen.game.sequence();
        StringBuilder steps = new StringBuilder();
        for (int i = 0; i < sequence.size(); i++) {
            if (sequence.get(i) == color) steps.append(steps.isEmpty() ? "" : ", ").append(i + 1);
        }
        boolean next = screen.inputIndex < sequence.size() && sequence.get(screen.inputIndex) == color;
        ItemStack.Builder hinted = ExperimentGuis.item(next ? DYES.get(color) : screen.game.stake().litMaterial(color),
                next ? "<a>Next!" : "<7>Admin pattern", steps.isEmpty() ? "<8>Not in this pattern" : "<7>Steps: <e>" + steps);
        return next ? ExperimentGuis.glowing(hinted) : hinted;
    }

    private ItemStack.Builder claimButton(Screen screen) {
        ChronomatronStake stake = screen.game.stake();
        int chain = screen.game.longestChain();
        return ExperimentGuis.item(Material.LIME_DYE, "<d>Chronomatron", "<8>Claim", "",
                "<7>Stakes: <d>" + stake.displayName(), "",
                "<7>Longest Chain: <a>" + chain + " notes", "",
                "<7>Rewards:", "<b> +" + ExperimentGuis.commas(stake.xpFor(chain)) + " Enchanting Exp",
                "<d> +" + stake.clickBonus().clicksFor(chain, 0) + " Superpairs clicks", "", "<e>Click to claim rewards!");
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
        screen.inputIndex = 0;
        refresh(ctx);
        long time = ROUND_PAUSE_MILLIS;
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        for (int color : screen.game.sequence()) {
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
        screen.inputIndex++;
        refresh(ctx);
        ExperimentGuis.later(200, () -> !screen.closed, () -> {
            screen.lit = -1;
            refresh(ctx);
        });
        switch (result) {
            case ROUND_COMPLETE -> {
                screen.phase = Phase.SHOWING;
                screen.lit = -1;
                refresh(ctx);
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
        player.sendMessage("<a>You claimed <b>{} Enchanting Exp <a>and <d>{} <a>extra Superpairs clicks!",
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
