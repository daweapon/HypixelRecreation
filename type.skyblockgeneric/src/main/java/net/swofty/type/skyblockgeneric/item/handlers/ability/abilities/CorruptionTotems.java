package net.swofty.type.skyblockgeneric.item.handlers.ability.abilities;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Totem of Corruption deployables. Fishing asks {@link #isCorrupting(SkyBlockPlayer)} whether a
 * caught sea creature should spawn Corrupted.
 */
public final class CorruptionTotems {
    private static final double RANGE = 30;
    private static final long DURATION_MS = 5 * 60 * 1000;
    private static final Map<UUID, LivingEntity> ACTIVE = new ConcurrentHashMap<>();

    private CorruptionTotems() {
    }

    public static boolean deploy(SkyBlockPlayer player) {
        if (player.getInstance() == null) return false;
        LivingEntity previous = ACTIVE.remove(player.getUuid());
        if (previous != null) previous.remove();

        LivingEntity totem = new LivingEntity(EntityType.ARMOR_STAND);
        ArmorStandMeta meta = (ArmorStandMeta) totem.getEntityMeta();
        meta.setInvisible(true);
        meta.setHasNoGravity(true);
        totem.setCustomName(Component.text("§5Totem of Corruption"));
        totem.setCustomNameVisible(true);
        totem.setEquipment(EquipmentSlot.HELMET, ItemStack.of(Material.PURPLE_BANNER));
        totem.setInstance(player.getInstance(), player.getPosition());
        ACTIVE.put(player.getUuid(), totem);

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (ACTIVE.remove(player.getUuid(), totem)) totem.remove();
        }).delay(TaskSchedule.millis(DURATION_MS)).schedule();
        player.sendMessage("<a>You placed a <5>Totem of Corruption<a>! It lasts <e>5 minutes<a>.");
        return true;
    }

    public static boolean isCorrupting(SkyBlockPlayer player) {
        LivingEntity totem = ACTIVE.get(player.getUuid());
        return totem != null && !totem.isRemoved() && totem.getInstance() == player.getInstance()
                && totem.getPosition().distance(player.getPosition()) <= RANGE;
    }
}
