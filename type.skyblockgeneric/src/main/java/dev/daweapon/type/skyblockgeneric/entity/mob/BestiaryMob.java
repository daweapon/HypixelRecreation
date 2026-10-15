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
}
