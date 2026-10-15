package dev.daweapon.anticheat.loader;

import dev.daweapon.anticheat.flag.FlagType;

import java.util.UUID;

// Hook implemented by the host application to react when a player's flag certainty crosses the punishment threshold.
public abstract class PunishmentHandler {
    public abstract void onFlag(UUID uuid, FlagType flagType);
}
