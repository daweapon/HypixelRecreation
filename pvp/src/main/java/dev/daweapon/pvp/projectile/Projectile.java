package dev.daweapon.pvp.projectile;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

// Minimal contract for entities that can be shot from a point toward a target or direction.
public interface Projectile {
    void shoot(@NotNull Point from, double power, double spread);
    void shoot(@NotNull Point from, @NotNull Point to, double power, double spread);
}