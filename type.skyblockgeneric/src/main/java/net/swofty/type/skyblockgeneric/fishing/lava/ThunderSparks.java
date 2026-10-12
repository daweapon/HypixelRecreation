package net.swofty.type.skyblockgeneric.fishing.lava;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * When Thunder dies it bursts into 5–9 sparks (wiki: Thunder). Right-clicking one while holding an Empty Thunder
 * Bottle charges it by 250. How long sparks stay is not on the wiki; 30 seconds is an estimate.
 */
final class ThunderSparks {
    static final int MIN_SPARKS = 5;
    static final int MAX_SPARKS = 9;
    static final int CHARGE_PER_SPARK = 250;
    private static final int LIFETIME_SECONDS = 30;

    private ThunderSparks() {
    }

    static void burst(Instance instance, Pos at) {
        int count = ThreadLocalRandom.current().nextInt(MIN_SPARKS, MAX_SPARKS + 1);
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count;
            spawn(instance, at.add(Math.cos(angle) * 2, 0.5, Math.sin(angle) * 2));
        }
    }

    private static void spawn(Instance instance, Pos at) {
        Entity spark = new Entity(EntityType.ITEM_DISPLAY);
        spark.setNoGravity(true);
        spark.editEntityMeta(ItemDisplayMeta.class, meta -> meta.setItemStack(ItemStack.of(Material.NETHER_STAR)));
        spark.setInstance(instance, at);
        InteractionEntity hitbox = new InteractionEntity(0.8f, 0.8f, (player, _) -> collect((SkyBlockPlayer) player, spark));
        hitbox.setInstance(instance, at.sub(0, 0.4, 0));
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            spark.remove();
            hitbox.remove();
        }, TaskSchedule.seconds(LIFETIME_SECONDS), TaskSchedule.stop());
    }

    private static void collect(SkyBlockPlayer player, Entity spark) {
        if (spark.isRemoved()) return;
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        if (held.getAttributeHandler().getPotentialType() == null
                || ThunderBottles.tierOfEmpty(held.getAttributeHandler().getPotentialType().name()).isEmpty()) {
            player.sendMessage("<c>You need to hold an Empty Thunder Bottle to collect this spark!");
            return;
        }
        spark.remove();
        ThunderBottles.charge(player, CHARGE_PER_SPARK);
    }
}
