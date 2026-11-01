package dev.daweapon.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.experiments.ExperienceBottle;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIExperimentBottles extends StatelessView {
    private static final int[] SLOTS = {11, 12, 14, 15};

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
        layout.slot(30, ExperimentGuis.item(Material.ARROW, "<a>Go Back", "<7>To Experimentation Table"), (click, c) -> c.backOrClose());
        layout.slot(31, ExperimentGuis.item(Material.BARRIER, "<c>Close"), (click, c) -> c.player().closeInventory());
    }

    private net.minestom.server.item.ItemStack.Builder display(SkyBlockPlayer player, ExperienceBottle bottle) {
        return ExperimentGuis.item(Material.EXPERIENCE_BOTTLE, title(bottle),
                "<7>Grants <3>" + ExperimentGuis.commas(bottle.orbsFor(player)) + " <7>experience orbs.",
                "<7>In your inventory: <e>" + count(player, bottle), "", "<e>Click to consume one!");
    }

    private static String title(ExperienceBottle bottle) {
        return switch (bottle) {
            case EXPERIENCE_BOTTLE -> "<f>Experience Bottle";
            case GRAND -> "<a>Grand Experience Bottle";
            case TITANIC -> "<9>Titanic Experience Bottle";
            case COLOSSAL -> "<5>Colossal Experience Bottle";
        };
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
