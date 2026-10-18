package dev.daweapon.type.skyblockgeneric.item.equipment.passive;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.DamageIndicator;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Contagion {
    private static final int MAX_TRACKED = 2_048;
    private static final Set<UUID> CONTAMINATED = ConcurrentHashMap.newKeySet();

    private Contagion() {
    }

    public static void onKill(SkyBlockPlayer killer, SkyBlockMob dead) {
        CONTAMINATED.remove(dead.getUuid());
        if (!EquipmentPassives.wearing(killer, EquipmentPassiveMath.GAUNTLET_OF_CONTAGION)) return;
        Instance instance = killer.getInstance();
        double damage = EquipmentPassiveMath.contagionDamage(dead.getMaxHealth());
        if (instance == null || damage <= 0) return;
        Pos center = dead.getPosition();
        instance.sendGroupedPacket(new ParticlePacket(Particle.EXPLOSION, false, false,
                center.x(), center.y() + 0.5, center.z(), 0.5f, 0.5f, 0.5f, 0.02f, 8));
        if (CONTAMINATED.size() > MAX_TRACKED) CONTAMINATED.clear();
        for (Entity entity : instance.getNearbyEntities(center, EquipmentPassiveMath.CONTAGION_RADIUS)) {
            if (!(entity instanceof SkyBlockMob mob) || mob == dead || mob.isDead() || mob.isRemoved()) continue;
            if (mob.getPosition().distance(center) > EquipmentPassiveMath.CONTAGION_RADIUS) continue;
            CONTAMINATED.add(mob.getUuid());
            mob.damage(new Damage(DamageType.PLAYER_EXPLOSION, killer, killer, center, (float) damage));
            new DamageIndicator().damage((float) damage).pos(mob.getPosition()).critical(false).display(instance);
        }
    }
}
