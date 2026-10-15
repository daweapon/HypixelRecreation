package dev.daweapon.type.skyblockgeneric.item.components;

import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.ArrayList;
import java.util.List;

public class EnchantableComponent extends SkyBlockItemComponent {
    private final List<EnchantItemGroups> enchantGroups;
    private final boolean showEnchantLores;

    public EnchantableComponent(List<EnchantItemGroups> enchants, boolean showEnchantLores) {
        this.enchantGroups = enchants;
        this.showEnchantLores = showEnchantLores;
    }

    public List<EnchantItemGroups> getEnchantItemGroups() {
        return enchantGroups;
    }

    public boolean showEnchantLores() {
        return showEnchantLores;
    }

    public List<EnchantItemGroups> groupsFor(SkyBlockItem item) {
        return expand(enchantGroups, item.getMaterial().name(),
                item.getAttributeHandler().getPotentialType() == null ? "" : item.getAttributeHandler().getPotentialType().name());
    }

    public static List<EnchantItemGroups> expand(List<EnchantItemGroups> declared, String materialName, String itemName) {
        List<EnchantItemGroups> groups = new ArrayList<>(declared);
        if (declared.contains(EnchantItemGroups.ARMOR)) addPiece(groups, materialName, itemName);
        if (declared.contains(EnchantItemGroups.TOOLS)) {
            if (materialName.endsWith("_AXE")) groups.add(EnchantItemGroups.AXE);
            if (materialName.endsWith("_HOE")) groups.add(EnchantItemGroups.HOE);
            if (materialName.endsWith("_SHOVEL")) groups.add(EnchantItemGroups.SHOVEL);
            if (materialName.endsWith("_PICKAXE")) groups.add(EnchantItemGroups.PICKAXE);
        }
        if (declared.contains(EnchantItemGroups.EQUIPMENT)) addEquipment(groups, itemName);
        return groups.stream().distinct().toList();
    }

    private static void addPiece(List<EnchantItemGroups> groups, String materialName, String itemName) {
        if (materialName.endsWith("_HELMET") || materialName.equals("PLAYER_HEAD") || itemName.endsWith("HELMET")) groups.add(EnchantItemGroups.HELMET);
        if (materialName.endsWith("_CHESTPLATE") || itemName.endsWith("CHESTPLATE")) groups.add(EnchantItemGroups.CHESTPLATE);
        if (materialName.endsWith("_LEGGINGS") || itemName.endsWith("LEGGINGS")) groups.add(EnchantItemGroups.LEGGINGS);
        if (materialName.endsWith("_BOOTS") || itemName.endsWith("BOOTS")) groups.add(EnchantItemGroups.BOOTS);
    }

    private static void addEquipment(List<EnchantItemGroups> groups, String itemName) {
        List<EnchantItemGroups> pieces = List.of(EnchantItemGroups.NECKLACE, EnchantItemGroups.CLOAK,
                EnchantItemGroups.BELT, EnchantItemGroups.GLOVES, EnchantItemGroups.BRACELET);
        List<EnchantItemGroups> named = pieces.stream().filter(piece -> itemName.contains(piece.name())).toList();
        groups.addAll(named.isEmpty() ? pieces : named);
    }
}
