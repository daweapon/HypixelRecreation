package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.drop.ItemDrops;
import dev.daweapon.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMob;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMobs;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Wiki (Flint Skeleton): a killed Flint Skeleton leaves a Bone on the ground; if it is not picked up within
 * 5 seconds, another Flint Skeleton respawns where the Bone lay.
 */
public class MobFlintSkeleton extends SpiderDenMob {
    private static final int BONE_RESPAWN_SECONDS = 5;

    public MobFlintSkeleton() {
        super(SpiderDenMobs.FLINT_SKELETON);
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        Instance instance = getInstance();
        if (instance == null) return;
        DroppedItemEntityImpl bone = ItemDrops.dropAt(new DroppedItemEntityImpl(new SkyBlockItem(ItemType.BONE), killer),
                instance, getPosition().add(0, 0.5, 0));
        MinecraftServer.getSchedulerManager().buildTask(() -> respawnIfIgnored(bone, instance))
                .delay(TaskSchedule.seconds(BONE_RESPAWN_SECONDS)).schedule();
    }

    private static void respawnIfIgnored(DroppedItemEntityImpl bone, Instance instance) {
        if (bone.isRemoved() || bone.getInstance() != instance) return;
        Pos at = bone.getPosition();
        bone.remove();
        new MobFlintSkeleton().setInstance(instance, at);
    }
}
