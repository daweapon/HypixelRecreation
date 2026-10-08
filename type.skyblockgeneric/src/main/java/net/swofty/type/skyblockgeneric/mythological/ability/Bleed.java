package net.swofty.type.skyblockgeneric.mythological.ability;

import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.MobAttackService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Minotaur's Bleed, also used by the Sphinx: stacks build per player, the creature strikes for
 * {@code damagePerStack} times the stacks every two seconds, healing is reduced while stacks remain,
 * and stacks vanish after five seconds without a new one.
 */
public final class Bleed {
    private static final int STRIKE_INTERVAL_TICKS = 40;
    private static final long RESET_AFTER_MS = 5000;
    private static final double HEALING_REDUCTION_PER_STACK = 0.03;
    private static final double MIN_HEALING = 0.1;

    private final String source;
    private final Map<UUID, Integer> stacks = new HashMap<>();
    private final Map<UUID, Long> lastIncrease = new HashMap<>();
    private double damagePerStack;

    public Bleed(String source, double damagePerStack) {
        this.source = source;
        this.damagePerStack = damagePerStack;
    }

    public static String label(int stacks) {
        return "<c>☣ <f>Bleeds: <c>" + stacks;
    }

    public void setDamagePerStack(double damagePerStack) {
        this.damagePerStack = damagePerStack;
    }

    public void add(SkyBlockPlayer player, int amount) {
        int total = stacks.merge(player.getUuid(), amount, Integer::sum);
        lastIncrease.put(player.getUuid(), System.currentTimeMillis());
        double healing = Math.max(MIN_HEALING, 1 - total * HEALING_REDUCTION_PER_STACK);
        player.setHealingModifier(source, new SkyBlockPlayer.HealingModifier(healing, null));
    }

    public int stacks(SkyBlockPlayer player) {
        return stacks.getOrDefault(player.getUuid(), 0);
    }

    public int highest() {
        return stacks.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public void tick(MythologicalCreatureMob mob, int ticksAlive) {
        long now = System.currentTimeMillis();
        for (UUID id : Map.copyOf(lastIncrease).keySet()) {
            if (now - lastIncrease.get(id) >= RESET_AFTER_MS) clear(id);
        }
        if (ticksAlive % STRIKE_INTERVAL_TICKS != 0) return;
        stacks.forEach((id, count) -> {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(id);
            if (player != null && player.getInstance() == mob.getInstance()) {
                MobAttackService.trueStrike(mob, player, damagePerStack * count);
            }
        });
    }

    public void clearAll() {
        Map.copyOf(stacks).keySet().forEach(this::clear);
    }

    private void clear(UUID id) {
        stacks.remove(id);
        lastIncrease.remove(id);
        SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(id);
        if (player != null) player.removeHealingModifier(source);
    }
}
