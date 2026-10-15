package dev.daweapon.type.skyblockgeneric.experiments;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

public final class ExperimentationTableDisplay {
    private static final Tag<Boolean> DISPLAY = Tag.Boolean("experimentation_table_display");
    private static final String MODEL = "hypixel_skyblock:item/island_relevant/enchanting/experimentation_table";

    private ExperimentationTableDisplay() {
    }

    public static void spawn(Instance instance, Point position) {
        remove(instance, position);
        Entity entity = new Entity(EntityType.ITEM_DISPLAY);
        entity.setNoGravity(true);
        entity.setTag(DISPLAY, true);
        entity.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(ItemStack.builder(Material.PAPER).set(DataComponents.ITEM_MODEL, MODEL).build());
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
