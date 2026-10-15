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
import net.swofty.type.skyblockgeneric.experiments.ExperimentService;
import net.swofty.type.skyblockgeneric.experiments.UltrasequencerGame;
import net.swofty.type.skyblockgeneric.experiments.UltrasequencerStake;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashSet;
import java.util.Set;

public class GUIUltrasequencer implements StatefulView<GUIUltrasequencer.Screen> {
    private static final long MEMORIZE_MILLIS = 2_000;
    private static final long ROUND_PAUSE_MILLIS = 800;

    public enum Phase { SHOWING, INPUT, OVER }

    public static final class Screen {
        private final UltrasequencerGame game;
        private final boolean admin;
        private Phase phase = Phase.SHOWING;
        private final Set<Integer> solved = new HashSet<>();
        private boolean closed;
        private boolean settled;
        private String reason = "";

        public Screen(UltrasequencerGame game, boolean admin) {
            this.game = game;
            this.admin = admin;
        }
    }

    @Override
    public Screen initialState() {
        return null;
    }

    @Override
    public ViewConfiguration<Screen> configuration() {
        return ViewConfiguration.withString((screen, ctx) -> "Ultrasequencer (" + screen.game.stake().displayName() + ")", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<Screen> layout, Screen screen, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        layout.slot(4, (s, c) -> ExperimentGuis.item(Material.BOOKSHELF, "<a>Round: <f>" + s.game.round()));
        layout.slot(49, (s, c) -> switch (s.phase) {
            case SHOWING -> ExperimentGuis.item(Material.GLOWSTONE, "<a>Remember the pattern!");
            case INPUT -> ExperimentGuis.item(Material.CLOCK, "<a>Click the numbers in order!");
            case OVER -> ExperimentGuis.item(Material.BARRIER, "<c>Experiment Over");
        });
        for (int slot : screen.game.stake().slots()) {
            layout.slot(slot, (s, c) -> cell(s, slot), (click, c) -> press(state(c), slot, c));
        }
        layout.slot(2, (s, c) -> s.phase == Phase.OVER ? claimButton(s) : blank(), (click, c) -> claim(state(c), c));
        layout.slot(6, (s, c) -> s.phase == Phase.OVER ? ExperimentGuis.item(Material.REDSTONE_BLOCK, "<c>Decline",
                "<7>Play one more time today!", "<7>Subsequent attempts today cost", "<7>Experience Levels.", "", "<e>Click to decline!") : blank(),
                (click, c) -> decline(state(c), c));
    }

    private ItemStack.Builder blank() {
        return ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE);
    }

    private Screen state(ViewContext ctx) {
        return ctx.session(Screen.class).state();
    }

    private ItemStack.Builder cell(Screen screen, int slot) {
        int number = screen.game.numberAt(slot);
        if ((screen.phase == Phase.SHOWING || (screen.admin && screen.phase == Phase.INPUT)) && number > 0 && !screen.solved.contains(slot)) {
            return ExperimentGuis.item(UltrasequencerStake.dyeFor(number), number, "<a>" + number);
        }
        if (screen.solved.contains(slot)) {
            return ExperimentGuis.item(Material.LIME_STAINED_GLASS_PANE, number, "<a>" + number);
        }
        return ExperimentGuis.item(screen.phase == Phase.OVER ? Material.GRAY_STAINED_GLASS_PANE
                : UltrasequencerStake.paneFor(screen.game.round()), "<7>?");
    }

    private ItemStack.Builder claimButton(Screen screen) {
        UltrasequencerStake stake = screen.game.stake();
        int length = screen.game.longestSequence();
        return ExperimentGuis.item(Material.LIME_DYE, "<a>Claim Rewards",
                "<7>Longest Sequence: <e>" + length,
                "<7>Rewards:", "<8> +<b>" + ExperimentGuis.commas(stake.xpFor(length)) + " Enchanting Exp",
                "<8> +<e>" + stake.clickBonus().clicksFor(length, 0) + " <8>Superpairs clicks", "", "<e>Click to claim!");
    }

    @Override
    public void onOpen(Screen screen, ViewContext ctx) {
        startRound(screen, ctx);
    }

    @Override
    public void onClose(Screen screen, ViewContext ctx, ViewSession.CloseReason reason) {
        screen.closed = true;
        if (screen.phase == Phase.OVER || screen.settled || reason == ViewSession.CloseReason.REPLACED) return;
        screen.game.abandon();
        screen.phase = Phase.OVER;
        screen.reason = "You closed the game!";
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        net.minestom.server.MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            screen.closed = false;
            player.openView(new GUIUltrasequencer(), screen);
        });
    }

    private void startRound(Screen screen, ViewContext ctx) {
        screen.game.nextRound();
        screen.solved.clear();
        screen.phase = Phase.SHOWING;
        refresh(ctx);
        ExperimentGuis.later(ROUND_PAUSE_MILLIS + MEMORIZE_MILLIS, () -> !screen.closed, () -> {
            screen.phase = Phase.INPUT;
            refresh(ctx);
        });
    }

    private void press(Screen screen, int slot, ViewContext ctx) {
        if (screen.phase != Phase.INPUT || screen.solved.contains(slot)) return;
        boolean emptyCell = screen.game.numberAt(slot) == 0;
        UltrasequencerGame.Result result = screen.game.press(slot);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        switch (result) {
            case CORRECT -> {
                screen.solved.add(slot);
                ExperimentGuis.sound(player, "block.note_block.harp", 1.2f);
            }
            case ROUND_COMPLETE -> {
                screen.solved.add(slot);
                ExperimentGuis.sound(player, "entity.experience_orb.pickup", 1.4f);
                screen.phase = Phase.SHOWING;
                ExperimentGuis.later(400, () -> !screen.closed, () -> startRound(screen, ctx));
            }
            case FAILED, FINISHED -> {
                screen.phase = Phase.OVER;
                screen.reason = result == UltrasequencerGame.Result.FAILED ? (emptyCell ? "Clicked empty cell" : "Clicked the wrong number") : "Maximum series";
                ExperimentGuis.sound(player, "block.note_block.bass", 0.6f);
            }
        }
        refresh(ctx);
    }

    private void claim(Screen screen, ViewContext ctx) {
        if (screen.phase != Phase.OVER || screen.settled) return;
        screen.settled = true;
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int clicks = ExperimentService.claimUltrasequencer(player, screen.game);
        player.sendMessage("<e>You claimed the <a>Ultrasequencer <e>rewards!");
        player.sendMessage("<7> +<b>{} Enchanting Exp", ExperimentGuis.commas(screen.game.stake().xpFor(screen.game.longestSequence())));
        if (clicks > 0) player.sendMessage("<7> +<d>{} Superpairs {}", clicks, clicks == 1 ? "click" : "clicks");
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
