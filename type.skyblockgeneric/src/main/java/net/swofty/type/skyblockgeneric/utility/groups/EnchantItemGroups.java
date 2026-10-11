package net.swofty.type.skyblockgeneric.utility.groups;

import lombok.Getter;
import net.swofty.commons.StringUtility;

import java.util.Optional;

@Getter
public enum EnchantItemGroups {
    TOOLS("Tools"),
    PICKAXE("Mining Tools"),
    SWORD("Melee Weapon"),
    FISHING_WEAPON("Melee Weapon"),
    LONG_SWORD("Melee Weapon"),
    ARMOR("Armor"),
    FISHING_ROD("Tools"),
    GAUNTLET("Melee Weapon"),
    DRILL("Tools"),
    BOW("Bow"),
    HOE("Hoe"),
    AXE("Axe"),
    EQUIPMENT("Equipment"),
    HELMET("Helmet"),
    CHESTPLATE("Chestplate"),
    LEGGINGS("Leggings"),
    BOOTS("Boots"),
    SHOVEL("Shovel"),
    NECKLACE("Necklace"),
    CLOAK("Cloak"),
    BELT("Belt"),
    GLOVES("Gloves"),
    BRACELET("Bracelet"),
    ;

    private final String displayName;

    EnchantItemGroups(String displayName) {
        this.displayName = displayName;
    }

    public static Optional<EnchantItemGroups> fromLabel(String label) {
        return Optional.ofNullable(switch (label.trim().toLowerCase()) {
            case "sword" -> SWORD;
            case "longsword" -> LONG_SWORD;
            case "gauntlet" -> GAUNTLET;
            case "fishing weapon" -> FISHING_WEAPON;
            case "fishing rod" -> FISHING_ROD;
            case "bow" -> BOW;
            case "armor" -> ARMOR;
            case "helmet" -> HELMET;
            case "chestplate" -> CHESTPLATE;
            case "leggings" -> LEGGINGS;
            case "boots" -> BOOTS;
            case "pickaxe" -> PICKAXE;
            case "drill" -> DRILL;
            case "axe" -> AXE;
            case "hoe", "farming tool" -> HOE;
            case "shovel" -> SHOVEL;
            case "tools" -> TOOLS;
            case "necklace" -> NECKLACE;
            case "cloak" -> CLOAK;
            case "belt" -> BELT;
            case "gloves" -> GLOVES;
            case "bracelet" -> BRACELET;
            default -> null;
        });
    }

    @Override
    public String toString() {
        return StringUtility.toNormalCase(super.toString().toLowerCase());
    }
}
