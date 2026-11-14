package dev.daweapon.type.bedwarsgame.shop.impl;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.bedwarsgame.shop.Currency;
import dev.daweapon.type.bedwarsgame.shop.ShopItem;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.collectibles.bedwars.BedWarsWoodSkinRuntimeService;
import dev.daweapon.type.generic.data.datapoints.DatapointBedWarsHotbar;

// Shop item for wood planks, giving the player's cosmetically-selected wood skin variant.
public class WoodShopItem extends ShopItem {

    public WoodShopItem() {
        super(
            "wood",
            "Wood",
            "Good block to defend your bed.\nStrong against pickaxes.",
            4,
            16,
            Currency.GOLD,
            Material.OAK_PLANKS,
            DatapointBedWarsHotbar.HotbarItemType.BLOCKS
        );
    }

    @Override
    public void onPurchase(BedWarsPlayer player) {
        Material selectedWood = BedWarsWoodSkinRuntimeService.resolveSelectedMaterial(player);
        giveItem(player, ItemStack.of(selectedWood).withAmount(getAmount()));
    }

    @Override
    public ItemStack getDisplay(BedWarsPlayer player) {
        Material selectedWood = BedWarsWoodSkinRuntimeService.resolveSelectedMaterial(player);
        return ItemStack.of(selectedWood);
    }
}
