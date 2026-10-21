package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

final class TotemHeads {
    static final double HEAD_HEIGHT = 1.0;
    private static final double DISPLAY_SCALE = 2.0;
    private static final int WATCH_TICKS = 20;

    private final Entity[] heads = new Entity[TotemPuzzle.HEADS];
    private final Pos home;
    private final Task watcher;

    TotemHeads(HuntingCreatureMob owner, TotemPuzzle puzzle) {
        this.home = owner.getPosition();
        for (int i = 0; i < heads.length; i++) {
            Entity head = new Entity(EntityType.ITEM_DISPLAY);
            head.setNoGravity(true);
            head.editEntityMeta(ItemDisplayMeta.class, meta -> {
                meta.setItemStack(ItemStack.of(Material.CARVED_PUMPKIN));
                meta.setDisplayContext(ItemDisplayMeta.DisplayContext.FIXED);
                meta.setScale(new Vec(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE));
                meta.setPosRotInterpolationDuration(3);
            });
            heads[i] = head;
            head.setInstance(owner.getInstance(), spot(i, puzzle.facing(i)));
        }
        this.watcher = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (owner.isRemoved() || owner.isDead()) remove();
        }).repeat(TaskSchedule.tick(WATCH_TICKS)).schedule();
    }

    void show(TotemPuzzle puzzle) {
        for (int i = 0; i < heads.length; i++) {
            if (!heads[i].isRemoved()) heads[i].teleport(spot(i, puzzle.facing(i)));
        }
    }

    void remove() {
        watcher.cancel();
        for (Entity head : heads) if (!head.isRemoved()) head.remove();
    }

    private Pos spot(int index, int facing) {
        return home.add(0, index * HEAD_HEIGHT + HEAD_HEIGHT / 2, 0).withView(facing * 90f, 0);
    }
}
