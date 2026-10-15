package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities;

import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Bee's Hive: values {0} Intelligence, {1} Strength and {2} Defense for each other player within
 * 25 blocks, up to 15 players.
 */
public final class HiveHandler implements PetAbilityHandler {
    private static final double RANGE = 25;
    private static final int MAX_PLAYERS = 15;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null) return ItemStatistics.empty();
        Instance instance = player.getInstance();
        if (instance == null) return ItemStatistics.empty();
        int nearby = (int) Math.min(MAX_PLAYERS, instance.getPlayers().stream()
                .filter(other -> other != player && other.getPosition().distance(player.getPosition()) <= RANGE)
                .count());
        if (nearby == 0) return ItemStatistics.empty();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.INTELLIGENCE, context.value(0) * nearby)
                .withBase(ItemStatistic.STRENGTH, context.value(1) * nearby)
                .withBase(ItemStatistic.DEFENSE, context.value(2) * nearby)
                .build();
    }
}
