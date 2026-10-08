package net.swofty.type.skyblockgeneric.mythological.burrow;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The particles only the burrow's owner sees: the burrow marker, the Echo trail and the arrow left
 * behind a dug burrow pointing at the next one. Minecraft dropped the 1.8 footstep particle, so the
 * overlapping footprints on a burrow are flat resource-pack decals that shrink away instead.
 */
public final class BurrowEffects {
    private static final double ECHO_MAX_LENGTH = 40;
    private static final double ECHO_STEP = 0.5;
    private static final double ARROW_LENGTH = 3;
    private static final Color NEAR = new Color(255, 255, 85);
    private static final Color MEDIUM = new Color(255, 85, 85);
    private static final Color FAR = new Color(40, 40, 40);
    private static final String FOOTPRINT_MODEL = "hypixel_skyblock:item/mythological/griffin_footprint";
    private static final double FOOTPRINT_SIZE = 0.6;
    private static final double FOOTPRINT_SPREAD = 0.35;
    private static final int FOOTPRINT_LIFETIME_TICKS = 60;
    private static final int FOOTPRINT_FADE_TICKS = 15;

    private BurrowEffects() {
    }

    public static void marker(SkyBlockPlayer player, GriffinBurrow burrow) {
        Point top = burrow.getPosition().add(0.5, 1.1, 0.5);
        send(player, Particle.ENCHANT, top.add(0, 0.4, 0), 0.4f, 0.3f, 8);
        send(player, burrow.getKind().getParticle(), top, 0.45f, 0.1f, 8);
    }

    public static void footprint(SkyBlockPlayer player, GriffinBurrow burrow) {
        if (player.getInstance() == null) return;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Point at = burrow.getPosition().add(
                0.5 + (random.nextDouble() * 2 - 1) * FOOTPRINT_SPREAD,
                1.01 + FOOTPRINT_SIZE / 2,
                0.5 + (random.nextDouble() * 2 - 1) * FOOTPRINT_SPREAD);
        double halfYaw = random.nextDouble() * Math.PI;

        Entity print = new Entity(EntityType.ITEM_DISPLAY);
        print.setNoGravity(true);
        print.updateViewableRule(viewer -> viewer.getUuid().equals(player.getUuid()));
        print.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(ItemStack.builder(Material.PAPER).set(DataComponents.ITEM_MODEL, FOOTPRINT_MODEL).build());
            meta.setScale(new Vec(FOOTPRINT_SIZE, FOOTPRINT_SIZE, FOOTPRINT_SIZE));
            meta.setLeftRotation(new float[]{0, (float) Math.sin(halfYaw), 0, (float) Math.cos(halfYaw)});
        });
        print.setInstance(player.getInstance(), Pos.fromPoint(at));
        print.scheduler().buildTask(() -> print.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setTransformationInterpolationStartDelta(0);
            meta.setTransformationInterpolationDuration(FOOTPRINT_FADE_TICKS);
            meta.setScale(Vec.ZERO);
        })).delay(TaskSchedule.tick(FOOTPRINT_LIFETIME_TICKS - FOOTPRINT_FADE_TICKS)).schedule();
        print.scheduler().buildTask(print::remove).delay(TaskSchedule.tick(FOOTPRINT_LIFETIME_TICKS)).schedule();
    }

    public static void echo(SkyBlockPlayer player, Point target) {
        Vec origin = player.getPosition().add(0, player.getEyeHeight(), 0).asVec();
        Vec toTarget = vec(target.add(0.5, 1, 0.5)).sub(origin);
        double distance = toTarget.length();
        Vec step = toTarget.normalize().mul(ECHO_STEP);
        Vec cursor = origin;
        for (double travelled = 0; travelled < Math.min(distance, ECHO_MAX_LENGTH); travelled += ECHO_STEP) {
            cursor = cursor.add(step);
            send(player, Particle.DRIPPING_LAVA, cursor, 0, 0, 1);
        }
        float pitch = (float) Math.max(0.5, 2 - distance / 100);
        player.playSound(Sound.sound(Key.key("block.note_block.harp"), Sound.Source.PLAYER, 1f, pitch));
    }

    public static void arrow(SkyBlockPlayer player, Point from, Point to) {
        Vec base = vec(from.add(0.5, 1.5, 0.5));
        Vec direction = vec(to).sub(vec(from)).withY(0);
        if (direction.isZero()) return;
        double distance = direction.length();
        direction = direction.normalize();
        Particle dust = Particle.DUST.withColor(distance < 50 ? NEAR : distance < 100 ? MEDIUM : FAR);

        for (double d = 0; d <= ARROW_LENGTH; d += 0.2) {
            send(player, dust, base.add(direction.mul(d)), 0, 0, 1);
        }
        Vec tip = base.add(direction.mul(ARROW_LENGTH));
        Vec left = direction.rotateAroundY(Math.toRadians(150));
        Vec right = direction.rotateAroundY(Math.toRadians(-150));
        for (double d = 0; d <= 1; d += 0.2) {
            send(player, dust, tip.add(left.mul(d)), 0, 0, 1);
            send(player, dust, tip.add(right.mul(d)), 0, 0, 1);
        }
    }

    public static void dig(SkyBlockPlayer player, Point burrow) {
        send(player, Particle.CLOUD, burrow.add(0.5, 1, 0.5), 0.3f, 0.2f, 8);
        player.playSound(Sound.sound(Key.key("block.grass.break"), Sound.Source.PLAYER, 1f, 1f));
    }

    private static Vec vec(Point point) {
        return new Vec(point.x(), point.y(), point.z());
    }

    private static void send(SkyBlockPlayer player, Particle particle, Point at, float spread, float speed, int count) {
        player.sendPacket(new ParticlePacket(particle, false, true,
                (float) at.x(), (float) at.y(), (float) at.z(),
                spread, spread, spread, speed, count));
    }
}
