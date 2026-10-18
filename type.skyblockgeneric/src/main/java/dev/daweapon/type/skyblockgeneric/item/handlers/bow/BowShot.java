package dev.daweapon.type.skyblockgeneric.item.handlers.bow;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentInfiniteQuiver;
import dev.daweapon.type.skyblockgeneric.entity.ArrowEntityImpl;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.BowComponent;
import dev.daweapon.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import dev.daweapon.type.skyblockgeneric.item.components.ShortBowComponent;
import dev.daweapon.type.skyblockgeneric.item.set.sets.terror.HydraStrike;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fires a bow: one arrow from the quiver per shot, a shortbow's whole volley fanned around the crosshair, and Hydra
 * Strike's speed and extra arrows on top.
 */
public final class BowShot {
    private static final String SULPHUR_BOW = "SULPHUR_BOW";
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

    /**
     * The client starts drawing a bow on its own whenever it can see an arrow, so a cancelled use alone leaves the
     * draw animation running. Re-sending the "hand active" flag as false makes the client let go.
     */
    public static void stopDrawing(SkyBlockPlayer player) {
        LivingEntityMeta meta = player.getLivingEntityMeta();
        if (meta == null) return;
        meta.setNotifyAboutChanges(false);
        meta.setHandActive(true);
        meta.setHandActive(false);
        meta.setNotifyAboutChanges(true);
    }

    public static void fire(SkyBlockPlayer player, SkyBlockItem bow, double power) {
        SkyBlockItem arrow = EnchantmentInfiniteQuiver.saves(player, bow) ? player.peekArrow() : player.getAndConsumeArrow();
        if (arrow == null) return;

        ShortBowComponent shortBow = bow.hasComponent(ShortBowComponent.class) ? bow.getComponent(ShortBowComponent.class) : null;
        int arrows = shortBow == null ? 1 : shortBow.getArrows();
        double spread = shortBow == null ? 0 : shortBow.getSpread();

        HydraStrike.Wear wear = HydraStrike.wear(player);
        int stacks = HydraStrike.stacks(player, wear);
        double speed = wear == null ? 1 : wear.arrowSpeedMultiplier(stacks);
        double infusion = arrowInfusion(player, bow);

        for (double offset : ShortBowVolley.yawOffsets(arrows, spread)) {
            spawn(player, bow, arrow, offset, power * speed, infusion);
        }
        double extraFactor = wear == null ? 0 : wear.extraArrowFactor(stacks);
        if (extraFactor <= 0) return;
        for (double offset : ShortBowVolley.hydraOffsets(HydraStrike.EXTRA_ARROWS)) {
            spawn(player, bow, arrow, offset, power * speed, extraFactor * infusion);
        }
    }

    private static double arrowInfusion(SkyBlockPlayer player, SkyBlockItem bow) {
        if (!SULPHUR_BOW.equals(bow.getAttributeHandler().getTypeAsString())) return 1;
        if (player.getAmountInInventory(ItemType.SULPHUR) < 1) return 1;
        return player.removeItemFromPlayer(ItemType.SULPHUR, 1) ? 2 : 1;
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
        Pos spawnAt = at.add(0, player.getEyeHeight() - 0.1, 0);
        entity.setOrigin(spawnAt);
        entity.setInstance(player.getInstance(), spawnAt);
    }
}
