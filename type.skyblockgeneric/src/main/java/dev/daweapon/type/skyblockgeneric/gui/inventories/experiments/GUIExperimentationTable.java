package dev.daweapon.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointExperiments;
import dev.daweapon.type.skyblockgeneric.experiments.ExperimentRngMeter;
import dev.daweapon.type.skyblockgeneric.experiments.ExperimentTable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIExperimentationTable extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Experimentation Table", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.PURPLE_STAINED_GLASS_PANE));
        for (int slot : new int[]{20, 21, 23, 24}) {
            layout.slot(slot, ExperimentGuis.item(Material.BLACK_STAINED_GLASS_PANE, "<7>Pending experiment..."));
        }
        layout.slot(22, (s, c) -> superpairs((SkyBlockPlayer) c.player()),
                (click, c) -> openSuperpairs((SkyBlockPlayer) c.player(), c));
        layout.slot(29, (s, c) -> chronomatron((SkyBlockPlayer) c.player()),
                (click, c) -> c.push(new GUIExperimentStakes(), new GUIExperimentStakes.State(GUIExperimentStakes.Experiment.CHRONOMATRON)));
        layout.slot(33, (s, c) -> ultrasequencer((SkyBlockPlayer) c.player()),
                (click, c) -> c.push(new GUIExperimentStakes(), new GUIExperimentStakes.State(GUIExperimentStakes.Experiment.ULTRASEQUENCER)));
        layout.slot(48, (s, c) -> meter((SkyBlockPlayer) c.player()), (click, c) -> c.push(new GUIExperimentRngMeter()));
        layout.slot(50, ExperimentGuis.item(Material.EXPERIENCE_BOTTLE, "<3>Experience Bottles",
                "<7>Missing experience?", "<7>Simple! Just consume the <3>Experience", "<3>Bottles <7>from your inventories", "<7>directly or purchase some at the", "<7>current <6>Bazaar <7>price!", "", "<e>Click to view!"),
                (click, c) -> c.push(new GUIExperimentBottles()));
        layout.slot(49, ExperimentGuis.item(Material.BARRIER, "<c>Close"), (click, c) -> c.player().closeInventory());
    }

    private void openSuperpairs(SkyBlockPlayer player, ViewContext ctx) {
        if (ExperimentTable.chargesAvailable(ExperimentTable.data(player), System.currentTimeMillis()) <= 0) {
            ctx.push(new GUIExperimentRenewal());
            return;
        }
        if (!ExperimentTable.superpairsUnlocked(player)) {
            player.sendMessage("<c>Complete the add-on experiments first!");
            return;
        }
        ctx.push(new GUIExperimentStakes(), new GUIExperimentStakes.State(GUIExperimentStakes.Experiment.SUPERPAIRS));
    }

    private net.minestom.server.item.ItemStack.Builder superpairs(SkyBlockPlayer player) {
        DatapointExperiments.ExperimentsData data = ExperimentTable.data(player);
        long now = System.currentTimeMillis();
        int charges = ExperimentTable.chargesAvailable(data, now);
        boolean unlocked = ExperimentTable.superpairsUnlocked(player);
        String status = charges <= 0 ? "<c>No charges left! Click to buy another game."
                : unlocked ? "<e>Click to play!" : "<c>Play the add-on experiments first!";
        return ExperimentGuis.glowing(ExperimentGuis.item(Material.CYAN_STAINED_GLASS_PANE, "<d>Superpairs",
                "<8>Main Experiment", "",
                "<7>Find <b>pairs <7>of items on the", "<7>grid to unlock them.", "",
                "<7>Earn <3>Enchanting Exp <7>and", "<c>powerful <7>enchanted books", "<7>every day!",
                "", "<d>Play add-ons first!", "",
                "<7>Charges: <a>" + charges + "<7>/<a>" + ExperimentTable.CHARGES,
                charges < ExperimentTable.CHARGES ? "<7>Next charge in: <e>" + duration(ExperimentTable.millisUntilCharge(data, now)) : "",
                "", status));
    }

    private net.minestom.server.item.ItemStack.Builder chronomatron(SkyBlockPlayer player) {
        boolean done = ExperimentTable.chronomatronDone(ExperimentTable.data(player));
        return ExperimentGuis.item(Material.CYAN_STAINED_GLASS, "<d>Chronomatron",
                "<8>Add-on Experiment", "",
                "<a>Repeat <7>the musical <d>pattern", "<7>to form the longest chain of", "<7>notes.", "",
                "<7>Earn extra <3>Enchanting Exp", "<7>and extra clicks on your", "<7>next <d>Superpairs<7>!", "",
                ExperimentTable.enchantingLevel(player) < ExperimentTable.CHRONOMATRON_REQUIRED_LEVEL
                        ? "<c>Requires Enchanting " + ExperimentTable.CHRONOMATRON_REQUIRED_LEVEL
                        : done ? "<a>Completed!" : "<e>Click to play!");
    }

    private net.minestom.server.item.ItemStack.Builder ultrasequencer(SkyBlockPlayer player) {
        boolean done = ExperimentTable.ultrasequencerDone(ExperimentTable.data(player));
        return ExperimentGuis.item(Material.LIME_DYE, "<d>Ultrasequencer",
                "<8>Add-on Experiment", "",
                "<a>1. <7>Number(s) appear for 2 seconds.", "<e>2. <7>They all disappear!", "<c>3. <7>Click them in order from memory.", "",
                "<7>Earn extra <3>Enchanting Exp", "<7>and extra clicks on your", "<7>next <d>Superpairs<7>!", "",
                ExperimentTable.enchantingLevel(player) < ExperimentTable.ULTRASEQUENCER_REQUIRED_LEVEL
                        ? "<c>Requires Enchanting " + ExperimentTable.ULTRASEQUENCER_REQUIRED_LEVEL
                        : done ? "<a>Completed!" : "<e>Click to play!");
    }

    private net.minestom.server.item.ItemStack.Builder meter(SkyBlockPlayer player) {
        ExperimentRngMeter.Target target = ExperimentRngMeter.selected(player);
        java.util.List<String> lines = new java.util.ArrayList<>(java.util.List.of(
                "<d>Experimentation Table RNG Meter",
                "<7>Your <d>Experimentation Table RNG", "<d>Meter <7>fills with <b>Enchanting XP <7>every",
                "<7>time you complete your <d>Superpairs<7>!", "",
                "<7>Your selected <d>RNG Drop <7>will be", "<7>guaranteed to appear in your next",
                "<d>Superpairs <7>once your <d>RNG Meter <7>is", "<7>full!", ""));
        if (target == null) {
            lines.add("<7>Selected Drop");
            lines.add("<c>None");
            lines.add("");
            lines.add("<e>Click to select a drop!");
        } else {
            lines.add("<7>Selected Drop");
            lines.add(target.prize().name());
            lines.add("");
            lines.add("<7>Progress: <d>" + String.format("%.1f", ExperimentRngMeter.percent(player, target)) + "%");
            lines.add("<d>" + ExperimentGuis.commas(ExperimentRngMeter.stored(player)) + "<5>/<d>" + ExperimentGuis.commas(target.meterXp()) + " XP");
            lines.add("");
            lines.add("<e>Click to view!");
        }
        return ExperimentGuis.item(Material.ENDER_EYE, lines.toArray(new String[0]));
    }

    private String duration(long millis) {
        long minutes = millis / 60_000;
        return (minutes / 60) + "h " + (minutes % 60) + "m";
    }
}
