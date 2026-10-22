package dev.daweapon.type.galatea.npcs.dax;

import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.hunting.HuntrapDisplay;
import dev.daweapon.type.skyblockgeneric.hunting.HuntrapTier;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpawner;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DaxHuntrap {
    private static final String SHARD = "Verdant";
    private static final double VIEW_RANGE = 40;
    private static final String MESSAGE_PICKUP = "<e>[NPC] <b>Dax<f>: Hey! That is mine!";
    private static final String MESSAGE_CLAIM = "<e>[NPC] <b>Dax<f>: Hold on... that was mine.";

    private final Pos daxPosition;
    private final String metTrigger;
    private final Map<UUID, HuntrapDisplay> displays = new HashMap<>();
    private Pos trapPosition;

    public DaxHuntrap(Pos daxPosition, String metTrigger) {
        this.daxPosition = daxPosition;
        this.metTrigger = metTrigger;
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            tick();
            return TaskSchedule.seconds(2);
        }, ExecutionType.TICK_START);
    }

    private void tick() {
        Map<UUID, SkyBlockPlayer> eligible = new HashMap<>();
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (player.getInstance() == null || !ChapterTriggers.done(player, metTrigger)) continue;
            if (player.getPosition().distance(daxPosition) > VIEW_RANGE) continue;
            eligible.put(player.getUuid(), player);
        }
        displays.keySet().removeIf(uuid -> {
            if (eligible.containsKey(uuid)) return false;
            displays.get(uuid).remove();
            return true;
        });
        eligible.forEach((uuid, player) -> {
            if (displays.containsKey(uuid)) return;
            Pos position = position(player);
            AttributeDefinition caught = AttributeRegistry.findByShard(SHARD).orElse(null);
            if (position == null || caught == null) return;
            displays.put(uuid, new HuntrapDisplay(player.getInstance(), position, uuid, HuntrapTier.RETIA_BASICA, caught,
                    0, clicker -> clicker.sendMessage(clicker.isSneaking() ? MESSAGE_PICKUP : MESSAGE_CLAIM)));
        });
    }

    private Pos position(SkyBlockPlayer player) {
        if (trapPosition != null) return trapPosition;
        Pos ground = CreatureSpawner.surface(player.getInstance(), (int) Math.floor(daxPosition.x()) + 2,
                (int) Math.floor(daxPosition.z()) + 1, false, (int) daxPosition.y() + 4);
        if (ground == null) return null;
        trapPosition = new Pos(Math.floor(ground.x()), ground.y(), Math.floor(ground.z()));
        return trapPosition;
    }
}
