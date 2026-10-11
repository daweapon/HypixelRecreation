package net.swofty.type.skyblockgeneric.item.events;

import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;

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
