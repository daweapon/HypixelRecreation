package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.SkullHeadComponent;
import dev.daweapon.type.skyblockgeneric.minion.MinionRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.entity.TextDisplayEntity;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

final class FurnitureEntities {
    static final String TOGGLED_ON = "on";
    private static final double SEAT_SPACING = 0.8;
    private static final double SEAT_DROP = 0.45;
    private static final double MINION_SCALE = 0.6;

    private FurnitureEntities() {
    }

    static SpawnedFurniture spawn(Instance instance, IslandFurniture owner, PlacedFurniture placed, FurnitureDefinition definition) {
        Pos pos = positionOf(placed);
        HologramSettings hologram = definition.interaction() == FurnitureInteraction.HOLOGRAM ? HologramSettings.decode(placed.data()) : null;
        ItemStack stack = hologram != null && !hologram.modelVisible() ? ItemStack.AIR : headStack(definition.itemId());
        LivingEntity display = displayEntity(stack, definition.scale(), definition.scale() / 2);
        display.setInstance(instance, pos);

        InteractionEntity hitbox = new InteractionEntity(definition.width(), definition.height(),
                (player, _) -> owner.interact((SkyBlockPlayer) player, placed, player.isSneaking()));
        hitbox.setInstance(instance, pos);

        SpawnedFurniture spawned = new SpawnedFurniture(display, hitbox);
        if (definition.interaction() == FurnitureInteraction.MINION_CHAIR && placed.data() != null) {
            ItemStack minion = minionStack(placed.data());
            if (minion != null) {
                spawned.minion = displayEntity(minion, MINION_SCALE, definition.height() * 0.6);
                spawned.minion.setInstance(instance, pos);
            }
        }
        if (definition.interaction() == FurnitureInteraction.TOGGLE && TOGGLED_ON.equals(placed.data())) {
            startFlames(instance, pos, definition, spawned);
        }
        if (hologram != null) startHologram(instance, pos, definition, hologram, spawned);
        return spawned;
    }

    private static void startHologram(Instance instance, Pos pos, FurnitureDefinition definition, HologramSettings hologram, SpawnedFurniture spawned) {
        if (hologram.hasText()) {
            String joined = hologram.lines().stream().map(line -> line.replace('&', '\u00a7')).collect(Collectors.joining("\n"));
            TextDisplayEntity text = new TextDisplayEntity(Text.legacy(joined), meta -> meta.setScale(new Vec(1, 1, 1)));
            text.setInstance(instance, pos.add(0, definition.height() + 0.4 + 0.15 * hologram.lines().size(), 0));
            spawned.text = text;
        }
        if (hologram.particle() == null) return;
        Color colour = HologramColours.of(hologram.particle());
        double y = pos.y() + definition.height() * 0.6;
        spawned.flames = MinecraftServer.getSchedulerManager().buildTask(() -> {
            ParticlePacket dust = new ParticlePacket(Particle.DUST.withColor(colour), pos.x(), y, pos.z(), 0.2f, 0.2f, 0.2f, 0f, 3);
            instance.getPlayers().forEach(viewer -> viewer.sendPacket(dust));
        }).repeat(TaskSchedule.tick(8)).schedule();
    }

    static Pos positionOf(PlacedFurniture placed) {
        return new Pos(placed.x() + 0.5, placed.y(), placed.z() + 0.5, placed.yaw(), 0);
    }

    static boolean sit(Instance instance, SkyBlockPlayer player, PlacedFurniture placed, FurnitureDefinition definition, SpawnedFurniture spawned) {
        spawned.seats.removeIf(seat -> seat.isRemoved());
        FurnitureSeat seat = spawned.seats.stream().filter(FurnitureSeat::isFree).findFirst().orElse(null);
        if (seat == null) {
            if (spawned.seats.size() >= definition.seats()) return false;
            seat = new FurnitureSeat();
            seat.setInstance(instance, seatPosition(placed, definition, spawned.seats.size())).join();
            spawned.seats.add(seat);
        }
        seat.addPassenger(player);
        return true;
    }

    private static Pos seatPosition(PlacedFurniture placed, FurnitureDefinition definition, int index) {
        Pos base = positionOf(placed);
        double offset = (index - (definition.seats() - 1) / 2.0) * SEAT_SPACING;
        double radians = Math.toRadians(placed.yaw());
        return base.add(Math.cos(radians) * offset, definition.height() * 0.6 - SEAT_DROP, Math.sin(radians) * offset);
    }

    static void startFlames(Instance instance, Pos pos, FurnitureDefinition definition, SpawnedFurniture spawned) {
        double y = pos.y() + definition.height() * 0.5;
        spawned.flames = MinecraftServer.getSchedulerManager().buildTask(() -> {
            ParticlePacket flame = new ParticlePacket(Particle.FLAME, pos.x(), y, pos.z(), 0.15f, 0.1f, 0.15f, 0.01f, 3);
            ParticlePacket smoke = new ParticlePacket(Particle.SMOKE, pos.x(), y + 0.3, pos.z(), 0.1f, 0.2f, 0.1f, 0.01f, 1);
            instance.getPlayers().forEach(viewer -> {
                viewer.sendPacket(flame);
                viewer.sendPacket(smoke);
            });
        }).repeat(TaskSchedule.tick(6)).schedule();
    }

    static ItemStack headStack(String itemId) {
        SkyBlockItem item = new SkyBlockItem(ItemType.get(itemId));
        if (!item.hasComponent(SkullHeadComponent.class)) return item.getItemStack();
        String texture = item.getComponent(SkullHeadComponent.class).getSkullTexture(item);
        return ItemStacks.head(texture, 1, Text.literal(""), List.of()).build();
    }

    static String minionData(MinionRegistry registry, int tier) {
        return registry.name() + ":" + tier;
    }

    static @Nullable SkyBlockItem minionItem(String data) {
        String[] parts = data.split(":");
        try {
            MinionRegistry registry = MinionRegistry.valueOf(parts[0]);
            SkyBlockItem item = new SkyBlockItem(registry.getItemType());
            item.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(Integer.parseInt(parts[1]), 0));
            return item;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static @Nullable ItemStack minionStack(String data) {
        SkyBlockItem item = minionItem(data);
        return item == null ? null : item.getItemStack();
    }

    private static LivingEntity displayEntity(ItemStack stack, double scale, double lift) {
        LivingEntity entity = new LivingEntity(EntityType.ITEM_DISPLAY);
        entity.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setHasNoGravity(true);
            meta.setScale(new Vec(scale, scale, scale));
            meta.setTranslation(new Vec(0, lift, 0));
            meta.setItemStack(stack);
        });
        return entity;
    }
}
