package net.swofty.type.skyblockgeneric.shop.catalogue;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.shop.type.CoinShopPrice;

import java.util.ArrayList;
import java.util.List;

public class CatalogueShopView extends ShopView {
    private final ShopCatalogue catalogue;

    public CatalogueShopView(String catalogueId) {
        this(ShopCatalogue.get(catalogueId));
    }

    public CatalogueShopView(ShopCatalogue catalogue) {
        super(Text.of(catalogue.title()), catalogue.slots());
        this.catalogue = catalogue;
    }

    @Override
    public void initializeShopItems() {
        catalogue.entries().forEach(entry -> attachItem(toShopItem(entry)));
    }

    private ShopItem toShopItem(CatalogueEntry entry) {
        if (!entry.available()) {
            return new UnavailableItem(entry);
        }
        SkyBlockItem item = new SkyBlockItem(entry.itemType());
        return entry.stackable()
                ? ShopItem.Stackable(item, entry.amount(), entry.cost().toShopPrice())
                : ShopItem.Single(item, entry.amount(), entry.cost().toShopPrice());
    }

    @Override
    protected ItemStack.Builder renderItem(ShopItem item, int index, HypixelPlayer player) {
        if (!(item instanceof UnavailableItem unavailable)) {
            return super.renderItem(item, index, player);
        }
        CatalogueEntry entry = unavailable.entry;
        List<Text> lore = new ArrayList<>();
        entry.lore().forEach(line -> lore.add(Text.of(line)));
        if (!lore.isEmpty()) lore.add(Text.empty());
        lore.add(Text.of("<7>Cost"));
        lore.addAll(entry.cost().display());
        lore.add(Text.empty());
        lore.addAll(Text.of(entry.reason()).lines());
        return ItemStacks.item(entry.material(), entry.amount(), Text.of(entry.name()), lore);
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, ShopItem item, int index) {
        if (item instanceof UnavailableItem unavailable) {
            ctx.player().sendMessage(Text.of(unavailable.entry.reason()));
            return;
        }
        super.onItemClick(click, ctx, item, index);
    }

    private static final class UnavailableItem extends ShopItem {
        private final CatalogueEntry entry;

        private UnavailableItem(CatalogueEntry entry) {
            super(new SkyBlockItem(entry.material()), entry.amount(), new CoinShopPrice(entry.cost().coins()), false);
            this.entry = entry;
        }
    }
}
