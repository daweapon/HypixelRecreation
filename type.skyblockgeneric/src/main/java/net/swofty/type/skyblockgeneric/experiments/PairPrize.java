package net.swofty.type.skyblockgeneric.experiments;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import org.jspecify.annotations.Nullable;

public record PairPrize(String key, Kind kind, Material icon, String name, @Nullable ItemType item, int amount, long xp,
                        @Nullable EnchantmentType enchantment, int level, @Nullable Rarity rarity) {
    public enum Kind { XP, ITEM, BOOK, PET, LAPIS_BLOCK, FEATHER, DIAMOND, SERUM }

    public boolean isPowerUp() {
        return kind == Kind.LAPIS_BLOCK || kind == Kind.FEATHER || kind == Kind.DIAMOND;
    }

    public static PairPrize xp(String key, Material icon, long xp) {
        return new PairPrize(key, Kind.XP, icon, "<d>+" + String.format("%,d", xp) + " Enchanting Exp", null, 0, xp, null, 0, null);
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

    public static PairPrize powerUp(Kind kind) {
        return switch (kind) {
            case LAPIS_BLOCK -> new PairPrize("POWER_LAPIS", kind, Material.LAPIS_BLOCK, "<9>Enchanted Lapis Block <7>(+1 click)", null, 0, 0, null, 0, null);
            case FEATHER -> new PairPrize("POWER_FEATHER", kind, Material.FEATHER, "<f>Feather <7>(+3 clicks)", null, 0, 0, null, 0, null);
            case DIAMOND -> new PairPrize("POWER_DIAMOND", kind, Material.DIAMOND, "<b>Diamond <7>(Instant Find)", null, 0, 0, null, 0, null);
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
        return net.swofty.commons.StringUtility.getAsRomanNumeral(level);
    }
}
