package dev.daweapon.type.catacombs.run;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.item.ItemStack;

import java.util.function.Consumer;

/**
 * One part of an in-world reward chest: an invisible, silent armor stand (small for some parts, head tilted for the
 * skulls) wearing a block or head. Right-clicking it opens that chest, as on Hypixel.
 */
public final class RewardChestStand extends LivingEntity {
    private final Consumer<SkyBlockPlayer> onClick;

    public RewardChestStand(boolean small, float headPitch, ItemStack helmet, Consumer<SkyBlockPlayer> onClick) {
        super(EntityType.ARMOR_STAND);
        this.onClick = onClick;
        setInvisible(true);
        setSilent(true);
        setNoGravity(true);
        editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setSmall(small);
            meta.setHasArms(true);
            meta.setHeadRotation(new Vec(headPitch, 0, 0));
        });
        setEquipment(EquipmentSlot.HELMET, helmet);
    }

    public void click(SkyBlockPlayer player) {
        onClick.accept(player);
    }

    @Override
    public boolean damage(Damage damage) {
        return false;
    }
}
