package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Turtle's Turtle Tactics: +{0}% Defense, and another +10% while standing still.
 */
public final class TurtleTacticsHandler implements PetAbilityHandler {
    private static final double STILL_BONUS = 10;
    private static final long STILL_MS = 1_000;
    private static final double MOVED = 0.05;
    private static final Map<UUID, Still> LAST_SEEN = new ConcurrentHashMap<>();

    private record Still(Pos position, long since) {
    }

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        double percent = context.value(0);
        SkyBlockPlayer player = context.player();
        if (player != null && standingStill(player)) percent += STILL_BONUS;
        if (percent <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withAdditivePercentage(ItemStatistic.DEFENSE, percent).build();
    }

    private static boolean standingStill(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        Pos position = player.getPosition();
        Still last = LAST_SEEN.get(player.getUuid());
        if (last == null || last.position().distance(position) > MOVED) {
            LAST_SEEN.put(player.getUuid(), new Still(position, now));
            return false;
        }
        return now - last.since() >= STILL_MS;
    }
}
