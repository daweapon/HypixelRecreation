package dev.daweapon.type.theend.dragon;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

public final class DragonSpawnAnimation {
    public static final int RISE_TICKS = GlassRise.RISE_TICKS;
    public static final int STEP_TICKS = 2;
    private static final double BASE_Y = GlassRise.BASE_Y;
    private static final Pos COLUMN_CORNER = new Pos(-672, BASE_Y, -276);
    private static final double COLUMN_WIDTH = 1;

    private DragonSpawnAnimation() {
    }

    public static void play(Instance instance, Runnable onBurst) {
        Entity column = display(instance, Block.PURPLE_STAINED_GLASS, COLUMN_CORNER, new Vec(COLUMN_WIDTH, 0.05, COLUMN_WIDTH));
        Entity tip = display(instance, Block.LIGHT_BLUE_STAINED_GLASS, COLUMN_CORNER, new Vec(COLUMN_WIDTH, 1, COLUMN_WIDTH));
        instance.playSound(Sound.sound(Key.key("block.beacon.activate"), Sound.Source.HOSTILE, 6f, 0.7f),
                DragonArena.ALTAR_CENTER.x(), DragonArena.ALTAR_CENTER.y(), DragonArena.ALTAR_CENTER.z());
        Task[] holder = new Task[1];
        int[] tick = {0};
        holder[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            tick[0] += STEP_TICKS;
            double height = Math.max(0.05, GlassRise.heightAt(tick[0]));
            column.editEntityMeta(BlockDisplayMeta.class, meta -> meta.setScale(new Vec(COLUMN_WIDTH, height, COLUMN_WIDTH)));
            tip.teleport(COLUMN_CORNER.add(0, height, 0));
            if (tick[0] % 20 == 0) {
                instance.playSound(Sound.sound(Key.key("block.amethyst_block.resonate"), Sound.Source.HOSTILE, 4f, 0.5f + tick[0] / 200f),
                        DragonArena.ALTAR_CENTER.x(), DragonArena.ALTAR_CENTER.y() + height, DragonArena.ALTAR_CENTER.z());
            }
            if (tick[0] < RISE_TICKS) return;
            holder[0].cancel();
            column.remove();
            tip.remove();
            burst(instance);
            onBurst.run();
        }).repeat(TaskSchedule.tick(STEP_TICKS)).schedule();
    }

    private static Entity display(Instance instance, Block block, Pos at, Vec scale) {
        Entity entity = new Entity(EntityType.BLOCK_DISPLAY);
        entity.setNoGravity(true);
        entity.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(block);
            meta.setScale(scale);
            meta.setTransformationInterpolationDuration(STEP_TICKS);
        });
        entity.setInstance(instance, at);
        return entity;
    }

    private static void burst(Instance instance) {
        Pos egg = DragonArena.EGG;
        DragonEgg.remove(instance);
        instance.sendGroupedPacket(new ParticlePacket(Particle.EXPLOSION_EMITTER, true, true,
                egg.x(), egg.y(), egg.z(), 3f, 3f, 3f, 0f, 12));
        instance.sendGroupedPacket(new ParticlePacket(Particle.END_ROD, true, true,
                egg.x(), egg.y(), egg.z(), 4f, 4f, 4f, 0.4f, 200));
        instance.playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.HOSTILE, 8f, 0.7f), egg.x(), egg.y(), egg.z());
        instance.playSound(Sound.sound(Key.key("entity.ender_dragon.growl"), Sound.Source.HOSTILE, 8f, 0.8f), egg.x(), egg.y(), egg.z());
    }
}
