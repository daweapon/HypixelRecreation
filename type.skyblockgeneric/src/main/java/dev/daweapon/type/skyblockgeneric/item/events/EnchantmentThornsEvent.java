package dev.daweapon.type.skyblockgeneric.item.events;

import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;

public class EnchantmentThornsEvent extends SkyBlockValueEvent {
    public static final double CHANCE_PERCENT = 50;
    public static final double REBOUND_PERCENT_PER_LEVEL = 3;

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamagedByMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number damage)) return;
        SkyBlockMob mob = ((PlayerDamagedByMobValueUpdateEvent) event).getMob();
        SkyBlockPlayer player = event.getPlayer();
        for (SkyBlockItem piece : player.getArmor()) {
            SkyBlockEnchantment thorns = piece.getAttributeHandler().getEnchantment(EnchantmentType.THORNS);
            if (thorns == null) continue;
            if (!LootRoll.roll(player.getUuid(), null, CHANCE_PERCENT)) continue;
            AttackService.applyHit(player, mob, (float) rebound(damage.doubleValue(), thorns.level()), false);
        }
    }

    public static double rebound(double damage, int level) {
        return damage * level * REBOUND_PERCENT_PER_LEVEL / 100D;
    }
}
