package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.garden.GardenLayout;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestVacuum;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class RepugnantAromaHandler implements PetAbilityHandler {
    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null || player.getSkyblockDataHandler() == null || !PestVacuum.inGarden(player)) return ItemStatistics.empty();
        int plot = GardenLayout.plotAt(player.getPosition().x(), player.getPosition().z());
        if (plot == GardenLayout.NONE || Gardens.profile(player).pests().sprayOn(plot, System.currentTimeMillis()) == null) {
            return ItemStatistics.empty();
        }
        return ItemStatistics.builder().withBase(ItemStatistic.FARMING_FORTUNE, context.value(0)).build();
    }
}
