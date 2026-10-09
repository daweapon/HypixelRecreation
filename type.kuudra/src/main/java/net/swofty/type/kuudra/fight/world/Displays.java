package net.swofty.type.kuudra.fight.world;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Consumer;

/** Small helpers for the fight's display entities, holograms and click boxes. */
public final class Displays {
    private Displays() {
    }

    /** A block display whose block is centred on {@code at} horizontally and sits on it vertically. */
    public static Entity block(Instance instance, Pos at, Block block, Vec scale) {
        Entity entity = new Entity(EntityType.BLOCK_DISPLAY);
        entity.setNoGravity(true);
        entity.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(block);
            meta.setScale(scale);
            meta.setTranslation(new Vec(-scale.x() / 2, 0, -scale.z() / 2));
            meta.setPosRotInterpolationDuration(2);
            meta.setTransformationInterpolationDuration(2);
        });
        entity.setInstance(instance, at);
        return entity;
    }

    public static Entity item(Instance instance, Pos at, ItemStack item, float scale) {
        Entity entity = new Entity(EntityType.ITEM_DISPLAY);
        entity.setNoGravity(true);
        entity.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(item);
            meta.setScale(new Vec(scale, scale, scale));
            meta.setPosRotInterpolationDuration(2);
        });
        entity.setInstance(instance, at);
        return entity;
    }

    public static Entity text(Instance instance, Pos at, String markup) {
        Entity entity = new Entity(EntityType.TEXT_DISPLAY);
        entity.setNoGravity(true);
        entity.editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setText(Text.parse(markup).asComponent());
            meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
            meta.setBackgroundColor(0);
            meta.setShadow(true);
            meta.setLineWidth(400);
            meta.setPosRotInterpolationDuration(2);
        });
        entity.setInstance(instance, at);
        return entity;
    }

    public static void setText(Entity display, String markup) {
        display.editEntityMeta(TextDisplayMeta.class, meta -> meta.setText(Text.parse(markup).asComponent()));
    }

    public static void setBlock(Entity display, Block block) {
        display.editEntityMeta(BlockDisplayMeta.class, meta -> meta.setBlockState(block));
    }

    /** A click box: right-click runs {@code onUse}; left-clicks arrive as attacks and are routed by the fight. */
    public static Hitbox hitbox(Instance instance, Pos at, float width, float height, Consumer<SkyBlockPlayer> onUse) {
        Hitbox hitbox = new Hitbox(width, height, onUse);
        hitbox.setInstance(instance, at);
        return hitbox;
    }

    public static final class Hitbox extends InteractionEntity {
        public Hitbox(float width, float height, Consumer<SkyBlockPlayer> onUse) {
            super(width, height, (player, _) -> {
                if (player instanceof SkyBlockPlayer skyBlockPlayer) onUse.accept(skyBlockPlayer);
            });
        }
    }

    public static void remove(Entity entity) {
        if (entity != null && !entity.isRemoved()) entity.remove();
    }
}
