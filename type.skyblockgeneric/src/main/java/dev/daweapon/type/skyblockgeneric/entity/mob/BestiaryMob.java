package dev.daweapon.type.skyblockgeneric.entity.mob;

import net.minestom.server.entity.EntityType;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;

public abstract class BestiaryMob extends SkyBlockMob {
    public BestiaryMob(EntityType entityType) {
        super(entityType);
    }

    public abstract int getMaxBestiaryTier();
    public abstract int getBestiaryBracket();
    public abstract String getMobID();
    public abstract GUIMaterial getGuiMaterial();

    public boolean countsForBestiary() {
        return true;
    }

    /** The mob whose Bestiary kill count a kill of this one raises; a family kept under its own id overrides this. */
    public BestiaryMob bestiaryKillTarget() {
        return this;
    }
}
