package dev.daweapon.type.hub.gui;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles.Toggles.ToggleType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Consumer;

public class GUIAnglerAngusReward extends StatelessView {
    private static final int BAIT_AMOUNT = 16;

    private final Consumer<SkyBlockPlayer> onClaim;

    public GUIAnglerAngusReward(Consumer<SkyBlockPlayer> onClaim) {
        this.onClaim = onClaim;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Claim Reward", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        layout.slot(22,
                ItemStacks.appendLore(
                        PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(ItemType.FISH_BAIT).getItemStack()).amount(BAIT_AMOUNT),
                        "\n<e>Click to claim!"),
                (click, context) -> {
                    if (player.getToggles().get(ToggleType.HAS_CLAIMED_ANGLER_ANGUS_BAIT)) return;
                    player.getToggles().set(ToggleType.HAS_CLAIMED_ANGLER_ANGUS_BAIT, true);
                    player.addAndUpdateItem(ItemType.FISH_BAIT, BAIT_AMOUNT);
                    player.closeInventory();
                    onClaim.accept(player);
                });
    }
}
