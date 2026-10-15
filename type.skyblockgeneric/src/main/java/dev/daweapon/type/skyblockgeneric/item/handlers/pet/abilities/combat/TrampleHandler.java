package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;

/**
 * Horse and Skeleton Horse Trample: a fall of 20 or more blocks is absorbed and hits mobs near the landing for
 * {0}% of the player's Damage per block fallen.
 */
public final class TrampleHandler implements PetAbilityHandler {
    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.FallDamage fall) || !PetPerkMath.tramples(fall.distance())) return;
        fall.damage(0);
        SkyBlockPlayer player = fall.player();
        Instance instance = player.getInstance();
        if (instance == null) return;
        double weaponDamage = player.getStatistics().allStatistics().getOverall(ItemStatistic.DAMAGE);
        float damage = (float) PetPerkMath.trampleDamage(weaponDamage, context.value(0), fall.distance());
        if (damage <= 0) return;
        for (Entity entity : instance.getNearbyEntities(player.getPosition(), PetPerkMath.TRAMPLE_RADIUS)) {
            if (entity instanceof SkyBlockMob mob) AttackService.applyHit(player, mob, damage, false);
        }
    }
}
