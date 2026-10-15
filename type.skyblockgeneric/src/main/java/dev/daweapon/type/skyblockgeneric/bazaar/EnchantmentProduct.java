package dev.daweapon.type.skyblockgeneric.bazaar;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// One enchantment book at one level, e.g. ENCHANTMENT_ULTIMATE_WISE_3 on the real Bazaar.
public final class EnchantmentProduct implements BazaarProduct {
    public static final String ID_PREFIX = "ENCHANTMENT_";
    private static final int INVENTORY_SLOTS = 36;

    private final String neuId;
    private final CatalogueEntry entry;
    private final int level;

    EnchantmentProduct(String neuId, CatalogueEntry entry, int level) {
        this.neuId = neuId;
        this.entry = entry;
        this.level = level;
    }

    public CatalogueEntry enchantment() {
        return entry;
    }

    public int level() {
        return level;
    }

    public boolean ultimate() {
        return entry.ultimate();
    }

    @Override
    public String id() {
        return ID_PREFIX + neuId + "_" + level;
    }

    @Override
    public String name() {
        return entry.name() + " " + StringUtility.getAsRomanNumeral(level);
    }

    @Override
    public Rarity rarity() {
        return Rarity.COMMON;
    }

    @Override
    public Material material() {
        return ItemType.ENCHANTED_BOOK.material;
    }

    @Override
    public ItemType iconType() {
        return ItemType.ENCHANTED_BOOK;
    }

    @Override
    public @Nullable String headTexture() {
        return null;
    }

    @Override
    public SkyBlockItem item() {
        SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.valueOf(entry.key()), level));
        return book;
    }

    @Override
    public @Nullable ItemType itemType() {
        return null;
    }

    public boolean matches(ItemStack stack) {
        if (stack.isAir()) {
            return false;
        }
        SkyBlockItem candidate = new SkyBlockItem(stack);
        if (candidate.getAttributeHandler().getPotentialType() != ItemType.ENCHANTED_BOOK) {
            return false;
        }
        List<SkyBlockEnchantment> enchantments = candidate.getAttributeHandler().getEnchantments().toList();
        return enchantments.size() == 1
                && enchantments.getFirst().type().name().equals(entry.key())
                && enchantments.getFirst().level() == level;
    }

    @Override
    public int held(SkyBlockPlayer player) {
        int total = 0;
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (matches(stack)) {
                total += stack.amount();
            }
        }
        return total;
    }

    @Override
    public int room(SkyBlockPlayer player) {
        int maxStack = material().maxStackSize();
        int fit = 0;
        for (int slot = 0; slot < INVENTORY_SLOTS; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) {
                fit += maxStack;
            } else if (matches(stack)) {
                fit += maxStack - stack.amount();
            }
        }
        return fit;
    }

    @Override
    public @Nullable Runnable take(SkyBlockPlayer player, int amount) {
        if (held(player) < amount) {
            return null;
        }
        List<SkyBlockItem> taken = new ArrayList<>();
        int left = amount;
        for (int slot = 0; slot < INVENTORY_SLOTS && left > 0; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (!matches(stack)) {
                continue;
            }
            int removed = Math.min(left, stack.amount());
            SkyBlockItem copy = new SkyBlockItem(stack);
            copy.setAmount(removed);
            taken.add(copy);
            player.getInventory().setItemStack(slot, stack.consume(removed));
            left -= removed;
        }
        return () -> player.addAndUpdateItem(taken);
    }

    @Override
    public void give(SkyBlockPlayer player, int amount) {
        int maxStack = material().maxStackSize();
        for (int left = amount; left > 0; left -= maxStack) {
            SkyBlockItem book = item();
            book.setAmount(Math.min(left, maxStack));
            player.addAndUpdateItem(book);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EnchantmentProduct product && product.id().equals(id());
    }

    @Override
    public int hashCode() {
        return id().hashCode();
    }
}
