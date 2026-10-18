package dev.daweapon.type.skyblockgeneric.item.equipment.passive;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.DamageIndicator;

public final class BlazingPalisade {
    private BlazingPalisade() {
    }

    public static boolean reflect(SkyBlockPlayer player, SkyBlockMob shooter, double damage) {
        double chance = EquipmentPassiveMath.reflectChancePercent(EquipmentPassives.worn(player));
        if (chance <= 0 || !LootRoll.roll(player.getUuid(), null, chance)) return false;
        if (!shooter.isDead() && !shooter.isRemoved() && shooter.getInstance() != null) {
            shooter.damage(new Damage(DamageType.PLAYER_ATTACK, player, player, player.getPosition(), (float) damage));
            new DamageIndicator().damage((float) damage).pos(shooter.getPosition()).critical(false).display(shooter.getInstance());
        }
        player.sendMessage("<7>Your Blazing Palisade reflected a <c>Fireball<7>!");
        return true;
    }
}
