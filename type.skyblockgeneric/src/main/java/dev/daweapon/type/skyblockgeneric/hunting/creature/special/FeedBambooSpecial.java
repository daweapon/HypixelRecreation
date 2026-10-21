package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

final class FeedBambooSpecial implements CreatureSpecial {
    private static final String BAMBOO = "BAMBOO";
    private static final double MAX_GROWTH = 1.0;
    private static final double FLEE_SPEED = 8;
    private static final double FLEE_LIFT = 4;
    private static final double PANDA_HEIGHT = 1.25;
    private static final double PANDA_RADIUS = 0.65;

    private final PerMob<BambooFeeding> states = new PerMob<>();

    @Override
    public boolean onInteract(SkyBlockPlayer player, HuntingCreatureMob mob) {
        if (!HeldItems.holds(player, BAMBOO)) return false;
        BambooFeeding feeding = states.of(mob, () -> roll(mob.spec()));
        double next = scale(feeding.fed() + 1, feeding);
        if (!hasRoom(mob, next)) {
            SpecialMessages.send(player, mob.spec(), "no_space");
            return true;
        }
        TimedConsumables.consumeHeld(player, HeldItems.held(player));
        boolean done = feeding.feed();
        mob.getAttribute(Attribute.SCALE).setBaseValue(next);
        SpecialEffects.burst(mob, Particle.HAPPY_VILLAGER, mob.getPosition().add(0, PANDA_HEIGHT * next / 2, 0), 6, 0.4f);
        if (!done) return true;
        states.clear(mob);
        SpecialMessages.send(player, mob.spec(), "caught");
        mob.capture(player);
        return true;
    }

    @Override
    public boolean onPlayerHit(SkyBlockPlayer player, HuntingCreatureMob mob) {
        SpecialMessages.send(player, mob.spec(), "hit");
        Vec away = CreatureMotion.horizontal(player.getPosition(), mob.getPosition());
        mob.setVelocity(new Vec(away.x() * FLEE_SPEED, FLEE_LIFT, away.z() * FLEE_SPEED));
        return true;
    }

    private static BambooFeeding roll(CreatureSpec spec) {
        int min = 40;
        int max = 44;
        if (spec.raw().get("feedBamboo") instanceof Map<?, ?> range) {
            if (range.get("min") instanceof Number number) min = number.intValue();
            if (range.get("max") instanceof Number number) max = number.intValue();
        }
        return BambooFeeding.roll(min, max, ThreadLocalRandom.current());
    }

    private static double scale(int fed, BambooFeeding feeding) {
        return 1 + MAX_GROWTH * Math.min(1, (double) fed / feeding.target());
    }

    private static boolean hasRoom(HuntingCreatureMob mob, double scale) {
        Instance instance = mob.getInstance();
        if (instance == null) return false;
        Pos at = mob.getPosition();
        int height = (int) Math.ceil(PANDA_HEIGHT * scale);
        for (int dy = 1; dy <= height; dy++) {
            if (instance.getBlock(at.add(0, dy, 0)).isSolid()) return false;
        }
        double reach = PANDA_RADIUS * scale;
        double mid = PANDA_HEIGHT * scale / 2;
        for (Vec side : new Vec[]{new Vec(reach, 0, 0), new Vec(-reach, 0, 0), new Vec(0, 0, reach), new Vec(0, 0, -reach)}) {
            if (instance.getBlock(at.add(side.x(), mid, side.z())).isSolid()) return false;
        }
        return true;
    }
}
