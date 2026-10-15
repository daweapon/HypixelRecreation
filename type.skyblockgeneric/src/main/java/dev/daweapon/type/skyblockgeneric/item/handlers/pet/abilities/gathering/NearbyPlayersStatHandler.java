package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Grants value {i} of the i-th statistic for each other player within range (optionally only
 * players with a given pet summoned), up to a cap.
 */
public final class NearbyPlayersStatHandler implements PetAbilityHandler {
    public static final double WHOLE_ISLAND = Double.MAX_VALUE;

    private final double range;
    private final int cap;
    private final @Nullable ItemType requiredPet;
    private final ItemStatistic[] statistics;

    public NearbyPlayersStatHandler(double range, int cap, @Nullable ItemType requiredPet, ItemStatistic... statistics) {
        this.range = range;
        this.cap = cap;
        this.requiredPet = requiredPet;
        this.statistics = statistics;
    }

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null || player.getInstance() == null) return ItemStatistics.empty();
        int nearby = Math.min(cap, count(player, player.getInstance()));
        if (nearby == 0) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (int i = 0; i < statistics.length; i++) {
            double value = context.value(i) * nearby;
            if (value != 0) builder.withBase(statistics[i], value);
        }
        return builder.build();
    }

    private int count(SkyBlockPlayer player, Instance instance) {
        int count = 0;
        for (Player other : instance.getPlayers()) {
            if (other == player || !(other instanceof SkyBlockPlayer skyBlockPlayer)) continue;
            if (range != WHOLE_ISLAND && other.getPosition().distance(player.getPosition()) > range) continue;
            if (requiredPet != null && !hasSummoned(skyBlockPlayer)) continue;
            count++;
        }
        return count;
    }

    private boolean hasSummoned(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return false;
        SkyBlockItem summoned = player.getPetData().getEnabledPet();
        return summoned != null && summoned.getAttributeHandler().getPotentialType() == requiredPet;
    }
}
