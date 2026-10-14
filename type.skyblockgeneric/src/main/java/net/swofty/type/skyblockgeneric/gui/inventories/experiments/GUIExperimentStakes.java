package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.experiments.ChronomatronGame;
import net.swofty.type.skyblockgeneric.experiments.ChronomatronStake;
import net.swofty.type.skyblockgeneric.experiments.ExperimentService;
import net.swofty.type.skyblockgeneric.experiments.ExperimentTable;
import net.swofty.type.skyblockgeneric.experiments.SuperpairsGame;
import net.swofty.type.skyblockgeneric.experiments.SuperpairsStake;
import net.swofty.type.skyblockgeneric.experiments.UltrasequencerGame;
import net.swofty.type.skyblockgeneric.experiments.UltrasequencerStake;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIExperimentStakes implements StatefulView<GUIExperimentStakes.State> {
    public enum Experiment { CHRONOMATRON, ULTRASEQUENCER, SUPERPAIRS }

    public record State(Experiment experiment) {
    }

    @Override
    public State initialState() {
        return new State(Experiment.SUPERPAIRS);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> switch (state.experiment()) {
            case CHRONOMATRON -> "Chronomatron";
            case ULTRASEQUENCER -> "Ultrasequencer";
            case SUPERPAIRS -> "Superpairs";
        }, InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.PURPLE_STAINED_GLASS_PANE));
        int count = switch (state.experiment()) {
            case CHRONOMATRON -> ChronomatronStake.values().length;
            case ULTRASEQUENCER -> UltrasequencerStake.values().length;
            case SUPERPAIRS -> SuperpairsStake.values().length;
        };
        int first = 10 + (7 - count) / 2;
        for (int i = 0; i < count; i++) {
            int index = i;
            layout.slot(first + i, (s, c) -> display(s.experiment(), index, (SkyBlockPlayer) c.player()),
                    (click, c) -> start(state.experiment(), index, (SkyBlockPlayer) c.player(), c));
        }
        layout.slot(30, ExperimentGuis.item(Material.ARROW, "<a>Go Back"), (click, c) -> c.backOrClose());
        layout.slot(31, ExperimentGuis.item(Material.BARRIER, "<c>Close"), (click, c) -> c.player().closeInventory());
    }

    private ItemStack.Builder display(Experiment experiment, int index, SkyBlockPlayer player) {
        int level = ExperimentTable.enchantingLevel(player);
        List<String> lines = new ArrayList<>();
        String name;
        Material icon;
        int required;
        switch (experiment) {
            case CHRONOMATRON -> {
                ChronomatronStake stake = ChronomatronStake.values()[index];
                name = stake.displayName();
                icon = stake.litMaterial(index);
                required = stake.requiredEnchanting();
                lines.add("<7>Colors: <e>" + stake.colors());
                lines.add("<7>Exp per note: <b>" + ExperimentGuis.commas(stake.xpPerNote()) + " <8>(max " + ChronomatronStake.XP_NOTE_CAP + " notes)");
                int cost = ExperimentTable.attemptCost(player, "C" + stake.name(), stake.restartLevels());
                lines.add(cost > 0 ? "<7>Retry cost: <c>" + cost + " Exp Levels" : "<7>First attempt today is free");
            }
            case ULTRASEQUENCER -> {
                UltrasequencerStake stake = UltrasequencerStake.values()[index];
                name = stake.displayName();
                icon = UltrasequencerStake.dyeFor(index * 3 + 1);
                required = stake.requiredEnchanting();
                lines.add("<7>Exp per number: <b>" + ExperimentGuis.commas(stake.xpPerNumber()) + " <8>(max " + UltrasequencerStake.XP_NUMBER_CAP + " numbers)");
                int cost = ExperimentTable.attemptCost(player, "U" + stake.name(), stake.restartLevels());
                lines.add(cost > 0 ? "<7>Retry cost: <c>" + cost + " Exp Levels" : "<7>First attempt today is free");
            }
            default -> {
                SuperpairsStake stake = SuperpairsStake.values()[index];
                name = stake.displayName();
                icon = Material.CYAN_STAINED_GLASS_PANE;
                required = stake.requiredEnchanting();
                lines.add("<7>Cost: <c>" + stake.startLevels() + " Exp Levels");
                lines.add("<7>Base clicks: <e>" + stake.baseClicks() + " <7>+ add-ons");
                lines.add("<7>Stake Exp: <b>" + ExperimentGuis.commas(stake.stakeXp()));
                lines.add("<7>Pair Exp: <b>" + ExperimentGuis.commas(stake.pairXpMin()) + "<7>-<b>" + ExperimentGuis.commas(stake.pairXpMax()));
            }
        }
        lines.add(0, "");
        lines.add("");
        lines.add(level >= required ? "<e>Click to play!" : "<c>Requires Enchanting " + required);
        List<String> all = new ArrayList<>();
        all.add((level >= required ? "<a>" : "<c>") + name);
        all.addAll(lines);
        return ExperimentGuis.item(icon, all.toArray(new String[0]));
    }

    private void start(Experiment experiment, int index, SkyBlockPlayer player, ViewContext ctx) {
        switch (experiment) {
            case CHRONOMATRON -> {
                ChronomatronGame game = ExperimentService.startChronomatron(player, ChronomatronStake.values()[index]);
                if (game != null) ctx.replace(new GUIChronomatron(), new GUIChronomatron.Screen(game));
            }
            case ULTRASEQUENCER -> {
                UltrasequencerGame game = ExperimentService.startUltrasequencer(player, UltrasequencerStake.values()[index]);
                if (game != null) ctx.replace(new GUIUltrasequencer(), new GUIUltrasequencer.Screen(game));
            }
            case SUPERPAIRS -> {
                SuperpairsGame game = ExperimentService.startSuperpairs(player, SuperpairsStake.values()[index]);
                if (game != null) ctx.replace(new GUISuperpairs(), new GUISuperpairs.Screen(game));
            }
        }
    }
}
