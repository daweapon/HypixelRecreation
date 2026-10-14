package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.experiments.ExperimentRngMeter;
import net.swofty.type.skyblockgeneric.experiments.ExperimentService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIExperimentRngMeter implements StatefulView<GUIExperimentRngMeter.State> {
    private static final int PER_PAGE = 36;

    public record State(int page) {
    }

    @Override
    public State initialState() {
        return new State(0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Experimentation RNG Meter", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.PURPLE_STAINED_GLASS_PANE));
        List<ExperimentRngMeter.Target> targets = ExperimentRngMeter.targets();
        int pages = (targets.size() + PER_PAGE - 1) / PER_PAGE;
        for (int i = 0; i < PER_PAGE; i++) {
            int index = state.page() * PER_PAGE + i;
            if (index >= targets.size()) break;
            ExperimentRngMeter.Target target = targets.get(index);
            layout.slot(9 + i, (s, c) -> display((SkyBlockPlayer) c.player(), target), (click, c) -> select((SkyBlockPlayer) c.player(), target, c));
        }
        if (state.page() > 0) {
            layout.slot(48, ExperimentGuis.item(Material.ARROW, "<a>Previous Page"), (click, c) -> c.session(State.class).setState(new State(state.page() - 1)));
        }
        if (state.page() < pages - 1) {
            layout.slot(50, ExperimentGuis.item(Material.ARROW, "<a>Next Page"), (click, c) -> c.session(State.class).setState(new State(state.page() + 1)));
        }
        layout.slot(49, ExperimentGuis.item(Material.ARROW, "<a>Go Back"), (click, c) -> c.backOrClose());
        layout.slot(4, (s, c) -> header((SkyBlockPlayer) c.player()));
    }

    private ItemStack.Builder header(SkyBlockPlayer player) {
        ExperimentRngMeter.Target target = ExperimentRngMeter.selected(player);
        return ExperimentGuis.item(Material.ENDER_EYE, "<d>Experimentation Table RNG Meter",
                "<7>Every <d>" + ExperimentGuis.commas(ExperimentRngMeter.ENCHANTING_XP_PER_METER_XP) + " Enchanting Exp <7>earned in",
                "<7>Superpairs fills the meter by <d>1 XP<7>.",
                "<7>When it is full, your selected drop is", "<7>guaranteed in your next Superpairs.", "",
                target == null ? "<c>No drop selected" : "<7>Selected: " + target.prize().name());
    }

    private ItemStack.Builder display(SkyBlockPlayer player, ExperimentRngMeter.Target target) {
        boolean selected = target.id().equals(player.getSkyblockDataHandler() == null ? null
                : ExperimentRngMeter.selected(player) == null ? null : ExperimentRngMeter.selected(player).id());
        SkyBlockItem real = ExperimentService.itemFor(target.prize());
        ItemStack.Builder builder = real != null ? real.getItemStack().builder().amount(1)
                : ExperimentGuis.item(target.prize().icon(), target.prize().name());
        String progress = selected
                ? "<7>Progress: <d>" + String.format("%.1f", ExperimentRngMeter.percent(player, target)) + "%"
                : "<7>Required: <d>" + ExperimentGuis.commas(target.meterXp()) + " XP";
        ItemStack.Builder shown = ItemStacks.lore(builder, "<8>RNG Meter drop\n" + progress + "\n"
                + (selected ? "<d>" + ExperimentGuis.commas(ExperimentRngMeter.stored(player)) + "<5>/<d>" + ExperimentGuis.commas(target.meterXp()) + " XP\n\n<a>Selected!" : "\n<e>Click to select!"));
        return selected ? ExperimentGuis.glowing(shown) : shown;
    }

    private void select(SkyBlockPlayer player, ExperimentRngMeter.Target target, ViewContext ctx) {
        boolean already = ExperimentRngMeter.selected(player) != null && ExperimentRngMeter.selected(player).id().equals(target.id());
        ExperimentRngMeter.select(player, already ? null : target);
        ctx.session(State.class).setState(ctx.session(State.class).state());
    }
}
