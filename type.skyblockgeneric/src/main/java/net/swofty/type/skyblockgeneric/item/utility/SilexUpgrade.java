package net.swofty.type.skyblockgeneric.item.utility;

import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.DrillComponent;
import net.swofty.type.skyblockgeneric.item.components.PickaxeComponent;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;

public final class SilexUpgrade {
    public static final String HANDLER_ID = "SIL_EX";
    public static final int MIN_LEVEL = 5;
    public static final int MAX_LEVEL = 10;

    private SilexUpgrade() {
    }

    public static boolean canUpgrade(int efficiencyLevel) {
        return efficiencyLevel >= MIN_LEVEL && efficiencyLevel < MAX_LEVEL;
    }

    public static void registerAnvilCombine() {
        AnvilCombineRegistry.register(HANDLER_ID, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> upgradeItem.getAttributeHandler().addEnchantment(
                        new SkyBlockEnchantment(EnchantmentType.EFFICIENCY, efficiency(upgradeItem) + 1)),
                (player, upgradeItem, sacrificeItem) -> isMiningTool(upgradeItem) && canUpgrade(efficiency(upgradeItem)),
                (upgradeItem, sacrificeItem, player) -> 0));
    }

    private static boolean isMiningTool(SkyBlockItem item) {
        return item.hasComponent(PickaxeComponent.class) || item.hasComponent(DrillComponent.class);
    }

    private static int efficiency(SkyBlockItem item) {
        SkyBlockEnchantment enchantment = item.getAttributeHandler().getEnchantment(EnchantmentType.EFFICIENCY);
        return enchantment == null ? 0 : enchantment.level();
    }
}
