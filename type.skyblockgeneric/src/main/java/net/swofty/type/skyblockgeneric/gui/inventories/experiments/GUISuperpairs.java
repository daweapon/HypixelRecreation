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
import net.swofty.type.skyblockgeneric.experiments.PairPrize;
import net.swofty.type.skyblockgeneric.experiments.SuperpairsGame;
import net.swofty.type.skyblockgeneric.experiments.SuperpairsResult;
import net.swofty.type.skyblockgeneric.experiments.SuperpairsStake;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUISuperpairs implements StatefulView<GUISuperpairs.Screen> {
    private static final long MISMATCH_MILLIS = 900;

    public static final class Screen {
        private final SuperpairsGame game;
        private final long deadline = System.currentTimeMillis() + SuperpairsStake.TIME_LIMIT_SECONDS * 1000L;
        private SuperpairsResult result;
        private boolean closed;

        public Screen(SuperpairsGame game) {
            this.game = game;
        }
    }

    @Override
    public Screen initialState() {
        return null;
    }

    @Override
    public ViewConfiguration<Screen> configuration() {
        return ViewConfiguration.withString((screen, ctx) -> "Superpairs (" + screen.game.stake().displayName() + ")", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<Screen> layout, Screen screen, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        layout.slot(4, (s, c) -> s.result == null
                ? ExperimentGuis.item(Material.BOOKSHELF, Math.max(1, Math.min(64, s.game.clicksRemaining())), "<a>Remaining Clicks: <f>" + s.game.clicksRemaining())
                : summary(s.result));
        layout.slot(49, (s, c) -> s.result == null
                ? ExperimentGuis.item(Material.CLOCK, "<a>Timer: <f>" + secondsLeft(s) + "s")
                : ExperimentGuis.item(Material.BARRIER, "<c>Close"), (click, c) -> {
            if (state(c).result != null) c.player().closeInventory();
        });
        for (int tile = 0; tile < screen.game.tileCount(); tile++) {
            int index = tile;
            layout.slot(screen.game.stake().slotOf(tile), (s, c) -> render(s, index), (click, c) -> click(state(c), index, c));
        }
    }

    private Screen state(ViewContext ctx) {
        return ctx.session(Screen.class).state();
    }

    private int secondsLeft(Screen screen) {
        return (int) Math.max(0, (screen.deadline - System.currentTimeMillis() + 999) / 1000);
    }

    private ItemStack.Builder render(Screen screen, int tile) {
        SuperpairsGame game = screen.game;
        if (screen.result != null && !game.isRevealed(tile)) {
            return ExperimentGuis.item(Material.GRAY_STAINED_GLASS_PANE, "<7>" + prizeName(game.prizeAt(tile)));
        }
        if (game.isRevealed(tile)) {
            PairPrize prize = game.prizeAt(tile);
            ItemStack.Builder builder = icon(prize);
            return game.isMatched(tile) ? ExperimentGuis.glowing(builder) : builder;
        }
        return ExperimentGuis.item(Material.CYAN_STAINED_GLASS_PANE, game.instantFindActive() ? "<b>Instant Find!" : "<a>Click any button!");
    }

    private String prizeName(PairPrize prize) {
        return prize.name();
    }

    private ItemStack.Builder icon(PairPrize prize) {
        SkyBlockItem item = ExperimentService.itemFor(prize);
        if (item != null) {
            return item.getItemStack().builder().amount(Math.min(64, Math.max(1, prize.amount())));
        }
        return ExperimentGuis.item(prize.icon(), prize.name());
    }

    private ItemStack.Builder summary(SuperpairsResult result) {
        List<String> lines = new ArrayList<>();
        lines.add("<a>Experiment Over");
        lines.add("<7>Pairs found: <e>" + result.pairsFound() + "<7>/<e>" + result.pairsTotal());
        lines.add("<8> +<b>" + ExperimentGuis.commas(result.stakeXp()) + " Enchanting Exp <8>(Stakes)");
        lines.add("<8> +<b>" + ExperimentGuis.commas(result.pairXp()) + " Enchanting Exp <8>(Pairs)");
        if (!result.prizes().isEmpty()) lines.add("");
        for (PairPrize prize : result.prizes()) lines.add("<8> +" + prize.name());
        return ExperimentGuis.item(Material.EXPERIENCE_BOTTLE, lines.toArray(new String[0]));
    }

    @Override
    public void onOpen(Screen screen, ViewContext ctx) {
        tick(screen, ctx);
    }

    @Override
    public void onClose(Screen screen, ViewContext ctx, ViewSession.CloseReason reason) {
        screen.closed = true;
        finish(screen, ctx);
    }

    private void tick(Screen screen, ViewContext ctx) {
        ExperimentGuis.later(1000, () -> !screen.closed && screen.result == null, () -> {
            if (secondsLeft(screen) <= 0) finish(screen, ctx);
            else {
                refresh(ctx);
                tick(screen, ctx);
            }
        });
    }

    private void click(Screen screen, int tile, ViewContext ctx) {
        if (screen.result != null) return;
        SuperpairsGame.Click click = screen.game.click(tile);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        switch (click.outcome()) {
            case IGNORED -> {
                return;
            }
            case FIRST_REVEALED -> ExperimentGuis.sound(player, "block.note_block.harp", 1.0f);
            case MATCHED -> ExperimentGuis.sound(player, "entity.experience_orb.pickup", 1.3f);
            case MISMATCHED -> {
                ExperimentGuis.sound(player, "block.note_block.bass", 0.7f);
                ExperimentGuis.later(MISMATCH_MILLIS, () -> !screen.closed && screen.result == null, () -> {
                    screen.game.hidePending();
                    if (screen.game.isOver()) finish(screen, ctx);
                    else refresh(ctx);
                });
            }
        }
        if (screen.game.isOver() && !screen.game.awaitingHide()) finish(screen, ctx);
        else refresh(ctx);
    }

    private void finish(Screen screen, ViewContext ctx) {
        if (screen.result != null) return;
        screen.result = ExperimentService.finishSuperpairs((SkyBlockPlayer) ctx.player(), screen.game);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        player.sendMessage("<a>Experiment over! You gained <b>{} Enchanting Exp<a> and found <e>{} <a>pairs.",
                ExperimentGuis.commas(screen.result.totalXp()), screen.result.pairsFound());
        if (!screen.closed) refresh(ctx);
    }

    private void refresh(ViewContext ctx) {
        ViewSession<Screen> session = ctx.session(Screen.class);
        session.setState(session.state());
    }
}
