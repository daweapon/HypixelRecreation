package dev.daweapon.type.skyblockgeneric.entity;


import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.SkullHeadComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;


public class DecorationEntityImpl extends EntityCreature {
    public DecorationEntityImpl(SkyBlockItem item, SkyBlockPlayer player) {
        super(EntityType.ARMOR_STAND);
        this.setInvisible(true);

        ArmorStandMeta armorStandMeta = (ArmorStandMeta) getEntityMeta();
        armorStandMeta.setCustomNameVisible(false);
        armorStandMeta.setHasNoGravity(true);
        armorStandMeta.setNotifyAboutChanges(false);

        SkullHeadComponent decorationHead = item.getComponent(SkullHeadComponent.class);

        setHelmet(ItemStacks.head(decorationHead.getSkullTexture(item), "").build());
    }

    public void spawn(Instance instance, Point pos) {
       this.setInstance(instance, pos.sub(0, 1.46875, 0));
    }
}
