package dev.daweapon.type.skyblockgeneric.abiphone.ninef;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;

public class EntityNineFRelay extends EntityCreature {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODQzYTRhOGZhNzlmOGQxNjIxZDVlYTQ5MGI5ZTAyMTc5NjJmOTA1OTRlZGE4OTY3NWZjMTNiMjlkNTI5MDQ3NCJ9fX0=";
    private static final double HEAD_OFFSET = 1.46875;

    private final NineFRelay relay;

    public EntityNineFRelay(NineFRelay relay) {
        super(EntityType.ARMOR_STAND);
        this.relay = relay;
        setInvisible(true);
        setAutoViewable(false);
        ArmorStandMeta meta = (ArmorStandMeta) getEntityMeta();
        meta.setCustomNameVisible(false);
        meta.setHasNoGravity(true);
        meta.setNotifyAboutChanges(false);
        setHelmet(ItemStack.builder(Material.PLAYER_HEAD)
                .set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(TEXTURE, null)))
                .build());
    }

    public NineFRelay relay() {
        return relay;
    }

    public void spawn(Instance instance) {
        setInstance(instance, relay.position().sub(0, HEAD_OFFSET, 0));
    }
}
