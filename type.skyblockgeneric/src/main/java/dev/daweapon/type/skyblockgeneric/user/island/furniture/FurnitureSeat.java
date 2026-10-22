package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;

public class FurnitureSeat extends LivingEntity {
    private boolean closing;

    public FurnitureSeat() {
        super(EntityType.ARMOR_STAND);
        editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setInvisible(true);
            meta.setHasNoGravity(true);
            meta.setSmall(true);
            meta.setMarker(true);
        });
    }

    @Override
    public void removePassenger(Entity entity) {
        super.removePassenger(entity);
        if (closing || !getPassengers().isEmpty() || isRemoved()) return;
        closing = true;
        remove();
    }

    public boolean isFree() {
        return !isRemoved() && getPassengers().isEmpty();
    }
}
