package dev.daweapon.type.hub;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.generic.entity.hologram.HologramEntity;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookEvent;
import dev.daweapon.type.skyblockgeneric.greatspook.VargulService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.greatspook.GUIVargulShop;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public final class VargulTombstone {
    private static final Pos POSITION = new Pos(-2.5, 84, 150.5, 0, 0);

    private final Instance instance;
    private final List<Entity> spawned = new ArrayList<>();

    private VargulTombstone(Instance instance) {
        this.instance = instance;
    }

    public static void start(Instance instance) {
        VargulTombstone tombstone = new VargulTombstone(instance);
        MinecraftServer.getSchedulerManager().buildTask(tombstone::sync)
                .delay(TaskSchedule.seconds(5)).repeat(TaskSchedule.seconds(2)).schedule();
    }

    private void sync() {
        try {
            boolean active = GreatSpookEvent.isActive();
            if (active && spawned.isEmpty()) spawn();
            else if (!active && !spawned.isEmpty()) despawn();
        } catch (RuntimeException exception) {
            Logger.error(exception, "Vargul's tombstone failed");
        }
    }

    private void spawn() {
        spawned.add(block(Block.POLISHED_ANDESITE, new Vec(1.0, 0.25, 0.7), new Vec(-0.5, 0, -0.35)));
        spawned.add(block(Block.STONE_BRICKS, new Vec(0.8, 1.3, 0.25), new Vec(-0.4, 0.25, -0.125)));
        spawned.add(block(Block.CHISELED_STONE_BRICKS, new Vec(0.4, 0.4, 0.3), new Vec(-0.2, 0.9, -0.15)));

        HologramEntity title = new HologramEntity(Text.of("<c><l>VARGUL"));
        title.setInstance(instance, POSITION.add(0, 2.3, 0));
        HologramEntity subtitle = new HologramEntity(Text.of("<e><l>CLICK TO UNEARTH"));
        subtitle.setInstance(instance, POSITION.add(0, 2.0, 0));
        spawned.add(title);
        spawned.add(subtitle);

        InteractionEntity click = new InteractionEntity(1.2f, 2.0f, (player, _) -> {
            if (!GreatSpookEvent.isActive()) return;
            VargulService.unearth((SkyBlockPlayer) player, viewer -> viewer.openView(new GUIVargulShop()));
        });
        click.setInstance(instance, POSITION);
        spawned.add(click);
    }

    private Entity block(Block block, Vec scale, Vec translation) {
        Entity display = new Entity(EntityType.BLOCK_DISPLAY);
        display.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(block);
            meta.setScale(scale);
            meta.setTranslation(translation);
            meta.setHasNoGravity(true);
        });
        display.setNoGravity(true);
        display.setInstance(instance, POSITION);
        return display;
    }

    private void despawn() {
        spawned.forEach(Entity::remove);
        spawned.clear();
    }
}
