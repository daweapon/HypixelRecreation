package dev.daweapon.type.skyblockgeneric.gui.inventories.rabbits;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.StatefulView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.hoppity.HoppityShopService;
import dev.daweapon.type.skyblockgeneric.hoppity.HoppityShopStock;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIHoppityShop implements StatefulView<GUIHoppityShop.State> {
    private static final String HOPPITY_TEXTURE = "b79e7f3341b672d9de6564cbaca052a6a723ea466a2e66af35ba1ba855f0d692";
    private static final int FIRST_SLOT = 10;
    private static final int CLOSE_SLOT = 31;
    private static final int BACK_SLOT = 30;

    public record State(int purchases) {
    }

    @Override
    public State initialState() {
        return new State(0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Hoppity's Shop", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        SkyBlockPlayer viewer = (SkyBlockPlayer) ctx.player();
        List<ChocolateRabbit> stock = HoppityShopService.stock(viewer);
        for (int index = 0; index < stock.size(); index++) {
            int slot = index;
            ChocolateRabbit rabbit = stock.get(index);
            layout.slot(FIRST_SLOT + index, (s, c) -> rabbitItem((SkyBlockPlayer) c.player(), rabbit),
                    (click, c) -> buy((SkyBlockPlayer) c.player(), slot, c));
        }
        Components.back(layout, BACK_SLOT, ctx);
        Components.close(layout, CLOSE_SLOT);
    }

    private void buy(SkyBlockPlayer player, int slot, ViewContext ctx) {
        HoppityShopService.Result result = HoppityShopService.buy(player, slot);
        switch (result) {
            case CANNOT_AFFORD -> player.sendMessage("<c>You cannot afford this rabbit!");
            case ALREADY_PURCHASED -> player.sendMessage("<c>You already purchased this rabbit this season!");
            default -> {
            }
        }
        if (result == HoppityShopService.Result.BOUGHT) {
            ctx.session(State.class).update(current -> new State(current.purchases() + 1));
        }
    }

    private ItemStack.Builder rabbitItem(SkyBlockPlayer player, ChocolateRabbit rabbit) {
        boolean purchased = HoppityShopService.purchased(player, rabbit);
        boolean owned = ChocolateFactoryHelper.getData(player).hasFoundRabbit(rabbit);
        long price = HoppityShopStock.price(rabbit);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Grants <6>+{} Chocolate <7>and <6>{}", rabbit.getChocolateBonus(),
                String.format("%.3fx", rabbit.getMultiplierBonus())));
        lore.add(Text.of("<6>Chocolate <7>per second to your"));
        lore.add(Text.of("<6>Chocolate Factory<7>."));
        lore.add(Text.empty());
        lore.add(rabbit.getRarity().getFormattedName());
        lore.add(Text.empty());
        lore.add(Text.of("<7>Cost: <6>{} Coins", StringUtility.commaify(price)));
        lore.add(Text.empty());
        if (purchased) {
            lore.add(Text.of("<a>Already purchased!"));
        } else {
            if (owned) {
                lore.add(Text.of("<e>You own this rabbit: you will"));
                lore.add(Text.of("<e>receive <6>Chocolate <e>instead."));
                lore.add(Text.empty());
            }
            lore.add(Text.of("<e>Click to purchase!"));
        }
        if (purchased) return ItemStacks.item(Material.GRAY_DYE, 1, rabbit.getFormattedName(), lore);
        return ItemStacks.head(HOPPITY_TEXTURE, 1, rabbit.getFormattedName(), lore);
    }
}
