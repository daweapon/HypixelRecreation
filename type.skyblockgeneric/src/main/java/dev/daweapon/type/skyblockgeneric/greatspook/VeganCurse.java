package dev.daweapon.type.skyblockgeneric.greatspook;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VeganCurse {
    public enum Animal {
        PIG(EntityType.PIG, Material.CARROT),
        COW(EntityType.COW, Material.HAY_BLOCK),
        CHICKEN(EntityType.CHICKEN, Material.WHEAT_SEEDS);

        private final EntityType entityType;
        private final Material liked;

        Animal(EntityType entityType, Material liked) {
            this.entityType = entityType;
            this.liked = liked;
        }

        public Material liked() {
            return liked;
        }
    }

    public static final List<Material> FOOD = List.of(Material.CARROT, Material.HAY_BLOCK, Material.WHEAT_SEEDS);
    public static final double PICKUP_RANGE = 1.4;

    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.MAIN_HAND, EquipmentSlot.OFF_HAND,
            EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS};
    private static final Map<UUID, Curse> CURSES = new ConcurrentHashMap<>();

    public record Collected(Animal animal, Material food) {
        public boolean liked() {
            return food == animal.liked();
        }
    }

    private record Curse(Animal animal, Entity disguise, List<Entity> food) {
    }

    private VeganCurse() {
    }

    public static boolean isTransformed(SkyBlockPlayer player) {
        return CURSES.containsKey(player.getUuid());
    }

    public static void apply(SkyBlockPlayer player, Animal animal, List<net.minestom.server.coordinate.Pos> foodSpots) {
        if (player.getInstance() == null || isTransformed(player)) return;
        Entity disguise = new Entity(animal.entityType);
        disguise.updateViewableRule(viewer -> viewer != player);
        disguise.setInstance(player.getInstance(), player.getPosition()).thenRun(() -> player.addPassenger(disguise));
        player.setInvisible(true);
        sendEquipment(player, false);
        List<Entity> food = new ArrayList<>();
        for (int i = 0; i < FOOD.size(); i++) {
            Entity display = new Entity(EntityType.ITEM_DISPLAY);
            Material material = FOOD.get(i);
            display.editEntityMeta(ItemDisplayMeta.class, meta -> {
                meta.setItemStack(ItemStack.of(material));
                meta.setHasNoGravity(true);
            });
            display.setInstance(player.getInstance(), foodSpots.get(i).add(0, 0.4, 0));
            food.add(display);
        }
        CURSES.put(player.getUuid(), new Curse(animal, disguise, food));
        player.sendMessage("<c>The Vegan Crawler turned you into a <e>" + animal.name().toLowerCase() + "<c>! Grab some food to change back!");
    }

    public static Collected collected(SkyBlockPlayer player) {
        Curse curse = CURSES.get(player.getUuid());
        if (curse == null) return null;
        for (int i = 0; i < curse.food().size(); i++) {
            Entity item = curse.food().get(i);
            if (item.isRemoved() || item.getPosition().distance(player.getPosition()) > PICKUP_RANGE) continue;
            restore(player);
            return new Collected(curse.animal(), FOOD.get(i));
        }
        return null;
    }

    public static void restore(SkyBlockPlayer player) {
        Curse curse = CURSES.remove(player.getUuid());
        if (curse == null) return;
        curse.disguise().remove();
        curse.food().forEach(entity -> {
            if (!entity.isRemoved()) entity.remove();
        });
        player.setInvisible(false);
        sendEquipment(player, true);
    }

    private static void sendEquipment(SkyBlockPlayer player, boolean visible) {
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : SLOTS) equipment.put(slot, visible ? player.getEquipment(slot) : ItemStack.AIR);
        player.sendPacketToViewers(new EntityEquipmentPacket(player.getEntityId(), equipment));
    }
}
