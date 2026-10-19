package dev.daweapon.type.theend.npcs;

import java.util.Collection;
import java.util.List;

public final class EnderArmorSet {
    public static final List<String> ARMOR = List.of("END_HELMET", "END_CHESTPLATE", "END_LEGGINGS", "END_BOOTS");
    public static final List<String> ACCESSORIES = List.of("ENDER_CLOAK", "ENDER_BELT", "ENDER_NECKLACE", "ENDER_GAUNTLET");

    private EnderArmorSet() {
    }

    public static List<String> missing(Collection<String> wornArmor, Collection<String> ownedAccessories) {
        return java.util.stream.Stream.concat(
                ARMOR.stream().filter(id -> !wornArmor.contains(id)),
                ACCESSORIES.stream().filter(id -> !ownedAccessories.contains(id))
        ).toList();
    }
}
