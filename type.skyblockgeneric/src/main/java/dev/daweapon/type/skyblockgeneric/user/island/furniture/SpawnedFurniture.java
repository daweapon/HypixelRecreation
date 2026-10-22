package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class SpawnedFurniture {
    final LivingEntity display;
    final LivingEntity hitbox;
    final List<FurnitureSeat> seats = new ArrayList<>();
    @Nullable LivingEntity minion;
    @Nullable Task flames;

    SpawnedFurniture(LivingEntity display, LivingEntity hitbox) {
        this.display = display;
        this.hitbox = hitbox;
    }

    void remove() {
        display.remove();
        hitbox.remove();
        if (minion != null) minion.remove();
        if (flames != null) flames.cancel();
        seats.forEach(seat -> {
            new ArrayList<>(seat.getPassengers()).forEach(seat::removePassenger);
            if (!seat.isRemoved()) seat.remove();
        });
        seats.clear();
    }
}
