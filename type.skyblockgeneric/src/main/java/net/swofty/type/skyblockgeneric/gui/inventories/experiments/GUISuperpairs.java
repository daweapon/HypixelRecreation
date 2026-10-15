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
    public enum Phase { PLAYING, REVIEW, REWARDS }

    public static final class Screen {
        private final SuperpairsGame game;
        private final long deadline = System.currentTimeMillis() + SuperpairsStake.TIME_LIMIT_SECONDS * 1000L;
        private Phase phase = Phase.PLAYING;
        private SuperpairsResult result;
        private boolean claimed;
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
        return ViewConfiguration.withString((screen, ctx) -> screen.phase == Phase.REWARDS ? "Superpairs Rewards"
                : "Superpairs (" + screen.game.stake().displayName() + ")", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<Screen> layout, Screen screen, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        layout.slot(4, (s, c) -> switch (s.phase) {
            case PLAYING -> ExperimentGuis.item(Material.BOOKSHELF, Math.max(1, Math.min(64, s.game.clicksRemaining())),
                    "<a>Remaining Clicks: <f>" + s.game.clicksRemaining());
            case REVIEW -> blank();
            case REWARDS -> blank();
        });
        layout.slot(49, (s, c) -> s.phase == Phase.PLAYING
                ? ExperimentGuis.item(Material.CLOCK, Math.max(1, Math.min(64, secondsLeft(s))), "<a>Timer: <f>" + secondsLeft(s) + "s")
                : s.phase == Phase.REVIEW ? ExperimentGuis.item(Material.CLOCK, Math.max(1, Math.min(64, secondsLeft(s))), "<a>Experiment over") : blank());
        for (int tile = 0; tile < screen.game.tileCount(); tile++) {
            int index = tile;
            layout.slot(screen.game.stake().slotOf(tile), (s, c) -> render(s, index, (SkyBlockPlayer) c.player()), (click, c) -> click(state(c), index, c));
        }
    }

    private ItemStack.Builder blank() {
        return ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE);
    }

    private Screen state(ViewContext ctx) {
        return ctx.session(Screen.class).state();
    }

    private int secondsLeft(Screen screen) {
        return (int) Math.max(0, (screen.deadline - System.currentTimeMillis() + 999) / 1000);
    }

    private ItemStack.Builder render(Screen screen, int tile, SkyBlockPlayer player) {
        SuperpairsGame game = screen.game;
        if (screen.phase == Phase.REWARDS) return game.stake().slotOf(tile) == 22 ? summary(screen.result) : blank();
        if (screen.phase == Phase.REVIEW) {
            ItemStack.Builder builder = proceedHint(icon(player, game.prizeAt(tile)));
            return game.isMatched(tile) ? ExperimentGuis.glowing(builder) : builder;
        }
        if (game.isRevealed(tile)) {
            ItemStack.Builder builder = icon(player, game.prizeAt(tile));
            return game.isMatched(tile) ? ExperimentGuis.glowing(builder) : builder;
        }
        return ExperimentGuis.item(Material.LIGHT_BLUE_STAINED_GLASS, game.instantFindActive() ? "<b>Instant Find!"
                : game.hasFirstPick() ? "<e>Click a second button!" : "<e>Click any button!");
    }

    private ItemStack.Builder proceedHint(ItemStack.Builder builder) {
        java.util.List<net.swofty.commons.text.Text> lore = new java.util.ArrayList<>(ItemStacks.loreText(builder));
        lore.add(net.swofty.commons.text.Text.of(" "));
        lore.add(net.swofty.commons.text.Text.of("<e>Click any to proceed!"));
        return ItemStacks.lore(builder, lore);
    }

    private ItemStack.Builder icon(SkyBlockPlayer player, PairPrize prize) {
        SkyBlockItem item = ExperimentService.itemFor(prize);
        if (item != null) {
            ItemStack.Builder real = ExperimentGuis.real(player, item, prize.amount());
            java.util.List<net.swofty.commons.text.Text> lore = new java.util.ArrayList<>();
            lore.add(net.swofty.commons.text.Text.of("<8>Item Reward"));
            lore.add(net.swofty.commons.text.Text.of(" "));
            lore.addAll(ItemStacks.loreText(real));
            return ItemStacks.lore(real, lore);
        }
        if (prize.kind() == PairPrize.Kind.LAPIS_BLOCK) {
            return ExperimentGuis.item(prize.icon(), prize.name(), "<7>+1 Click", "<b>Instant powerup!");
        }
        if (prize.kind() == PairPrize.Kind.DIAMOND) {
            return ExperimentGuis.item(Material.DIAMOND, prize.name(), "<7>+1 Click", "<b>Powerup");
        }
        if (prize.kind() == PairPrize.Kind.XP) {
            return ExperimentGuis.item(prize.icon(), prize.name(), "<8>Skill Exp Reward");
        }
        return ExperimentGuis.item(prize.icon(), prize.name());
    }

    private ItemStack.Builder summary(SuperpairsResult result) {
        List<String> lines = new ArrayList<>();
        lines.add("<d>Superpairs");
        lines.add("<8>Experiment over");
        lines.add("");
        lines.add("<7>Stakes: <d>" + result.stakeName());
        lines.add("");
        lines.add("<7>Clicks used: <a>" + result.clicksUsed());
        lines.add("<7>Pairs found: <e>" + result.pairsFound());
        lines.add("");
        lines.add("<7>Rewards:");
        lines.add("<b> +" + ExperimentGuis.commas(result.stakeXp()) + " Enchanting Exp <b>(Stakes)");
        lines.add("<b> +" + ExperimentGuis.commas(result.pairXp()) + " Enchanting Exp <b>(Pairs)");
        for (PairPrize prize : result.prizes()) lines.add("<7> + " + prize.name());
        lines.add("");
        lines.add("<e>Click to claim rewards!");
        return ExperimentGuis.item(Material.CHEST, lines.toArray(new String[0]));
    }

    @Override
    public void onOpen(Screen screen, ViewContext ctx) {
        tick(screen, ctx);
    }

    @Override
    public void onClose(Screen screen, ViewContext ctx, ViewSession.CloseReason reason) {
        screen.closed = true;
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (screen.result == null) screen.result = ExperimentService.finishSuperpairs(player, screen.game);
        if (!screen.claimed) award(screen, player);
    }

    private void tick(Screen screen, ViewContext ctx) {
        ExperimentGuis.later(1000, () -> !screen.closed && screen.phase == Phase.PLAYING, () -> {
            if (secondsLeft(screen) <= 0) finish(screen, ctx);
            else {
                refresh(ctx);
                tick(screen, ctx);
            }
        });
    }

    private void click(Screen screen, int tile, ViewContext ctx) {
        if (screen.phase == Phase.REVIEW) {
            screen.phase = Phase.REWARDS;
            refresh(ctx);
            return;
        }
        if (screen.phase == Phase.REWARDS) {
            if (screen.game.stake().slotOf(tile) == 22) claim(screen, ctx);
            return;
        }
        if (screen.phase != Phase.PLAYING) return;
        SuperpairsGame.Click click = screen.game.click(tile);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        switch (click.outcome()) {
            case IGNORED -> {
                return;
            }
            case FIRST_REVEALED -> ExperimentGuis.sound(player, "block.note_block.harp", 1.0f);
            case MATCHED -> ExperimentGuis.sound(player, "entity.experience_orb.pickup", 1.3f);
            case MISMATCHED -> ExperimentGuis.sound(player, "block.note_block.bass", 0.7f);
        }
        if (screen.game.isOver()) finish(screen, ctx);
        else refresh(ctx);
    }

    private void finish(Screen screen, ViewContext ctx) {
        if (screen.result != null) return;
        screen.result = ExperimentService.finishSuperpairs((SkyBlockPlayer) ctx.player(), screen.game);
        screen.phase = Phase.REVIEW;
        refresh(ctx);
    }

    private void claim(Screen screen, ViewContext ctx) {
        if (screen.phase != Phase.REWARDS || screen.claimed) return;
        award(screen, (SkyBlockPlayer) ctx.player());
        ctx.player().closeInventory();
    }

    private void award(Screen screen, SkyBlockPlayer player) {
        if (screen.claimed || screen.result == null) return;
        screen.claimed = true;
        ExperimentService.claimSuperpairs(player, screen.game, screen.result);
        player.sendMessage("<e>You claimed the <d>Superpairs <e>rewards!");
        player.sendMessage("<7> +<b>" + ExperimentGuis.commas(screen.result.stakeXp()) + " Enchanting Exp");
        player.sendMessage("<7> +<b>" + ExperimentGuis.commas(screen.result.pairXp()) + " Enchanting Exp");
        for (PairPrize prize : screen.result.prizes()) player.sendMessage("<7> +" + prize.name());
    }

    private void refresh(ViewContext ctx) {
        ViewSession<Screen> session = ctx.session(Screen.class);
        session.setState(session.state());
    }
}
