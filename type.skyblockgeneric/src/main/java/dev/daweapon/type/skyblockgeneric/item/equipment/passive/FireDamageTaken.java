package dev.daweapon.type.skyblockgeneric.item.equipment.passive;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FireDamageTaken {
    private static final Map<UUID, Double> LAST_FIRE_DAMAGE = new ConcurrentHashMap<>();

    private FireDamageTaken() {
    }

    public static boolean isFire(Damage damage) {
        DamageType type = damage.getType().asValue();
        return type == DamageType.ON_FIRE.asValue() || type == DamageType.IN_FIRE.asValue()
                || type == DamageType.LAVA.asValue() || type == DamageType.HOT_FLOOR.asValue()
                || type == DamageType.FIREBALL.asValue() || type == DamageType.UNATTRIBUTED_FIREBALL.asValue();
    }

    public static double multiplier(SkyBlockPlayer player) {
        Set<String> worn = EquipmentPassives.worn(player);
        double multiplier = 1;
        if (worn.contains(EquipmentPassiveMath.DELIRIUM_NECKLACE)) {
            double strength = player.getStatistics().allStatistics().getOverall(ItemStatistic.STRENGTH);
            multiplier *= EquipmentPassiveMath.deliriumMultiplier(strength);
        }
        if (worn.contains(EquipmentPassiveMath.SCOURGE_CLOAK)) multiplier *= EquipmentPassiveMath.scourgeFireMultiplier();
        return multiplier;
    }

    public static void record(SkyBlockPlayer player, double amount) {
        if (amount > 0 && EquipmentPassives.wearing(player, EquipmentPassiveMath.SCOURGE_CLOAK)) {
            LAST_FIRE_DAMAGE.put(player.getUuid(), amount);
        }
    }

    public static double consumeNextHitBonus(SkyBlockPlayer player) {
        Double last = LAST_FIRE_DAMAGE.remove(player.getUuid());
        return last == null ? 0 : EquipmentPassiveMath.scourgeNextHitBonus(last);
    }
}
