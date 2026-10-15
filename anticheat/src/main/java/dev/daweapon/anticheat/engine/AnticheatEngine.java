package dev.daweapon.anticheat.engine;

import dev.daweapon.anticheat.api.AnticheatAPI;
import dev.daweapon.anticheat.event.AnticheatEventHandler;
import dev.daweapon.anticheat.flag.FlagType;
import dev.daweapon.anticheat.loader.Loader;
import dev.daweapon.anticheat.loader.Anticheat;
import dev.daweapon.anticheat.loader.managers.AnticheatSchedulerManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// Bootstraps the anticheat's repeating tick/ping schedulers and registers all movement/flag event listeners.
public class AnticheatEngine {
    public static void startSchedulers(Loader loader) {
        AnticheatSchedulerManager scheduler = loader.getSchedulerManager();
        scheduler.scheduleRepeatingTask(() -> {
            List<UUID> onlinePlayers = loader.getOnlinePlayers();
            List<AnticheatPlayer> players = new ArrayList<>();

            synchronized (AnticheatPlayer.players) {
                AnticheatPlayer.players.forEach((uuid, player) -> {
                    if (!onlinePlayers.contains(uuid)) {
                        player.getWorld().shutdown();
                        AnticheatPlayer.players.remove(uuid);
                        // Clean up player data when they disconnect
                        AnticheatAPI.clearPlayerData(uuid);
                        AnticheatAPI.clearBypasses(uuid);
                    }
                });
            }

            onlinePlayers.forEach(uuid -> {
                if (!AnticheatPlayer.players.containsKey(uuid)) {
                    players.add(new AnticheatPlayer(uuid));
                } else {
                    players.add(AnticheatPlayer.players.get(uuid));
                }
            });

            players.forEach((player -> {
                player.moveTickOn();
                player.sendPingRequest();
                if (player.ticksSinceLastPingResponse() > Anticheat.getValues().getTicksAllowedToMissPing()) {
                    player.flag(FlagType.TIMEOUT_PING_PACKETS, 100);
                }
            }));
        }, 1, 1);

        // Cleanup expired bypasses every 5 seconds (100 ticks)
        scheduler.scheduleRepeatingTask(() -> {
            AnticheatAPI.getBypassManager().cleanupExpired();
        }, 100, 100);
    }

    public static void registerEvents() {
        AnticheatEventHandler.registerEventMethods(new MovementEvents());
        AnticheatEventHandler.registerEventMethods(new PingEvents());

        Arrays.stream(FlagType.values()).forEach(flagType -> {
            AnticheatEventHandler.registerEventMethods(flagType.getFlagSupplier().get());
        });
    }
}
