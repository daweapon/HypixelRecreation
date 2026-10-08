package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spider's and Tarantula's Web Battlefield: each mob kill adds a stack of +{0} Strength and +{1}
 * Magic Find for 40 seconds, up to 10, while the player stays within 20 blocks of the last kill.
 */
public final class WebBattlefieldHandler implements PetAbilityHandler {
    private static final long DURATION_MS = 40_000;
    private static final int MAX_STACKS = 10;
    private static final double RANGE = 20;
    private static final Map<UUID, Pos> LAST_KILL = new ConcurrentHashMap<>();

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        AbilityRuntime runtime = context.runtime();
        SkyBlockPlayer player = context.player();
        if (runtime == null || player == null || runtime.getStacks() <= 0) return ItemStatistics.empty();
        if (System.currentTimeMillis() >= runtime.getBuffUntil()) return ItemStatistics.empty();
        Pos where = LAST_KILL.get(player.getUuid());
        if (where == null || player.getPosition().distance(where) > RANGE) return ItemStatistics.empty();
        int stacks = runtime.getStacks();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.STRENGTH, context.value(0) * stacks)
                .withBase(ItemStatistic.MAGIC_FIND, context.value(1) * stacks)
                .build();
    }

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.Kill kill) || context.runtime() == null) return;
        AbilityRuntime runtime = context.runtime();
        long now = System.currentTimeMillis();
        int stacks = now >= runtime.getBuffUntil() ? 0 : runtime.getStacks();
        runtime.setStacks(Math.min(MAX_STACKS, stacks + 1));
        runtime.setBuffUntil(now + DURATION_MS);
        LAST_KILL.put(kill.player().getUuid(), kill.mob().getPosition());
    }
}
