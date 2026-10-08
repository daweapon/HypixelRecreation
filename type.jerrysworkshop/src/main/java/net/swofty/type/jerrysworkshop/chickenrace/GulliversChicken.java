package net.swofty.type.jerrysworkshop.chickenrace;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.entity.drop.ItemDrops;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The chicken beside Gulliver. Like a vanilla chicken it lays an egg every 5-10 minutes, one for
 * each player nearby, and has a 0.3% chance to lay Cluck the Fish instead.
 */
public final class GulliversChicken {
    private static final Pos POSITION = new Pos(70.5, 105, 35.5, 120, 0);
    private static final double CLUCK_THE_FISH_PERCENT = 0.3;
    private static final double NEARBY = 16;

    private GulliversChicken() {
    }

    public static void spawn() {
        Instance instance = HypixelConst.getInstanceContainer();
        LivingEntity chicken = new LivingEntity(EntityType.CHICKEN);
        chicken.setInvulnerable(true);
        chicken.setInstance(instance, POSITION);
        scheduleEgg(instance);
    }

    private static void scheduleEgg(Instance instance) {
        int delayTicks = ThreadLocalRandom.current().nextInt(6000, 12001);
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            layEggs(instance);
            scheduleEgg(instance);
        }).delay(TaskSchedule.tick(delayTicks)).schedule();
    }

    private static void layEggs(Instance instance) {
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (player.getInstance() != instance || player.getPosition().distance(POSITION) > NEARBY) continue;
            ItemType laid = LootRoll.roll(player.getUuid(), ItemType.CLUCK_THE_FISH, CLUCK_THE_FISH_PERCENT)
                    ? ItemType.CLUCK_THE_FISH : ItemType.EGG;
            ItemDrops.dropAt(new DroppedItemEntityImpl(new SkyBlockItem(laid), player), instance, POSITION);
        }
    }
}
