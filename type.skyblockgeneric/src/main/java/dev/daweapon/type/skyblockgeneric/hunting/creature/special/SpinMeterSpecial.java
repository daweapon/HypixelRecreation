package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

final class SpinMeterSpecial implements CreatureSpecial {
    private static final double DECAY_PER_TICK = 0.4;
    private static final double JITTER = 4;
    private static final Map<CreatureSpec.NetTier, Double> GAIN = Map.of(
            CreatureSpec.NetTier.BASIC, 25D, CreatureSpec.NetTier.MEDIUM, 35D,
            CreatureSpec.NetTier.TURBO, 45D, CreatureSpec.NetTier.GIGANTIC, 55D);

    private final Map<HuntingCreatureMob, SpinMeter> meters = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public boolean onNetHit(SkyBlockPlayer player, HuntingCreatureMob mob, CreatureSpec.NetTier tier, double distance) {
        SpinMeter meter = meters.computeIfAbsent(mob, key -> new SpinMeter());
        double gain = GAIN.get(tier) + ThreadLocalRandom.current().nextDouble(-JITTER, JITTER);
        switch (meter.add(gain)) {
            case STARTED -> SpecialMessages.send(player, mob.spec(), "spin_start");
            case KNOCKED_OUT -> {
                SpecialMessages.send(player, mob.spec(), "knocked_out", String.valueOf(Math.round(meter.value())));
                meters.remove(mob);
                mob.setHeader("");
                mob.capture(player);
            }
            case EXPLODED -> {
                SpecialMessages.send(player, mob.spec(), "exploded");
                meters.remove(mob);
                mob.remove();
            }
            default -> {
            }
        }
        return true;
    }

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        SpinMeter meter = meters.get(mob);
        if (meter == null || !meter.spinning()) return;
        meter.decay(DECAY_PER_TICK);
        if (ticks % 5 != 0) return;
        mob.setHeader(meter.spinning() ? "<e>Spin <f>" + Math.round(meter.value()) + "%" : "");
    }
}
