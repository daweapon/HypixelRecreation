package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class GreatSpookStaff {
    public static final long SPOOK_COOLDOWN_MILLIS = 15_000;
    public static final long SPOOKER_COOLDOWN_MILLIS = 60_000;
    public static final long DISGUISE_TICKS = 200;
    public static final long NEW_PROFILE_MILLIS = 10L * 60 * 60 * 1000;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.MAIN_HAND, EquipmentSlot.OFF_HAND,
            EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS};
    private static final Map<UUID, Long> SPOOK_READY = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> SPOOKER_READY = new ConcurrentHashMap<>();

    private GreatSpookStaff() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(GreatSpookFear.STAFF_ID, InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> spook(player)).build());
    }

    public static long remainingMillis(Map<UUID, Long> ready, UUID id, long now) {
        return Math.max(0, ready.getOrDefault(id, 0L) - now);
    }

    public static void spook(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        long remaining = remainingMillis(SPOOK_READY, player.getUuid(), now);
        if (remaining > 0) {
            player.sendMessage("<c>This ability is on cooldown for {}s.", Math.ceil(remaining / 1000D));
            return;
        }
        SPOOK_READY.put(player.getUuid(), now + SPOOK_COOLDOWN_MILLIS);
        player.removeEffect(PotionEffect.NIGHT_VISION);
        disguise(player);
        player.sendMessage("<5><l>WOAH! <d>your staff turned you into a <a>Dead Bush<d>!");
    }

    public static void spooker(SkyBlockPlayer spooker, SkyBlockPlayer target) {
        if (spooker == target || !GreatSpookFear.wearsFullSet(spooker)) return;
        long now = System.currentTimeMillis();
        long remaining = remainingMillis(SPOOKER_READY, spooker.getUuid(), now);
        if (remaining > 0) {
            spooker.sendMessage("<c>This ability is on cooldown for {}s.", Math.ceil(remaining / 1000D));
            return;
        }
        if (isNew(target)) {
            spooker.sendMessage("<c>This player is too new to be spooked!");
            return;
        }
        SPOOKER_READY.put(spooker.getUuid(), now + SPOOKER_COOLDOWN_MILLIS);
        SpookerMessages.Spook spook = SpookerMessages.random(ThreadLocalRandom.current());
        spooker.sendMessage("<e>You <8><o>spooked <7>{} <e>with '<7>{}<e>'!", target.getUsername(), spook.name());
        target.sendMessage(SpookerMessages.reveal(ThreadLocalRandom.current(), spooker.getUsername()));
        SpookerMessages.play(target, spook);
    }

    private static boolean isNew(SkyBlockPlayer target) {
        return System.currentTimeMillis() - target.getSkyblockDataHandler()
                .get(dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler.Data.CREATED,
                        dev.daweapon.type.generic.data.datapoints.DatapointLong.class).getValue() < NEW_PROFILE_MILLIS;
    }

    private static void disguise(SkyBlockPlayer player) {
        if (player.getInstance() == null) return;
        Entity bush = new Entity(EntityType.BLOCK_DISPLAY);
        bush.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(Block.DEAD_BUSH);
            meta.setTranslation(new Vec(-0.5, 0, -0.5));
            meta.setHasNoGravity(true);
        });
        bush.updateViewableRule(viewer -> viewer != player);
        bush.setInstance(player.getInstance(), player.getPosition()).thenRun(() -> player.addPassenger(bush));
        player.setInvisible(true);
        sendEquipment(player, false);
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            bush.remove();
            player.setInvisible(false);
            sendEquipment(player, true);
        }).delay(TaskSchedule.tick((int) DISGUISE_TICKS)).schedule();
    }

    private static void sendEquipment(SkyBlockPlayer player, boolean visible) {
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : SLOTS) equipment.put(slot, visible ? player.getEquipment(slot) : ItemStack.AIR);
        player.sendPacketToViewers(new EntityEquipmentPacket(player.getEntityId(), equipment));
    }
}
