package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Wolf's Pack Leader: +{0} Crit Damage for every wolf monster nearby, up to 10.
 */
public final class PackLeaderHandler implements PetAbilityHandler {
    private static final double RANGE = 10;
    private static final int MAX_WOLVES = 10;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        Instance instance = player == null ? null : player.getInstance();
        if (instance == null) return ItemStatistics.empty();
        int wolves = 0;
        for (Entity entity : instance.getNearbyEntities(player.getPosition(), RANGE)) {
            if (entity instanceof SkyBlockMob mob && mob.getEntityType() == EntityType.WOLF && !mob.isDead()) wolves++;
            if (wolves >= MAX_WOLVES) break;
        }
        if (wolves == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.CRITICAL_DAMAGE, context.value(0) * wolves).build();
    }
}
