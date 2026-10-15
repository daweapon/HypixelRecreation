package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.FallDamageValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class EnchantmentFeatherFallingEvent extends SkyBlockValueEvent {
    public static final double REDUCTION_PERCENT_PER_LEVEL = 5;

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return FallDamageValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        int level = levelOf(event.getPlayer());
        if (level <= 0) return;
        event.setValue((float) reduce(damage.doubleValue(), level));
    }

    public static double reduce(double damage, int level) {
        return AccessoryFallDamageEvent.reduce(damage, level, level * REDUCTION_PERCENT_PER_LEVEL);
    }

    private static int levelOf(SkyBlockPlayer player) {
        SkyBlockEnchantment enchantment = new SkyBlockItem(player.getBoots()).getAttributeHandler()
                .getEnchantment(EnchantmentType.FEATHER_FALLING);
        return enchantment == null ? 0 : enchantment.level();
    }
}
