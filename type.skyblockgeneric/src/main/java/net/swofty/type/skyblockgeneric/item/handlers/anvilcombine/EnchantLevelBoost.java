package net.swofty.type.skyblockgeneric.item.handlers.anvilcombine;

import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public enum EnchantLevelBoost {
    GOLDEN_BOUNTY(List.of(EnchantmentType.SCAVENGER), 5),
    ENDSTONE_IDOL(List.of(EnchantmentType.ENDER_SLAYER), 6),
    ENSNARED_SNAIL(List.of(EnchantmentType.BANE_OF_ARTHROPODS), 6),
    DISTANT_ECHO(List.of(EnchantmentType.STEALTH), 5),
    ENCHANTED_TURBO_GOURD(Stream.of(EnchantmentType.values()).filter(type -> type.name().startsWith("TURBO_")).toList(), 6),
    TURBO_GOURD(Stream.of(EnchantmentType.values()).filter(type -> type.name().startsWith("TURBO_")).toList(), 5),
    TROUBLED_BUBBLE(List.of(EnchantmentType.PISCARY), 6),
    VIBRANT_CORAL(List.of(EnchantmentType.SCUBA), 5),
    WATER_HYACINTH(List.of(EnchantmentType.FOREST_PLEDGE), 5);

    private final List<EnchantmentType> targets;
    private final int requiredLevel;

    EnchantLevelBoost(List<EnchantmentType> targets, int requiredLevel) {
        this.targets = targets;
        this.requiredLevel = requiredLevel;
    }

    public Map<EnchantmentType, Integer> boosted(Map<EnchantmentType, Integer> levels) {
        Map<EnchantmentType, Integer> result = new LinkedHashMap<>(levels);
        for (EnchantmentType target : targets) {
            Integer level = result.get(target);
            if (level != null && level == requiredLevel) result.put(target, requiredLevel + 1);
        }
        return result;
    }

    public boolean appliesTo(Map<EnchantmentType, Integer> levels) {
        return !boosted(levels).equals(levels);
    }

    public static void registerAnvilCombines() {
        for (EnchantLevelBoost boost : values()) {
            AnvilCombineRegistry.register(boost.name(), new AnvilCombineHandler(
                    (upgradeItem, sacrificeItem) -> boost.apply(upgradeItem),
                    (player, upgradeItem, sacrificeItem) -> boost.appliesTo(levelsOf(upgradeItem)),
                    (upgradeItem, sacrificeItem, player) -> 0));
        }
    }

    private void apply(SkyBlockItem item) {
        Map<EnchantmentType, Integer> before = levelsOf(item);
        boosted(before).forEach((type, level) -> {
            if (before.get(type).equals(level)) return;
            item.getAttributeHandler().removeEnchantment(type);
            item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(type, level));
        });
    }

    private static Map<EnchantmentType, Integer> levelsOf(SkyBlockItem item) {
        Map<EnchantmentType, Integer> levels = new LinkedHashMap<>();
        item.getAttributeHandler().getEnchantments().forEach(enchantment -> levels.put(enchantment.type(), enchantment.level()));
        return levels;
    }
}
