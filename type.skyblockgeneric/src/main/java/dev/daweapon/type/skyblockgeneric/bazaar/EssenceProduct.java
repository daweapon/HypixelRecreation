package dev.daweapon.type.skyblockgeneric.bazaar;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.EssenceGuideEntry;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

// Essence trades against the profile's essence balance: it is never an item in the inventory.
public final class EssenceProduct implements BazaarProduct {
    public static final String ID_PREFIX = "ESSENCE_";

    private final EssenceType type;

    EssenceProduct(EssenceType type) {
        this.type = type;
    }

    public EssenceType essence() {
        return type;
    }

    @Override
    public String id() {
        return ID_PREFIX + type.name();
    }

    @Override
    public String name() {
        return type.getFullName();
    }

    @Override
    public Rarity rarity() {
        return Rarity.COMMON;
    }

    @Override
    public Material material() {
        return Material.PLAYER_HEAD;
    }

    @Override
    public @Nullable ItemType iconType() {
        return null;
    }

    @Override
    public String headTexture() {
        return EssenceGuideEntry.of(type).texture();
    }

    @Override
    public @Nullable SkyBlockItem item() {
        return null;
    }

    @Override
    public @Nullable ItemType itemType() {
        return null;
    }

    @Override
    public int held(SkyBlockPlayer player) {
        return heldAmount(Essences.get(player, type));
    }

    @Override
    public int room(SkyBlockPlayer player) {
        return roomFor(Essences.get(player, type));
    }

    static int heldAmount(long balance) {
        return (int) Math.clamp(balance, 0, Integer.MAX_VALUE);
    }

    static int roomFor(long balance) {
        return (int) Math.clamp(EssenceType.CAP - balance, 0, Integer.MAX_VALUE);
    }

    @Override
    public @Nullable Runnable take(SkyBlockPlayer player, int amount) {
        if (!Essences.take(player, type, amount)) {
            return null;
        }
        return () -> Essences.add(player, type, amount);
    }

    @Override
    public void give(SkyBlockPlayer player, int amount) {
        Essences.add(player, type, amount);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EssenceProduct product && product.type == type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
