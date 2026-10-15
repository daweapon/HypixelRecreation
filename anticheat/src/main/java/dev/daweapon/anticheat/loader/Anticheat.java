package dev.daweapon.anticheat.loader;

import lombok.Getter;
import dev.daweapon.anticheat.engine.AnticheatEngine;
import dev.daweapon.anticheat.event.AntiCheatListener;
import dev.daweapon.anticheat.event.AnticheatEventHandler;
import dev.daweapon.anticheat.flag.FlagType;

import java.util.UUID;

// Static entry point for configuring and starting the anticheat (loader, settings, punishment handler).
public class Anticheat {
    @Getter
    private static Loader loader = null;
    @Getter
    private static AnticheatValues values = null;
    @Getter
    private static PunishmentHandler punishmentHandler = new PunishmentHandler() {
        @Override
        public void onFlag(UUID uuid, FlagType flagType) {}
    };

    public static void loader(Loader loader) {
        Anticheat.loader = loader;
    }

    public static void values(AnticheatValues values) {
        Anticheat.values = values;
    }

    public static void punishmentHandler(PunishmentHandler punishmentHandler) {
        Anticheat.punishmentHandler = punishmentHandler;
    }

    public static void registerEvent(AntiCheatListener listener) {
        AnticheatEventHandler.registerEventMethods(listener);
    }

    public static void start() {
        if (loader == null) {
            throw new IllegalStateException("Loader not set, use Anticheat#loader(Loader) to set the loader");
        }

        if (values == null) {
            throw new IllegalStateException("Values not set, use Anticheat#values(AnticheatValues) to set the values");
        }

        loader.onInitialize();
        AnticheatEngine.registerEvents();
        AnticheatEngine.startSchedulers(loader);
    }
}
