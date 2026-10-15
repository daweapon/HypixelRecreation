package dev.daweapon.type.skyblockgeneric.item.handlers.bow;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ArrowBounce {
    public static final String BINGBOW = "BINGBOW";
    public static final double CHANCE_PERCENT = 20;
    public static final int MAX_BOUNCES = 2;
    public static final double RANGE = 8;

    private ArrowBounce() {
    }

    public static boolean bounces(SkyBlockItem bow) {
        return bow != null && !bow.isAir() && BINGBOW.equals(bow.getAttributeHandler().getTypeAsString());
    }

    public static void chain(SkyBlockPlayer shooter, SkyBlockItem bow, SkyBlockMob firstTarget, float damage, boolean critical) {
        if (!bounces(bow)) return;
        List<SkyBlockMob> hit = new ArrayList<>(List.of(firstTarget));
        SkyBlockMob current = firstTarget;
        for (int bounce = 0; bounce < MAX_BOUNCES; bounce++) {
            if (!LootRoll.roll(shooter.getUuid(), null, CHANCE_PERCENT)) return;
            SkyBlockMob next = nearest(current, hit);
            if (next == null) return;
            hit.add(next);
            AttackService.applyHit(shooter, next, damage, critical);
            current = next;
        }
    }

    private static SkyBlockMob nearest(SkyBlockMob from, List<SkyBlockMob> excluded) {
        if (from.getInstance() == null) return null;
        return from.getInstance().getNearbyEntities(from.getPosition(), RANGE).stream()
                .filter(entity -> entity instanceof SkyBlockMob)
                .map(entity -> (SkyBlockMob) entity)
                .filter(mob -> !excluded.contains(mob) && !mob.isDead() && !mob.isImmuneToArrows())
                .min(Comparator.comparingDouble(mob -> mob.getPosition().distanceSquared(from.getPosition())))
                .orElse(null);
    }
}
