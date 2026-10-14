package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.experiments.ExperimentTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIExperimentRenewal extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Buy Another Experiment", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.PURPLE_STAINED_GLASS_PANE));
        layout.slot(13, (s, c) -> offer((SkyBlockPlayer) c.player()), (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            if (ExperimentTable.renew(player)) {
                player.sendMessage("<a>Your Experimentation Table was recharged!");
                c.backOrClose();
            }
        });
        layout.slot(31, ExperimentGuis.item(Material.ARROW, "<a>Go Back"), (click, c) -> c.backOrClose());
    }

    private net.minestom.server.item.ItemStack.Builder offer(SkyBlockPlayer player) {
        int used = ExperimentTable.renewalsToday(ExperimentTable.data(player));
        if (used >= ExperimentTable.RENEWAL_LEVELS.length) {
            return ExperimentGuis.item(Material.BARRIER, "<c>No more games today",
                    "<7>You have bought all " + ExperimentTable.RENEWAL_LEVELS.length + " extra games for today.", "<7>Resets at 12:00 AM GMT.");
        }
        return ExperimentGuis.item(Material.EMERALD, "<a>Buy another Superpairs game",
                "<7>Cost: <b>" + ExperimentTable.RENEWAL_LEVELS[used] + " Exp Levels <7>and <b>" + ExperimentTable.RENEWAL_BITS[used] + " Bits",
                "<7>Games bought today: <e>" + used + "<7>/" + ExperimentTable.RENEWAL_LEVELS.length, "", "<e>Click to purchase!");
    }
}
