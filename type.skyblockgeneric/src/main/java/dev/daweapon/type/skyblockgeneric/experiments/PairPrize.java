package dev.daweapon.type.skyblockgeneric.experiments;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import org.jspecify.annotations.Nullable;

public record PairPrize(String key, Kind kind, Material icon, String name, @Nullable ItemType item, int amount, long xp,
                        @Nullable EnchantmentType enchantment, int level, @Nullable Rarity rarity) {
    public enum Kind { XP, ITEM, BOOK, PET, LAPIS_BLOCK, FEATHER, DIAMOND, SERUM }

    public boolean isPowerUp() {
        return kind == Kind.LAPIS_BLOCK || kind == Kind.FEATHER || kind == Kind.DIAMOND;
    }

    public static PairPrize xp(String key, Material icon, long xp) {
        return new PairPrize(key, Kind.XP, icon, "<b>" + (xp >= 1000 ? String.format("%dk", Math.round(xp / 1000D)) : String.valueOf(xp)) + " Enchanting Exp", null, 0, xp, null, 0, null);
    }

    public static PairPrize item(String key, Material icon, String name, ItemType item, int amount) {
        return new PairPrize(key, Kind.ITEM, icon, name, item, amount, 0, null, 0, null);
    }

    public static PairPrize book(EnchantmentType enchantment, int level) {
        return new PairPrize("BOOK_" + enchantment.name() + "_" + level, Kind.BOOK, Material.ENCHANTED_BOOK,
                "<9>" + enchantment.getName() + " " + roman(level), null, 1, 0, enchantment, level, null);
    }

    public static PairPrize pet(Rarity rarity) {
        return new PairPrize("PET_" + rarity.name(), Kind.PET, Material.PRISMARINE_SHARD,
                colorOf(rarity) + "Guardian Pet", ItemType.get("GUARDIAN_PET"), 1, 0, null, 0, rarity);
    }

    public static PairPrize lapis(long xp) {
        return new PairPrize("POWER_LAPIS", Kind.LAPIS_BLOCK, Material.LAPIS_BLOCK, "<b>+" + String.format("%,d", xp) + " XP",
                null, 0, xp, null, 0, null);
    }

    public static PairPrize powerUp(Kind kind) {
        return switch (kind) {
            case LAPIS_BLOCK -> lapis(0);
            case FEATHER -> new PairPrize("POWER_FEATHER", kind, Material.FEATHER, "<f>Feather", null, 0, 0, null, 0, null);
            case DIAMOND -> new PairPrize("POWER_DIAMOND", kind, Material.DIAMOND, "<b>Instant Find", null, 0, 0, null, 0, null);
            default -> throw new IllegalArgumentException(kind.name());
        };
    }

    private static String colorOf(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "<f>";
            case UNCOMMON -> "<a>";
            case RARE -> "<9>";
            case EPIC -> "<5>";
            case LEGENDARY -> "<6>";
            default -> "<d>";
        };
    }

    private static String roman(int level) {
        return dev.daweapon.commons.StringUtility.getAsRomanNumeral(level);
    }
}
