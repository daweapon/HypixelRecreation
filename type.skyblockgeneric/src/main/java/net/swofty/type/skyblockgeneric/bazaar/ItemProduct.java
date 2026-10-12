package net.swofty.type.skyblockgeneric.bazaar;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ItemProduct implements BazaarProduct {
    private final ItemType type;

    ItemProduct(ItemType type) {
        this.type = type;
    }

    @Override
    public String id() {
        return type.name();
    }

    @Override
    public String name() {
        return BazaarNames.of(type);
    }

    @Override
    public Rarity rarity() {
        return type.rarity;
    }

    @Override
    public Material material() {
        return type.material;
    }

    @Override
    public ItemType iconType() {
        return type;
    }

    @Override
    public @Nullable String headTexture() {
        return null;
    }

    @Override
    public SkyBlockItem item() {
        return new SkyBlockItem(type);
    }

    @Override
    public ItemType itemType() {
        return type;
    }

    @Override
    public int held(SkyBlockPlayer player) {
        return player.getAmountInInventory(type);
    }

    @Override
    public int room(SkyBlockPlayer player) {
        return player.maxItemFit(type);
    }

    @Override
    public @Nullable Runnable take(SkyBlockPlayer player, int amount) {
        List<SkyBlockItem> taken = player.takeItem(type, amount);
        return taken == null ? null : () -> player.addAndUpdateItem(taken);
    }

    @Override
    public void give(SkyBlockPlayer player, int amount) {
        int maxStack = type.material.maxStackSize();
        for (int left = amount; left > 0; left -= maxStack) {
            SkyBlockItem item = new SkyBlockItem(type);
            item.setAmount(Math.min(left, maxStack));
            player.addAndUpdateItem(item);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemProduct product && product.type == type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
