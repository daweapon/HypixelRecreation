package dev.daweapon.type.skyblockgeneric.consumable;

import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.util.concurrent.ThreadLocalRandom;

public final class MarshSporeSoup {
    public static final String ID = "MARSH_SPORE_SOUP";
    public static final long DURATION_MILLIS = 80_000;
    private static final int TICK_STEP = 5;
    private static final double BLAST_CHANCE_PER_STEP = 0.35;
    private static final double SPREAD = 3;

    private MarshSporeSoup() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(ID, InteractableItemConfig.builder().rightClickHandler((player, item) -> {
            if (!TimedConsumables.start(player, ID, DURATION_MILLIS)) {
                player.sendMessage("<c>You already have the Marsh Spore Soup effect active!");
                return;
            }
            TimedConsumables.consumeHeld(player, item);
            player.sendMessage("<a>Mushrooms start bursting around you for <e>80 seconds<a>!");
            begin(player);
        }).build());
    }

    private static void begin(SkyBlockPlayer player) {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (player.isRemoved() || player.isDead() || player.getInstance() == null || !TimedConsumables.isActive(player, ID)) {
                return TaskSchedule.stop();
            }
            MythologicalCombat.particles(player.getInstance(), Particle.EFFECT, player.getPosition().add(0, 1, 0), 0.6f, 4);
            if (ThreadLocalRandom.current().nextDouble() < BLAST_CHANCE_PER_STEP) {
                SporeBurst.explode(player.getInstance(), player.getPosition(), SPREAD, 1);
                MythologicalCombat.sound(player, "entity.generic.explode", 0.6f, 1.4f);
            }
            return TaskSchedule.tick(TICK_STEP);
        });
    }
}
