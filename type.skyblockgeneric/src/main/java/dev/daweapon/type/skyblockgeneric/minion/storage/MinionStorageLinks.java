package dev.daweapon.type.skyblockgeneric.minion.storage;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class MinionStorageLinks {
    private MinionStorageLinks() {
    }

    public static List<Point> linkOrder(Point chest) {
        return List.of(
                new Vec(chest.blockX(), chest.blockY(), chest.blockZ() + 1),
                new Vec(chest.blockX() - 1, chest.blockY(), chest.blockZ()),
                new Vec(chest.blockX(), chest.blockY(), chest.blockZ() - 1),
                new Vec(chest.blockX() + 1, chest.blockY(), chest.blockZ()));
    }

    public static Optional<Point> firstAvailable(Point chest, Predicate<Point> available) {
        return linkOrder(chest).stream().filter(available).findFirst();
    }

    public static String facingToward(Point chest, Point minion) {
        int dx = minion.blockX() - chest.blockX();
        int dz = minion.blockZ() - chest.blockZ();
        if (dz > 0) return "south";
        if (dz < 0) return "north";
        return dx < 0 ? "west" : "east";
    }
}
