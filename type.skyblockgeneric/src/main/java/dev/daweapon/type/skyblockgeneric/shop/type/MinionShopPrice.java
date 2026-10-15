package dev.daweapon.type.skyblockgeneric.shop.type;

import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.MinionComponent;
import dev.daweapon.type.skyblockgeneric.shop.ShopPrice;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/** A price of one minion of an exact tier, such as the Ice Minion XI Einary takes for Ice Minion XII. */
public record MinionShopPrice(ItemType minion, int tier) implements ShopPrice {

    @Override
    public List<Text> getGUIDisplay() {
        return List.of(Text.of("<9>{} {}", minion.getDisplayName(), StringUtility.getAsRomanNumeral(tier)));
    }

    @Override
    public String getNamePlural() {
        return minion.getDisplayName() + " " + StringUtility.getAsRomanNumeral(tier) + "s";
    }

    @Override
    public boolean canAfford(SkyBlockPlayer player) {
        return findSlot(player) >= 0;
    }

    @Override
    public void processPurchase(SkyBlockPlayer player) {
        int slot = findSlot(player);
        if (slot < 0) return;
        ItemStack stack = player.getInventory().getItemStack(slot);
        player.getInventory().setItemStack(slot, stack.amount() > 1 ? stack.withAmount(stack.amount() - 1) : ItemStack.AIR);
    }

    @Override
    public ShopPrice multiply(int amount) {
        return this;
    }

    @Override
    public ShopPrice divide(double amount) {
        return this;
    }

    private int findSlot(SkyBlockPlayer player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
            SkyBlockItem item = new SkyBlockItem(stack);
            if (item.getAttributeHandler().getPotentialType() != minion || !item.hasComponent(MinionComponent.class)) continue;
            if (item.getAttributeHandler().getMinionData().tier() == tier) return slot;
        }
        return -1;
    }
}
