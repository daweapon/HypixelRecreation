package dev.daweapon.type.skyblockgeneric.farming.jake;

import dev.daweapon.type.skyblockgeneric.farming.animal.FarmAnimal;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingNpcCatalogueView;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;

public class JakeShopView extends CatalogueShopView {
    public JakeShopView() {
        super("jake");
    }

    @Override
    protected ShopItem decorate(CatalogueEntry entry, ShopItem item, SkyBlockPlayer player) {
        if (HuntingNpcCatalogueView.needsConfirmation(entry.cost().coins())) item.withConfirmation();
        String animal = Arrays.stream(FarmAnimal.values())
                .filter(candidate -> candidate.unlocks().equals(entry.itemId()))
                .map(FarmAnimal::displayName)
                .findFirst().orElse("animal");
        item.lockedWhen(viewer -> JakeShop.isOpen(entry.itemId()) ? null : "<c>Bring Jake a <e>" + animal + "<c> first!");
        return item;
    }
}
