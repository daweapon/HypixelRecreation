package net.swofty.type.hub.gui;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.travelingzoo.TravelingZooManager;

// Oringo's shop GUI, opened during the Traveling Zoo event - sells this visit's 3 pets
// (1 guaranteed Legendary + 2 random Common-Epic), see TravelingZooManager for the real-Hypixel
// stocking rules this pulls from.
public class GUITravelingZoo extends ShopView {

    public GUITravelingZoo() {
        super("Traveling Zoo", DEFAULT);
    }

    @Override
    public void initializeShopItems() {
        for (TravelingZooManager.PetOffer offer : TravelingZooManager.getCurrentOffers()) {
            SkyBlockItem item = new SkyBlockItem(offer.petType());
            item.getAttributeHandler().setRarity(offer.rarity());
            ShopItem shopItem = ShopItem.Single(item, 1, offer.price());
            attachItem(shopItem);
        }
    }
}
