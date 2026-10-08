package net.swofty.type.skyblockgeneric.gui.inventories.kuudra;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Consumer;

/** The Follower NPCs' "Claim Reward" menu (wiki UI: the reward alone at row 3, column 5). */
public class GUIKuudraClaimReward extends StatelessView {
    private final ItemType reward;
    private final Consumer<SkyBlockPlayer> onClaim;

    public GUIKuudraClaimReward(ItemType reward, Consumer<SkyBlockPlayer> onClaim) {
        this.reward = reward;
        this.onClaim = onClaim;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Claim Reward", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(22, (_, c) -> ItemStacks.appendLore(
                        PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(), new SkyBlockItem(reward).getItemStack()),
                        "\n<e>Click to claim!"),
                (_, c) -> {
                    c.player().closeInventory();
                    onClaim.accept((SkyBlockPlayer) c.player());
                });
    }
}
