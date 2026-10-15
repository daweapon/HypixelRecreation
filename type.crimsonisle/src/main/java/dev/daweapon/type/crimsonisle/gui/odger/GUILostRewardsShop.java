package dev.daweapon.type.crimsonisle.gui.odger;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.fishing.trophy.OdgerRewards;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Odger's Lost Rewards Shop (wiki: Odger/UI): buy back any Trophy Fishing reward for coins once its rank is reached.
 */
public class GUILostRewardsShop extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Lost Rewards Shop", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx, ItemStacks.item(Material.ARROW, "<a>Go Back\n<7>To Trophy Fishing"));
        Components.close(layout, 49);
        for (OdgerRewards.ShopEntry entry : OdgerRewards.LOST_REWARDS_SHOP) {
            layout.slot(entry.slot(), (_, c) -> icon((SkyBlockPlayer) c.player(), entry), (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                switch (OdgerRewards.buy(player, entry)) {
                    case BOUGHT -> player.sendMessage("<a>You bought back an item!");
                    case LOCKED -> player.sendMessage("<c>You haven't unlocked this reward yet!");
                    case TOO_POOR -> player.sendMessage("<c>You don't have enough coins!");
                    case UNAVAILABLE -> player.sendMessage("<c>This item isn't available yet.");
                }
                c.session().refresh();
            });
        }
    }

    private static ItemStack.Builder icon(SkyBlockPlayer player, OdgerRewards.ShopEntry entry) {
        ItemType type = ItemType.get(entry.itemId());
        String name = type == null ? StringUtility.toNormalCase(entry.itemId()) : new SkyBlockItem(type).getDisplayName();
        StringBuilder text = new StringBuilder("<f>").append(name).append("\n\n<7>Cost\n<6>")
                .append(StringUtility.commaify(entry.coins())).append(" Coins\n\n");
        if (type == null) {
            text.append("<c>Not available yet.");
        } else if (entry.requiredRank() != null && !entry.requiredRank().achieved(player.getTrophyFishData())) {
            text.append("<c>Not unlocked!\n<7>Requires ").append(entry.requiredRank().coloredName());
        } else {
            text.append("<e>Click to trade!");
        }
        return ItemStacks.item(type == null ? Material.GRAY_DYE : type.material, text.toString());
    }
}
