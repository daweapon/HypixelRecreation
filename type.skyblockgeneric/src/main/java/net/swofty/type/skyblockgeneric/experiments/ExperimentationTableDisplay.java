package net.swofty.type.skyblockgeneric.experiments;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;

public final class ExperimentationTableDisplay {
    private static final Tag<Boolean> DISPLAY = Tag.Boolean("experimentation_table_display");

    private ExperimentationTableDisplay() {
    }

    public static void spawn(Instance instance, Point position) {
        remove(instance, position);
        Entity entity = new Entity(EntityType.ITEM_DISPLAY);
        entity.setNoGravity(true);
        entity.setTag(DISPLAY, true);
        entity.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(new SkyBlockItem(ItemType.EXPERIMENTATION_TABLE).getItemStack());
            meta.setDisplayContext(ItemDisplayMeta.DisplayContext.FIXED);
        });
        entity.setInstance(instance, new Pos(position.blockX() + 0.5, position.blockY() + 0.5, position.blockZ() + 0.5));
    }

    public static void remove(Instance instance, Point position) {
        instance.getEntities().stream()
                .filter(entity -> entity.hasTag(DISPLAY))
                .filter(entity -> entity.getPosition().blockX() == position.blockX()
                        && entity.getPosition().blockY() == position.blockY()
                        && entity.getPosition().blockZ() == position.blockZ())
                .forEach(Entity::remove);
    }
}
