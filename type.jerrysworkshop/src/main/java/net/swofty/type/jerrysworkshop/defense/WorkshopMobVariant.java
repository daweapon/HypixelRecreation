package net.swofty.type.jerrysworkshop.defense;

import lombok.Getter;
import net.minestom.server.entity.EntityType;

// The real "Defend Jerry's Workshop" mob roster (SkyBlock Wiki, "Jerry's Workshop"): Frozen
// Steve, Frosty, Grinch, Nutcracker, Yeti, Reindrake - no Magma Cube. Entity types below are
// vanilla stand-ins (no real per-mob skin/model asset sourced), picked for rough visual fit.
@Getter
public enum WorkshopMobVariant {
    FROZEN_STEVE(EntityType.ZOMBIE, "Frozen Steve"),
    FROSTY(EntityType.SNOW_GOLEM, "Frosty"),
    GRINCH(EntityType.ZOMBIE_VILLAGER, "Grinch"),
    NUTCRACKER(EntityType.VINDICATOR, "Nutcracker"),
    YETI(EntityType.POLAR_BEAR, "Yeti"),
    REINDRAKE(EntityType.RAVAGER, "Reindrake");

    private final EntityType entityType;
    private final String displayName;

    WorkshopMobVariant(EntityType entityType, String displayName) {
        this.entityType = entityType;
        this.displayName = displayName;
    }
}
