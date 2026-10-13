package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.pest.PestVacuum;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
