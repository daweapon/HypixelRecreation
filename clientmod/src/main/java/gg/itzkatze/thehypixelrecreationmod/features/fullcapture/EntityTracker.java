package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gg.itzkatze.thehypixelrecreationmod.utils.StringUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.UUID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class EntityTracker {
    private static final int TRACK_INTERVAL_TICKS = 10;
    private static final int FULL_TRACK_EVERY = 20;
    private static final int MAX_TRACKED = 400;
    private static final double TRACK_RADIUS = 96.0;
    private static final double MOVED_THRESHOLD = 0.02;

    private final EventSink sink;
    private final Map<Integer, double[]> lastPositions = new HashMap<>();
    private final Map<UUID, JsonObject> npcs = new LinkedHashMap<>();

    private int trackCountdown;
    private int samples;
    private int spawnCount;

    EntityTracker(EventSink sink) {
        this.sink = sink;
    }

    int spawnCount() {
        return spawnCount;
    }

    JsonArray npcs() {
        JsonArray array = new JsonArray();
        npcs.values().forEach(array::add);
        return array;
    }

    void reset() {
        lastPositions.clear();
    }

    void onAdd(ClientboundAddEntityPacket packet) {
        spawnCount++;
        sink.emit("entities", "entity:add", CaptureJson.fields(packet));
    }

    void onRemove(ClientboundRemoveEntitiesPacket packet) {
        sink.emit("entities", "entity:remove", CaptureJson.fields(packet));
    }

    void onEquipment(ClientboundSetEquipmentPacket packet) {
        sink.emit("entities", "entity:equipment", CaptureJson.fields(packet));
    }

    void onData(ClientboundSetEntityDataPacket packet) {
        JsonObject payload = new JsonObject();
        payload.addProperty("id", packet.id());

        Entity entity = entity(packet.id());
        if (entity != null) {
            payload.addProperty("type", entity.getType().toShortString());
            payload.addProperty("uuid", entity.getUUID().toString());
        }

        JsonObject values = new JsonObject();
        for (SynchedEntityData.DataValue<?> value : packet.packedItems()) {
            values.add(String.valueOf(value.id()), CaptureJson.encode(value.value()));
        }
        payload.add("values", values);
        sink.emit("entities", "entity:data", payload);
    }

    void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            return;
        }
        if (--trackCountdown > 0) {
            return;
        }
        trackCountdown = TRACK_INTERVAL_TICKS;

        boolean full = samples++ % FULL_TRACK_EVERY == 0;
        Set<Integer> seen = new HashSet<>();
        JsonArray entities = new JsonArray();

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entities.size() >= MAX_TRACKED) {
                break;
            }
            if (entity.distanceToSqr(client.player) > TRACK_RADIUS * TRACK_RADIUS) {
                continue;
            }

            captureNpc(client, entity);
            int id = entity.getId();
            seen.add(id);
            double[] position = {entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot()};
            double[] previous = lastPositions.put(id, position);
            if (!full && previous != null && !moved(previous, position)) {
                continue;
            }

            JsonObject json = new JsonObject();
            json.addProperty("id", id);
            json.addProperty("type", entity.getType().toShortString());
            json.addProperty("x", CaptureJson.round(position[0]));
            json.addProperty("y", CaptureJson.round(position[1]));
            json.addProperty("z", CaptureJson.round(position[2]));
            json.addProperty("yaw", CaptureJson.round(position[3]));
            json.addProperty("pitch", CaptureJson.round(position[4]));
            if (entity.getCustomName() != null) {
                json.addProperty("name", StringUtility.toLegacyString(entity.getCustomName()));
            }
            entities.add(json);
        }

        lastPositions.keySet().retainAll(seen);
        if (entities.isEmpty() && !full) {
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("full", full);
        payload.addProperty("count", entities.size());
        payload.add("entities", entities);
        sink.emit("entities", "entity:track", payload);
    }

    private void captureNpc(Minecraft client, Entity entity) {
        if (entity == client.player || !(entity instanceof LivingEntity) || entity instanceof ArmorStand) {
            return;
        }
        GameProfile profile = profileOf(entity);
        boolean human = entity instanceof Player || profile != null;
        if (!human && entity.getCustomName() == null) {
            return;
        }
        JsonObject previous = npcs.get(entity.getUUID());
        if (previous != null && Math.abs(previous.get("x").getAsDouble() - entity.getX()) < 0.5
                && Math.abs(previous.get("z").getAsDouble() - entity.getZ()) < 0.5) {
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty("uuid", entity.getUUID().toString());
        json.addProperty("id", entity.getId());
        json.addProperty("type", entity.getType().toShortString());
        json.addProperty("x", CaptureJson.round(entity.getX()));
        json.addProperty("y", CaptureJson.round(entity.getY()));
        json.addProperty("z", CaptureJson.round(entity.getZ()));
        json.addProperty("yaw", CaptureJson.round(entity.getYRot()));
        json.addProperty("pitch", CaptureJson.round(entity.getXRot()));
        if (entity.getCustomName() != null) {
            json.addProperty("name", StringUtility.toLegacyString(entity.getCustomName()));
        }
        JsonArray lines = overheadLines(client, entity);
        json.add("holograms", lines);
        if (profile != null) {
            json.addProperty("profileName", profile.name());
            for (Property property : profile.properties().get("textures")) {
                json.addProperty("texture", property.value());
                json.addProperty("signature", property.signature());
                break;
            }
        }
        npcs.put(entity.getUUID(), json);
        sink.emit("entities", "entity:npc", json);
    }

    private static JsonArray overheadLines(Minecraft client, Entity npc) {
        JsonArray lines = new JsonArray();
        if (!(npc.level() instanceof ClientLevel level)) {
            return lines;
        }
        AABB box = npc.getBoundingBox().inflate(0.5, 2.5, 0.5);
        java.util.List<Entity> found = new java.util.ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity != npc && entity.getBoundingBox().intersects(box)
                    && (entity instanceof Display.TextDisplay || entity instanceof ArmorStand) && entity.hasCustomName()) {
                found.add(entity);
            }
        }
        found.sort((a, b) -> Double.compare(b.getY(), a.getY()));
        for (Entity entity : found) {
            lines.add(StringUtility.toLegacyString(entity.getCustomName()));
        }
        return lines;
    }

    private static GameProfile profileOf(Entity entity) {
        if (entity instanceof Player player) {
            return player.getGameProfile();
        }
        for (String name : new String[]{"getProfile", "profile", "getGameProfile"}) {
            try {
                Method method = entity.getClass().getMethod(name);
                Object value = method.invoke(entity);
                if (value instanceof GameProfile profile) {
                    return profile;
                }
                if (value != null) {
                    for (String inner : new String[]{"partialProfile", "gameProfile", "profile"}) {
                        try {
                            Object nested = value.getClass().getMethod(inner).invoke(value);
                            if (nested instanceof GameProfile profile) {
                                return profile;
                            }
                        } catch (ReflectiveOperationException ignored) {
                        }
                    }
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private static boolean moved(double[] previous, double[] position) {
        for (int index = 0; index < previous.length; index++) {
            if (Math.abs(previous[index] - position[index]) > MOVED_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private static Entity entity(int id) {
        Minecraft client = Minecraft.getInstance();
        return client.level == null ? null : client.level.getEntity(id);
    }
}
