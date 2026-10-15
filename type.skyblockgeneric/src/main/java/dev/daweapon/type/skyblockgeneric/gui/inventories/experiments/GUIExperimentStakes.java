package dev.daweapon.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.experiments.ChronomatronGame;
import dev.daweapon.type.skyblockgeneric.experiments.ChronomatronStake;
import dev.daweapon.type.skyblockgeneric.experiments.ClickBonus;
import dev.daweapon.type.skyblockgeneric.experiments.ExperimentService;
import dev.daweapon.type.skyblockgeneric.experiments.ExperimentTable;
import dev.daweapon.type.skyblockgeneric.experiments.SuperpairsGame;
import dev.daweapon.type.skyblockgeneric.experiments.SuperpairsStake;
import dev.daweapon.type.skyblockgeneric.experiments.UltrasequencerGame;
import dev.daweapon.type.skyblockgeneric.experiments.UltrasequencerStake;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIExperimentStakes implements StatefulView<GUIExperimentStakes.State> {
    public enum Experiment { CHRONOMATRON, ULTRASEQUENCER, SUPERPAIRS }

    public record State(Experiment experiment) {
    }

    private static final int[] SUPERPAIRS_SLOTS = {21, 22, 23, 30, 31, 32};
    private static final int[] CHRONOMATRON_SLOTS = {20, 21, 22, 23, 24};
    private static final int[] ULTRASEQUENCER_SLOTS = {21, 22, 23};
    private static final Material[] DYES = {Material.LIGHT_BLUE_DYE, Material.LIME_DYE, Material.YELLOW_DYE,
            Material.ORANGE_DYE, Material.RED_DYE, Material.MAGENTA_DYE};

    @Override
    public State initialState() {
        return new State(Experiment.SUPERPAIRS);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> switch (state.experiment()) {
            case CHRONOMATRON -> "Chronomatron ➜ Stakes";
            case ULTRASEQUENCER -> "Ultrasequencer ➜ Stakes";
            case SUPERPAIRS -> "Superpairs ➜ Stakes";
        }, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.PURPLE_STAINED_GLASS_PANE));
        int[] slots = switch (state.experiment()) {
            case CHRONOMATRON -> CHRONOMATRON_SLOTS;
            case ULTRASEQUENCER -> ULTRASEQUENCER_SLOTS;
            case SUPERPAIRS -> SUPERPAIRS_SLOTS;
        };
        for (int i = 0; i < slots.length; i++) {
            int index = i;
            layout.slot(slots[i], (s, c) -> display(s.experiment(), index, (SkyBlockPlayer) c.player()),
                    (click, c) -> start(state.experiment(), index, (SkyBlockPlayer) c.player(), c));
        }
        layout.slot(48, ExperimentGuis.item(Material.ARROW, "<a>Go Back", "<7>To Experimentation Table"), (click, c) -> c.backOrClose());
        layout.slot(49, ExperimentGuis.item(Material.BARRIER, "<c>Close"), (click, c) -> c.player().closeInventory());
    }

    private ItemStack.Builder display(Experiment experiment, int index, SkyBlockPlayer player) {
        int level = ExperimentTable.enchantingLevel(player);
        List<String> lines = new ArrayList<>();
        Material icon;
        int required;
        switch (experiment) {
            case CHRONOMATRON -> {
                ChronomatronStake stake = ChronomatronStake.values()[index];
                icon = DYES[Math.min(index + 1, DYES.length - 1)];
                required = stake.requiredEnchanting();
                lines.add(stake.color() + stake.displayName() + " Experiment");
                lines.add("<8>Chronomatron");
                lines.add("");
                lines.add("<7>Colors on board: <d>" + stake.colors());
                lines.add("");
                lines.add("<7>XP Reward: <b>" + shortNumber(stake.xpPerNote()) + " Enchanting Exp");
                lines.add("<7> per <e>note in longest chain!");
                lines.add("");
                lines.add("<7>Superpairs Rewards:");
                lines.addAll(bonusLines("Chain", stake.clickBonus()));
                lines.add("");
                lines.add("<7>Requires: <b>Enchanting " + StringUtility.getAsRomanNumeral(required));
                int cost = ExperimentTable.attemptCost(player, "C" + stake.name(), stake.restartLevels());
                if (cost > 0) lines.add("<7>Retry cost: <c>" + cost + " XP Levels");
            }
            case ULTRASEQUENCER -> {
                UltrasequencerStake stake = UltrasequencerStake.values()[index];
                icon = DYES[index + 2];
                required = stake.requiredEnchanting();
                lines.add(stake.color() + stake.displayName() + " Experiment");
                lines.add("<8>Ultrasequencer");
                lines.add("");
                lines.add("<7>Grid size: <d>" + stake.gridLabel());
                lines.add("");
                lines.add("<7>XP Reward: <b>" + shortNumber(stake.xpPerNumber()) + " Enchanting Exp");
                lines.add("<7> per <e>number in highest series!");
                lines.add("");
                lines.add("<7>Superpairs Rewards:");
                lines.addAll(bonusLines("Series", stake.clickBonus()));
                lines.add("");
                lines.add("<7>Requires: <b>Enchanting " + StringUtility.getAsRomanNumeral(required));
                int cost = ExperimentTable.attemptCost(player, "U" + stake.name(), stake.restartLevels());
                if (cost > 0) lines.add("<7>Retry cost: <c>" + cost + " XP Levels");
            }
            default -> {
                SuperpairsStake stake = SuperpairsStake.values()[index];
                icon = DYES[index];
                required = stake.requiredEnchanting();
                lines.add(stake.color() + stake.displayName() + " Experiment");
                lines.add("<8>Superpairs");
                lines.add("");
                lines.add("<7>Grid Size: <d>" + stake.gridLabel());
                lines.add("");
                lines.add("<7>Possible pairs:");
                lines.add(" <b>" + shortNumber(stake.pairXpMin()) + "-" + shortNumber(stake.pairXpMax()) + " Enchanting Exp");
                if (index <= SuperpairsStake.HIGH.ordinal()) lines.add(" <f>10-30 Experience Bottles");
                if (index >= SuperpairsStake.HIGH.ordinal()) {
                    lines.add(" <a>5-20 Grand Experience Bottles");
                    lines.add(" <9>" + (index >= SuperpairsStake.TRANSCENDENT.ordinal() ? "1-2" : "1") + " Titanic Experience Bottles");
                }
                if (index >= SuperpairsStake.GRAND.ordinal()) lines.add(" <6>III-VI Enchanted Books");
                if (index >= SuperpairsStake.GRAND.ordinal()) lines.add(" <d>1 Metaphysical Serum");
                if (index >= SuperpairsStake.SUPREME.ordinal()) {
                    lines.add("");
                    lines.add("<d>Rare chance to find VII books!");
                    lines.add("<6>Very rare chance to find Nadeshiko Dye!");
                }
                lines.add("");
                int addOns = ExperimentTable.addOnClicks(ExperimentTable.data(player), stake);
                lines.add("<7>Clicks: <d>" + (stake.baseClicks() + 6 * stake.addOnMultiplier() + ExperimentTable.SERUM_BONUS_CLICKS));
                lines.add(" <d>+" + stake.baseClicks() + " <7>default");
                lines.add(" <d>+" + 6 * stake.addOnMultiplier() + " <5>add-ons" + (stake.addOnMultiplier() > 1 ? " <7>(doubled!)" : ""));
                lines.add(" <d>+" + ExperimentTable.SERUM_BONUS_CLICKS + " <e>serum");
                lines.add("");
                lines.add("<7>Requires: <b>Enchanting " + StringUtility.getAsRomanNumeral(required));
                lines.add("<7>Starting cost: <b>" + stake.startLevels() + " XP Levels");
                lines.add("<7>Your XP: <a>" + player.getLevel());
            }
        }
        lines.add("");
        lines.add(level >= required ? "<e>Click to play!" : "<c>You need a higher Enchanting level!");
        return ExperimentGuis.item(icon, lines.toArray(new String[0]));
    }

    private List<String> bonusLines(String noun, ClickBonus bonus) {
        List<String> lines = new ArrayList<>();
        int[] thresholds = bonus.thresholds();
        for (int i = 0; i < thresholds.length; i++) {
            lines.add("<7> " + noun + " of <e>" + thresholds[i] + "<7>: <a>+" + (i + 1) + (i == 0 ? " Click" : " Clicks"));
        }
        return lines;
    }

    private String shortNumber(long value) {
        if (value >= 1_000 && value % 1_000 == 0) return (value / 1_000) + "k";
        if (value >= 1_000) return String.format("%.1fk", value / 1_000D);
        return String.valueOf(value);
    }

    private void start(Experiment experiment, int index, SkyBlockPlayer player, ViewContext ctx) {
        switch (experiment) {
            case CHRONOMATRON -> {
                ChronomatronGame game = ExperimentService.startChronomatron(player, ChronomatronStake.values()[index]);
                if (game != null) ctx.replace(new GUIChronomatron(), new GUIChronomatron.Screen(game, isAdmin(player)));
            }
            case ULTRASEQUENCER -> {
                UltrasequencerGame game = ExperimentService.startUltrasequencer(player, UltrasequencerStake.values()[index]);
                if (game != null) ctx.replace(new GUIUltrasequencer(), new GUIUltrasequencer.Screen(game, isAdmin(player)));
            }
            case SUPERPAIRS -> {
                SuperpairsGame game = ExperimentService.startSuperpairs(player, SuperpairsStake.values()[index]);
                if (game != null) ctx.replace(new GUISuperpairs(), new GUISuperpairs.Screen(game));
            }
        }
    }

    static boolean isAdmin(SkyBlockPlayer player) {
        return player.getRank() == dev.daweapon.type.generic.user.categories.Rank.STAFF;
    }
}
