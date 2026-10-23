package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

final class NpcGuiProbe {
    private static final double REACH = 4.5;
    private static final int SETTLE_TICKS = 14;
    private static final int GIVE_UP_TICKS = 50;
    private static final int GAP_TICKS = 12;

    private final Set<UUID> tried = new HashSet<>();
    private boolean automatic;
    private boolean sweep;
    private UUID waitingFor;
    private int waited;
    private int screenAge;
    private int gap;
    private int opened;

    String command(String mode) {
        switch (mode) {
            case "on" -> automatic = true;
            case "off" -> automatic = false;
            case "now" -> sweep = true;
            case "reset" -> tried.clear();
            default -> {
            }
        }
        return "NPC GUI probe: " + (automatic ? "automatic" : "off") + (sweep ? ", sweeping now" : "")
                + ", tried " + tried.size() + ", GUIs opened " + opened;
    }

    void tick(Minecraft client, EntityTracker entities) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            return;
        }
        if (waitingFor != null) {
            waited++;
            if (client.gui.screen() instanceof AbstractContainerScreen<?>) {
                if (++screenAge >= SETTLE_TICKS) {
                    client.player.closeContainer();
                    finish(true);
                }
            } else if (waited >= GIVE_UP_TICKS) {
                finish(false);
            }
            return;
        }
        if (gap > 0) {
            gap--;
            return;
        }
        if (!automatic && !sweep) {
            return;
        }
        if (client.gui.screen() != null) {
            return;
        }
        Entity target = next(client, entities);
        if (target == null) {
            sweep = false;
            return;
        }
        tried.add(target.getUUID());
        waitingFor = target.getUUID();
        waited = 0;
        screenAge = 0;
        client.gameMode.interact(client.player, target, new EntityHitResult(target), InteractionHand.MAIN_HAND);
    }

    private void finish(boolean gui) {
        if (gui) {
            opened++;
        }
        waitingFor = null;
        gap = GAP_TICKS;
    }

    private Entity next(Minecraft client, EntityTracker entities) {
        Entity best = null;
        double bestDistance = REACH;
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity == client.player || tried.contains(entity.getUUID()) || !entities.isNpc(entity.getUUID())) {
                continue;
            }
            double distance = client.player.distanceTo(entity);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }
}
