package dev.daweapon.type.skyblockgeneric.item.events;

import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.ValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityEffects;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityMath;
import dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityState;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class ArmorAbilityDamageTakenEvent extends SkyBlockValueEvent {
    public static final int MITHRIL_REGENERATION_TICKS = 100;

    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamagedByMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent event) {
        if (!(event.getValue() instanceof Number number)) return;
        SkyBlockPlayer player = event.getPlayer();
        if (player.getSkyblockDataHandler() == null) return;
        ArmorAbilityState.touchCombat(player);
        SkyBlockMob mob = ((PlayerDamagedByMobValueUpdateEvent) event).getMob();
        List<String> worn = WornArmor.ids(player);
        double damage = number.doubleValue();
        boolean animal = mob.getMobTypes().contains(MobType.ANIMAL);
        if (animal && WornArmor.wearsAll(worn, ArmorAbilityEffects.MASTIFF)) {
            damage = ArmorAbilityMath.reducedByPercent(damage, ArmorAbilityMath.MASTIFF_ANIMAL_REDUCTION);
        }
        if (animal) damage *= animalDefenseMultiplier(player, worn);
        if (worn.contains("MITHRIL_COAT")) {
            double maxHealth = player.getMaxHealth();
            if (ArmorAbilityMath.mithrilProtectionTriggers(damage, maxHealth)) {
                player.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 0, MITHRIL_REGENERATION_TICKS));
            }
            damage = ArmorAbilityMath.mithrilProtection(damage, maxHealth);
        }
        event.setValue(number instanceof Float ? (Object) (float) damage : (Object) damage);
    }

    private static double animalDefenseMultiplier(SkyBlockPlayer player, List<String> worn) {
        double extra = 0;
        for (String id : worn) {
            if (!ArmorAbilityEffects.PACK.contains(id)) continue;
            extra += id.equals("CHESTPLATE_OF_THE_PACK")
                    ? ArmorAbilityMath.PACK_CHESTPLATE_ANIMAL_DEFENSE : ArmorAbilityMath.PACK_PIECE_ANIMAL_DEFENSE;
        }
        if (extra == 0) return 1;
        return ArmorAbilityMath.extraDefenseMultiplier(
                player.getStatistics().allStatistics().getOverall(ItemStatistic.DEFENSE), extra);
    }
}
