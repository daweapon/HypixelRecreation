package dev.daweapon.type.skyblockgeneric.shop.catalogue;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.context.ClickContext;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.garden.quest.GardenQuests;
import dev.daweapon.type.skyblockgeneric.gui.ShopView;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.shop.type.CoinShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * A shop drawn from {@code configuration/skyblock/shops/<id>.yml}. Prices and reputation locks are
 * resolved for the viewing player each time an entry is drawn or clicked, so per-player discounts
 * ({@code priceModifier}) and {@code requires} gates stay current.
 */
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
        catalogue.entries().forEach(entry -> attachItem(new CatalogueItem(entry)));
    }

    private ShopItem forPlayer(CatalogueEntry entry, SkyBlockPlayer player) {
        double multiplier = CataloguePriceModifiers.coinMultiplier(catalogue.priceModifier(), player);
        SkyBlockItem item = entry.buildItem();
        ShopItem shopItem = entry.stackable()
                ? ShopItem.Stackable(item, entry.amount(), entry.cost().toShopPrice(multiplier))
                : ShopItem.Single(item, entry.amount(), entry.cost().toShopPrice(multiplier));
        if (entry.itemId().equals("BIOFUEL")) shopItem.onPurchase(GardenQuests::fuelPurchased);
        ReputationRequirement requirement = entry.requirement();
        if (requirement != null) shopItem.lockedWhen(viewer -> requirement.isMet(viewer) ? null : requirement.lockLine());
        return shopItem;
    }

    @Override
    protected ItemStack.Builder renderItem(ShopItem item, int index, HypixelPlayer player) {
        CatalogueEntry entry = ((CatalogueItem) item).entry;
        if (entry.available()) {
            return super.renderItem(forPlayer(entry, (SkyBlockPlayer) player), index, player);
        }
        List<Text> lore = new ArrayList<>();
        entry.lore().forEach(line -> lore.add(Text.of(line)));
        if (!lore.isEmpty()) lore.add(Text.empty());
        lore.add(Text.of("<7>Cost"));
        lore.addAll(entry.cost().display());
        lore.add(Text.empty());
        if (entry.requirement() != null) lore.add(Text.of(entry.requirement().lockLine()));
        lore.addAll(Text.of(entry.reason()).lines());
        ItemStack.Builder builder = ItemStacks.item(entry.material(), entry.amount(), Text.of(entry.name()), lore);
        dev.daweapon.commons.skyblock.item.ItemType type = entry.itemType();
        if (type != null) {
            String model = new SkyBlockItem(type).getItemStack().get(net.minestom.server.component.DataComponents.ITEM_MODEL);
            if (model != null) builder.set(net.minestom.server.component.DataComponents.ITEM_MODEL, model);
        }
        return builder;
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, ShopItem item, int index) {
        CatalogueEntry entry = ((CatalogueItem) item).entry;
        if (!entry.available()) {
            ctx.player().sendMessage(Text.of(entry.reason()));
            return;
        }
        super.onItemClick(click, ctx, forPlayer(entry, (SkyBlockPlayer) ctx.player()), index);
    }

    private static final class CatalogueItem extends ShopItem {
        private final CatalogueEntry entry;

        private CatalogueItem(CatalogueEntry entry) {
            super(new SkyBlockItem(entry.material()), entry.amount(), new CoinShopPrice(entry.cost().coins()), entry.stackable());
            this.entry = entry;
        }
    }
}
