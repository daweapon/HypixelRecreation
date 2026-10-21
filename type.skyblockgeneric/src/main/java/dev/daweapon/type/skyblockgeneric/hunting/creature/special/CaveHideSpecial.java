package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.FishingNetService;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

final class CaveHideSpecial implements CreatureSpecial {
    private static final int CAVES = 3;
    private static final double FALLBACK_RING = 14;
    private static final double ARRIVED_DISTANCE_SQUARED = 2.25;
    private static final long TRAVEL_LIMIT_TICKS = 160;
    private static final long STAY_TICKS = 200;
    private static final double DAMAGE_PER_PULL = 5;
    private static final double WEAKENING_HEALTH = 100;

    private final Map<HuntingCreatureMob, State> states = Collections.synchronizedMap(new WeakHashMap<>());

    private static final class State {
        final CaveHide hide = new CaveHide(TRAVEL_LIMIT_TICKS, STAY_TICKS);
        Pos cave;
        boolean weakeningSent;
    }

    @Override
    public boolean onNetHit(SkyBlockPlayer player, HuntingCreatureMob mob, CreatureSpec.NetTier tier, double distance) {
        State state = states.computeIfAbsent(mob, key -> new State());
        switch (state.hide.phase()) {
            case OPEN -> {
                state.cave = pickCave(mob);
                state.hide.startHiding(mob.getAliveTicks());
                mob.getVanillaNavigator().pathTo(state.cave);
            }
            case IN_CAVE -> damage(player, mob, tier, state);
            default -> {
            }
        }
        return true;
    }

    private void damage(SkyBlockPlayer player, HuntingCreatureMob mob, CreatureSpec.NetTier tier, State state) {
        double pull = Math.max(tier.pull(), player.getStatistics().allStatistics().getOverall(ItemStatistic.PULL));
        double damage = pull * DAMAGE_PER_PULL;
        if (damage >= mob.getHealth()) {
            SpecialMessages.send(player, mob.spec(), "fainted");
            states.remove(mob);
            mob.capture(player);
            return;
        }
        mob.applyToolDamage(player, damage);
        if (!state.weakeningSent && mob.getHealth() <= WEAKENING_HEALTH) {
            state.weakeningSent = true;
            SpecialMessages.send(player, mob.spec(), "health_100");
        }
    }

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        State state = states.get(mob);
        if (state == null || state.cave == null) return;
        boolean atCave = mob.getPosition().distanceSquared(state.cave) <= ARRIVED_DISTANCE_SQUARED;
        CaveHide.Phase phase = state.hide.tick(ticks, atCave);
        if (phase == CaveHide.Phase.OPEN) return;
        if (ticks % 10 == 0 && !atCave) mob.getVanillaNavigator().pathTo(state.cave);
    }

    private Pos pickCave(HuntingCreatureMob mob) {
        List<Pos> caves = configured(mob);
        if (caves.isEmpty()) caves = ring(mob);
        return caves.get(ThreadLocalRandom.current().nextInt(caves.size()));
    }

    private List<Pos> configured(HuntingCreatureMob mob) {
        List<Pos> result = new ArrayList<>();
        if (!(mob.spec().raw().get("caves") instanceof List<?> rows)) return result;
        for (Object row : rows) {
            if (row instanceof List<?> xyz && xyz.size() == 3 && xyz.get(0) instanceof Number x && xyz.get(1) instanceof Number y && xyz.get(2) instanceof Number z) {
                result.add(new Pos(x.doubleValue() + 0.5, y.doubleValue(), z.doubleValue() + 0.5));
            }
        }
        return result;
    }

    private List<Pos> ring(HuntingCreatureMob mob) {
        List<Pos> result = new ArrayList<>();
        double offset = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        for (int i = 0; i < CAVES; i++) {
            double angle = offset + i * Math.PI * 2 / CAVES;
            Pos candidate = mob.getPosition().add(Math.cos(angle) * FALLBACK_RING, 0, Math.sin(angle) * FALLBACK_RING);
            result.add(mob.getInstance() != null && mob.getInstance().getBlock(candidate).isLiquid() ? candidate : mob.getPosition());
        }
        return result;
    }
}
