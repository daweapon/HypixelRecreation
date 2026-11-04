package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class GUIRaffleHistory implements View<RaffleSnapshot> {
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43};
    private static final int EMPTY_SLOT = 22;
    private static final int BACK_SLOT = 48;
    private static final int CLOSE_SLOT = 49;

    @Override
    public ViewConfiguration<RaffleSnapshot> configuration() {
        return new ViewConfiguration<>("All Raffles", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<RaffleSnapshot> layout, RaffleSnapshot state, ViewContext ctx) {
        Components.fill(layout);
        if (state.history().isEmpty()) {
            layout.slot(EMPTY_SLOT, ItemStacks.item(Material.BARRIER, """
                    <c>No Raffles Yet
                    <7>You haven't been entered into a
                    <7>raffle that has been drawn yet."""));
        }
        for (int i = 0; i < state.history().size() && i < SLOTS.length; i++) {
            RaffleSnapshot.Row row = state.history().get(i);
            layout.slot(SLOTS[i], (_, _) -> render(row), (c, _) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                RaffleClaims.claimResult(player, state, row, () -> RaffleMenus.open(player, GUIRaffleHistory::new));
            });
        }
        layout.slot(BACK_SLOT, Components.BACK_BUTTON, (c, _) -> RaffleMenus.openBox((SkyBlockPlayer) c.player()));
        Components.close(layout, CLOSE_SLOT);
    }

    private static ItemStack.Builder render(RaffleSnapshot.Row row) {
        RaffleType type = RaffleClock.typeOf(row.key());
        String outcome;
        if (!row.drawn()) outcome = "<7>Waiting for the draw...";
        else if (row.tier() != null) outcome = "<a>You won a " + row.tier().display() + " <r><a>prize!";
        else outcome = "<7>You didn't win a prize.";
        String action;
        if (row.claimed()) action = "<8>Already claimed.";
        else if (row.claimable()) action = "<e>Click to claim!";
        else action = "<8>Nothing to claim.";
        Material material = row.claimable() ? Material.CHEST_MINECART : Material.MINECART;
        return ItemStacks.item(material, """
                %s%s
                <7>You entered: <b>%s tickets

                %s

                %s""".formatted(type.colour(), row.label(), StringUtility.commaify(row.entered()), outcome, action));
    }
}
