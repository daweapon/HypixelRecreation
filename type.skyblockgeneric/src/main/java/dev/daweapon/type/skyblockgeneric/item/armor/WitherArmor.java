package dev.daweapon.type.skyblockgeneric.item.armor;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * The five Floor VII Wither Armor sets (Wither, Maxor's, Storm's, Goldor's, Necron's). Every piece cuts damage from
 * withers by 10%, each Maxor's piece adds 5% arrow damage, and any four pieces of the family, mixed or not, give the
 * Witherborn full set bonus (wiki set pages; mixing per the wiki's Wither Armor talk page).
 */
public final class WitherArmor {
    public static final double WITHER_REDUCTION_PER_PIECE = 10;
    public static final double MAXOR_ARROW_DAMAGE_PER_PIECE = 5;
    public static final int FULL_SET = 4;
    public static final String SET_BONUS_LINE = "Full Set Bonus: Witherborn";
    private static final Set<String> VARIANTS = Set.of("WITHER", "SPEED_WITHER", "WISE_WITHER", "TANK_WITHER", "POWER_WITHER");
    private static final Set<String> SLOTS = Set.of("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");
    private static final String MAXOR = "SPEED_WITHER";

    private WitherArmor() {
    }

    public static boolean isPiece(@Nullable String id) {
        if (id == null) return false;
        int split = id.lastIndexOf('_');
        return split > 0 && VARIANTS.contains(id.substring(0, split)) && SLOTS.contains(id.substring(split + 1));
    }

    public static boolean isMaxorPiece(@Nullable String id) {
        return isPiece(id) && id.startsWith(MAXOR + "_");
    }

    public static int pieces(List<String> worn) {
        return (int) worn.stream().filter(WitherArmor::isPiece).count();
    }

    public static int maxorPieces(List<String> worn) {
        return (int) worn.stream().filter(WitherArmor::isMaxorPiece).count();
    }

    public static boolean witherborn(List<String> worn) {
        return pieces(worn) >= FULL_SET;
    }

    public static double witherDamageMultiplier(int pieces) {
        return Math.max(0, 1 - pieces * WITHER_REDUCTION_PER_PIECE / 100D);
    }

    public static double arrowDamageMultiplier(int maxorPieces) {
        return 1 + maxorPieces * MAXOR_ARROW_DAMAGE_PER_PIECE / 100D;
    }

    /** The set bonus header with the viewer's worn count, as "(3/4)", in place of the catalogue's "(0/4)". */
    public static String withWornCount(String line, int worn) {
        if (!line.contains(SET_BONUS_LINE)) return line;
        return line.replace("(0/" + FULL_SET + ")", "(" + Math.min(worn, FULL_SET) + "/" + FULL_SET + ")");
    }
}
