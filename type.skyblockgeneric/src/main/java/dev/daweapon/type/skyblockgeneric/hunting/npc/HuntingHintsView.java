package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.function.BiConsumer;

public class HuntingHintsView extends StatelessView {
    private final String title;
    private final List<HuntingHints.Hint> hints;
    private final BiConsumer<SkyBlockPlayer, HuntingHints.Hint> onPaid;

    public HuntingHintsView(String title, List<HuntingHints.Hint> hints, BiConsumer<SkyBlockPlayer, HuntingHints.Hint> onPaid) {
        this.title = title;
        this.hints = hints;
        this.onPaid = onPaid;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(title, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        for (int index = 0; index < hints.size() && index < ShopView.DEFAULT.length; index++) {
            HuntingHints.Hint hint = hints.get(index);
            layout.slot(ShopView.DEFAULT[index], icon(hint), (click, c) -> buy((SkyBlockPlayer) c.player(), hint));
        }
    }

    private void buy(SkyBlockPlayer player, HuntingHints.Hint hint) {
        ShopPrice price = new CoinShopPrice(hint.coins());
        if (!price.canAfford(player)) {
            player.sendMessage("<c>You don't have enough {}!", price.getNamePlural());
            return;
        }
        price.processPurchase(player);
        player.playSuccessSound();
        player.closeInventory();
        onPaid.accept(player, hint);
    }

    private static ItemStack.Builder icon(HuntingHints.Hint hint) {
        AttributeDefinition definition = AttributeRegistry.findByShard(hint.shard()).orElse(null);
        ItemStack.Builder builder = definition == null
                ? ItemStacks.item(Material.PAPER, 1, "")
                : AttributeShardComponent.create(definition, 1).getItemStackBuilder();
        ItemStacks.name(builder, Text.of("<a>Catching {}", hint.plural()));
        return ItemStacks.lore(builder, List.of(
                Text.of("<7>I will teach you how to find and"),
                Text.of("<7>catch {}, for a price.", hint.plural()),
                Text.empty(),
                Text.of("<7>Cost"),
                Text.of("<6>{} Coins", StringUtility.commaify(hint.coins())),
                Text.empty(),
                Text.of("<e>Click to pay!")));
    }
}
