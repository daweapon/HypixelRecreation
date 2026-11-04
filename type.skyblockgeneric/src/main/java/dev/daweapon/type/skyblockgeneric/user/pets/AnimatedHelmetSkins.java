package dev.daweapon.type.skyblockgeneric.user.pets;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.timer.TaskSchedule;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AnimatedHelmetSkins {
    private static final int RESCAN_TICKS = 20;
    private static final Map<UUID, Worn> WORN = new ConcurrentHashMap<>();
    private static long tick;

    private AnimatedHelmetSkins() {
    }

    private record Worn(SkyBlockPlayer player, AnimatedSkins.Animation animation) {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(AnimatedHelmetSkins::tick)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    private static void tick() {
        tick++;
        if (tick % RESCAN_TICKS == 0) rescan();
        for (Worn worn : WORN.values()) {
            SkyBlockPlayer player = worn.player();
            if (!player.isOnline()) continue;
            AnimatedSkins.Animation animation = worn.animation();
            if (tick % animation.ticks() != 0) continue;
            int frame = (int) ((tick / animation.ticks()) % animation.heads().size());
            ItemStack head = animation.heads().get(frame);
            player.sendPacketToViewers(new EntityEquipmentPacket(player.getEntityId(), Map.of(EquipmentSlot.HELMET, head)));
        }
    }

    private static void rescan() {
        WORN.clear();
        SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
            ItemStack helmet = player.getHelmet();
            if (helmet.isAir()) return;
            ItemType skin = new SkyBlockItem(helmet).getAttributeHandler().getItemSkin();
            AnimatedSkins.Animation animation = AnimatedSkins.get(skin == null ? null : skin.name());
            if (animation != null) WORN.put(player.getUuid(), new Worn(player, animation));
        });
    }
}
