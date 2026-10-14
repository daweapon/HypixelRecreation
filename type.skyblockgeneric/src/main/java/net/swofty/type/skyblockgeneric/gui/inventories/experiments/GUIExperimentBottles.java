package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.experiments.ExperienceBottle;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIExperimentBottles extends StatelessView {
    private static final int[] SLOTS = {10, 12, 14, 16};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Bottles of Enchanting", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.filler(ItemStacks.filler(Material.PURPLE_STAINED_GLASS_PANE));
        ExperienceBottle[] bottles = ExperienceBottle.values();
        for (int i = 0; i < bottles.length; i++) {
            ExperienceBottle bottle = bottles[i];
            layout.slot(SLOTS[i], (s, c) -> display((SkyBlockPlayer) c.player(), bottle),
                    (click, c) -> consume((SkyBlockPlayer) c.player(), bottle));
        }
        layout.slot(30, ExperimentGuis.item(Material.ARROW, "<a>Go Back"), (click, c) -> c.backOrClose());
        layout.slot(31, ExperimentGuis.item(Material.BARRIER, "<c>Close"), (click, c) -> c.player().closeInventory());
    }

    private net.minestom.server.item.ItemStack.Builder display(SkyBlockPlayer player, ExperienceBottle bottle) {
        return ExperimentGuis.item(Material.EXPERIENCE_BOTTLE, "<a>" + bottle.name().replace('_', ' ') + " Bottle",
                "<7>Grants <b>" + ExperimentGuis.commas(bottle.orbsFor(player)) + " <7>experience.",
                "<7>In your inventory: <e>" + count(player, bottle), "", "<e>Click to consume one!");
    }

    private int count(SkyBlockPlayer player, ExperienceBottle bottle) {
        return player.countItem(bottle.itemType());
    }

    private void consume(SkyBlockPlayer player, ExperienceBottle bottle) {
        if (!player.removeItemFromPlayer(bottle.itemType(), 1)) {
            player.sendMessage("<c>You do not have any of these bottles!");
            return;
        }
        bottle.apply(player);
        player.sendMessage("<a>You consumed a bottle and received <b>{}<a> experience!", ExperimentGuis.commas(bottle.orbsFor(player)));
    }
}
