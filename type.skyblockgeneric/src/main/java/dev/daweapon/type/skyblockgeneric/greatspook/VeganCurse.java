package dev.daweapon.type.skyblockgeneric.greatspook;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VeganCurse {
    public static final int DURATION_TICKS = 15 * 20;
    public static final List<EntityType> MOBS = List.of(EntityType.PIG, EntityType.COW, EntityType.CHICKEN,
            EntityType.SHEEP, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
            EntityType.SLIME, EntityType.ENDERMAN, EntityType.WOLF, EntityType.RABBIT, EntityType.VILLAGER);

    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.MAIN_HAND, EquipmentSlot.OFF_HAND,
            EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS};
    private static final Map<UUID, Entity> DISGUISES = new ConcurrentHashMap<>();

    private VeganCurse() {
    }

    public static boolean isTransformed(SkyBlockPlayer player) {
        return DISGUISES.containsKey(player.getUuid());
    }

    public static void apply(SkyBlockPlayer player, EntityType mob) {
        if (player.getInstance() == null || isTransformed(player)) return;
        Entity disguise = new Entity(mob);
        disguise.updateViewableRule(viewer -> viewer != player);
        disguise.setInstance(player.getInstance(), player.getPosition()).thenRun(() -> player.addPassenger(disguise));
        player.setInvisible(true);
        sendEquipment(player, false);
        DISGUISES.put(player.getUuid(), disguise);
        player.sendMessage("<c>The Vegan Crawler turned you into a <e>" + mob.name().replace("minecraft:", "") + "<c>!");
        MinecraftServer.getSchedulerManager().scheduleTask(() -> restore(player), TaskSchedule.tick(DURATION_TICKS),
                TaskSchedule.stop());
    }

    public static void restore(SkyBlockPlayer player) {
        Entity disguise = DISGUISES.remove(player.getUuid());
        if (disguise == null) return;
        disguise.remove();
        player.setInvisible(false);
        sendEquipment(player, true);
    }

    private static void sendEquipment(SkyBlockPlayer player, boolean visible) {
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : SLOTS) equipment.put(slot, visible ? player.getEquipment(slot) : ItemStack.AIR);
        player.sendPacketToViewers(new EntityEquipmentPacket(player.getEntityId(), equipment));
    }
}
