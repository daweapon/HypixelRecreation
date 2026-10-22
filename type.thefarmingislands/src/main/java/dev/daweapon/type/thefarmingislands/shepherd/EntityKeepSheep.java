package dev.daweapon.type.thefarmingislands.shepherd;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;

public class EntityKeepSheep extends EntityCreature {
    private long shearedAt;

    public EntityKeepSheep() {
        super(EntityType.SHEEP);
        setInvulnerable(true);
    }

    private SheepMeta sheepMeta() {
        return (SheepMeta) getEntityMeta();
    }

    public boolean isSheared() {
        return sheepMeta().isSheared();
    }

    public void shear(long now) {
        shearedAt = now;
        sheepMeta().setSheared(true);
    }

    public void regrowIfReady(long now, long regrowMillis) {
        if (isSheared() && now - shearedAt >= regrowMillis) sheepMeta().setSheared(false);
    }
}
