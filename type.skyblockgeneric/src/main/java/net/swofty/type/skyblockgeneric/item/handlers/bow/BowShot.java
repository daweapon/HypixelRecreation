package net.swofty.type.skyblockgeneric.item.handlers.bow;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.entity.ArrowEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.BowComponent;
import net.swofty.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import net.swofty.type.skyblockgeneric.item.components.ShortBowComponent;
import net.swofty.type.skyblockgeneric.item.set.sets.terror.HydraStrike;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fires a bow: one arrow from the quiver per shot, a shortbow's whole volley fanned around the crosshair, and Hydra
 * Strike's speed and extra arrows on top.
 */
public final class BowShot {
    private static final long MILLIS_PER_TICK = 50;
    private static final Map<UUID, Long> NEXT_SHORTBOW_SHOT = new ConcurrentHashMap<>();

    private BowShot() {
    }

    public static @Nullable BowComponent bowOf(SkyBlockItem item) {
        if (item.hasComponent(ShortBowComponent.class)) return item.getComponent(ShortBowComponent.class);
        if (item.hasComponent(BowComponent.class)) return item.getComponent(BowComponent.class);
        return null;
    }

    public static boolean isShortBow(SkyBlockItem item) {
        BowComponent bow = bowOf(item);
        return bow instanceof ShortBowComponent || (bow != null && !bow.isShouldBeArrow());
    }

    /** A click with a shortbow: fires at full power unless the shot cooldown (shortened by Attack Speed) is running. */
    public static boolean tryShortBow(SkyBlockPlayer player, SkyBlockItem item) {
        if (!isShortBow(item)) return false;
        if (item.hasComponent(ItemRequirementsComponent.class)
                && !item.getComponent(ItemRequirementsComponent.class).ensureCanUse(player)) return true;

        long now = System.currentTimeMillis();
        if (now < NEXT_SHORTBOW_SHOT.getOrDefault(player.getUuid(), 0L)) return true;
        float cooldown = item.hasComponent(ShortBowComponent.class) ? item.getComponent(ShortBowComponent.class).getCooldown() : 0.5f;
        double attackSpeed = player.getStatistics().allStatistics().getOverall(ItemStatistic.BONUS_ATTACK_SPEED);
        NEXT_SHORTBOW_SHOT.put(player.getUuid(), now + ShortBowVolley.shotCooldownTicks(cooldown, attackSpeed) * MILLIS_PER_TICK);

        bowOf(item).onBowShoot(player, item, 1.0);
        return true;
    }

    public static void fire(SkyBlockPlayer player, SkyBlockItem bow, double power) {
        SkyBlockItem arrow = player.getAndConsumeArrow();
        if (arrow == null) return;

        ShortBowComponent shortBow = bow.hasComponent(ShortBowComponent.class) ? bow.getComponent(ShortBowComponent.class) : null;
        int arrows = shortBow == null ? 1 : shortBow.getArrows();
        double spread = shortBow == null ? 0 : shortBow.getSpread();

        HydraStrike.Wear wear = HydraStrike.wear(player);
        int stacks = HydraStrike.stacks(player, wear);
        double speed = wear == null ? 1 : wear.arrowSpeedMultiplier(stacks);

        for (double offset : ShortBowVolley.yawOffsets(arrows, spread)) {
            spawn(player, bow, arrow, offset, power * speed, 1);
        }
        double extraFactor = wear == null ? 0 : wear.extraArrowFactor(stacks);
        if (extraFactor <= 0) return;
        for (double offset : ShortBowVolley.hydraOffsets(HydraStrike.EXTRA_ARROWS)) {
            spawn(player, bow, arrow, offset, power * speed, extraFactor);
        }
    }

    private static void spawn(SkyBlockPlayer player, SkyBlockItem bow, SkyBlockItem arrow, double yawOffset,
                              double power, double damageFactor) {
        ArrowEntityImpl entity = new ArrowEntityImpl(player, arrow);
        entity.setBow(bow);
        entity.setDamageFactor(damageFactor);
        Pos at = player.getPosition();
        Vec velocity = BowRegistry.calculateArrowVelocity(at.pitch(), (float) (at.yaw() + yawOffset), power);
        Vec playerVelocity = player.getVelocity();
        velocity = velocity.add(playerVelocity.x(), player.isOnGround() ? 0.0 : playerVelocity.y(), playerVelocity.z());
        entity.setVelocity(velocity);
        entity.setInstance(player.getInstance(), at.add(0, player.getEyeHeight() - 0.1, 0));
    }
}
