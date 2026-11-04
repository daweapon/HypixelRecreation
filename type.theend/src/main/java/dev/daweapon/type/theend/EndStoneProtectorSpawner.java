package dev.daweapon.type.theend;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.theend.MobEndStoneProtector;
import dev.daweapon.type.skyblockgeneric.theend.ProtectorProgress;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Watches the server Zealot counter: each 1,000 kills the ground trembles, and at 5,000 the End Stone Protector
 * rises at one of its spawn points (the "Golem Spawn" points of the SkyHanni End graph, floors read from the
 * captured world).
 */
public final class EndStoneProtectorSpawner {
    public static final int CHECK_TICKS = 20;
    public static final List<Pos> SPAWNS = List.of(
            new Pos(-726.5, 5, -283.5), new Pos(-688.5, 5, -271.5), new Pos(-643.5, 6, -269.5),
            new Pos(-677.5, 5, -331.5), new Pos(-637.5, 5, -326.5), new Pos(-647.5, 5, -217.5));

    private static ProtectorProgress.Stage announced = ProtectorProgress.Stage.RESTING;
    private static MobEndStoneProtector protector;

    private EndStoneProtectorSpawner() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(EndStoneProtectorSpawner::check)
                .repeat(TaskSchedule.tick(CHECK_TICKS)).schedule();
    }

    private static void check() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null) return;
        if (ProtectorProgress.summoned() && (protector == null || protector.isRemoved())) ProtectorProgress.reset();
        ProtectorProgress.Stage stage = ProtectorProgress.stage();
        if (stage.ordinal() < announced.ordinal()) announced = stage;
        if (ProtectorProgress.ready()) {
            summon(instance);
            return;
        }
        if (stage.ordinal() > announced.ordinal()) {
            announced = stage;
            broadcast(instance, "<c>You feel a tremor from beneath the earth!", "entity.iron_golem.step");
        }
    }

    private static void summon(Instance instance) {
        Pos spawn = SPAWNS.get(ThreadLocalRandom.current().nextInt(SPAWNS.size()));
        if (!instance.isChunkLoaded(spawn)) instance.loadChunk(spawn).join();
        ProtectorProgress.markSummoned();
        announced = ProtectorProgress.Stage.SUMMONED;
        protector = new MobEndStoneProtector();
        protector.setInstance(instance, spawn);
        broadcast(instance, "<c><l>The ground begins to shake as an End Stone Protector rises from below!", "entity.iron_golem.hurt");
    }

    private static void broadcast(Instance instance, String message, String sound) {
        Text text = Text.parse(message);
        Sound effect = Sound.sound(Key.key(sound), Sound.Source.HOSTILE, 1f, 0.5f);
        instance.getPlayers().forEach(player -> {
            player.sendMessage(text);
            player.playSound(effect);
        });
    }
}
