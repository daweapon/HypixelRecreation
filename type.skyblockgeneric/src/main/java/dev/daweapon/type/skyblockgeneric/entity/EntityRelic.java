package dev.daweapon.type.skyblockgeneric.entity;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;

public class EntityRelic extends EntityCreature {
    private static final String SKULL_TEXTURES = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmYwY2YxM2NiYjM2OGFmOWNlZTEyMzI5NzlhMTRiODRlNmViZjIyYzEzYjE0MjdmOTMxNmQ5NzRiY2UifX19";

    private final int relic;

    public EntityRelic(int relic) {
        super(EntityType.ARMOR_STAND);
        this.relic = relic;
        setInvisible(true);
        ArmorStandMeta meta = (ArmorStandMeta) getEntityMeta();
        meta.setCustomNameVisible(false);
        meta.setHasNoGravity(true);
        meta.setNotifyAboutChanges(false);
        setHelmet(ItemStack.builder(Material.PLAYER_HEAD)
                .set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(SKULL_TEXTURES, null)))
                .build());
    }

    public int relic() {
        return relic;
    }

    public void spawn(Instance instance, Pos at) {
        setInstance(instance, at.sub(0, 1.46875, 0));
    }
}
