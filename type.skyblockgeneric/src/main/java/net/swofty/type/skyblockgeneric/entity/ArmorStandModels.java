package net.swofty.type.skyblockgeneric.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ArmorStandModels {
    private ArmorStandModels() {
    }

    public record Stand(JSONObject spec) {
        public Pos position() {
            return new Pos(spec.getDouble("x"), spec.getDouble("y"), spec.getDouble("z"), (float) spec.optDouble("yaw", 0), 0);
        }

        public LivingEntity create() {
            LivingEntity stand = new LivingEntity(EntityType.ARMOR_STAND);
            stand.setInvisible(true);
            stand.setNoGravity(true);
            stand.setInvulnerable(true);
            stand.editEntityMeta(ArmorStandMeta.class, meta -> {
                meta.setSmall(spec.optBoolean("small"));
                meta.setHasArms(spec.optBoolean("arms"));
                meta.setHasNoBasePlate(true);
                meta.setMarker(true);
                pose(spec, "headpose", meta::setHeadRotation);
                pose(spec, "larm", meta::setLeftArmRotation);
                pose(spec, "rarm", meta::setRightArmRotation);
            });
            equip(stand, "head", EquipmentSlot.HELMET);
            equip(stand, "mainhand", EquipmentSlot.MAIN_HAND);
            return stand;
        }

        private void equip(LivingEntity stand, String key, EquipmentSlot slot) {
            JSONObject item = spec.optJSONObject(key);
            if (item == null) return;
            ItemStack stack = item.has("texture")
                    ? ItemStacks.head(item.getString("texture"), "").build()
                    : ItemStack.of(Material.fromKey("minecraft:" + item.getString("item")));
            stand.setEquipment(slot, stack);
        }
    }

    public static List<Stand> load(String resource, String key) {
        try (InputStream stream = ArmorStandModels.class.getResourceAsStream(resource)) {
            JSONArray array = new JSONObject(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getJSONArray(key);
            List<Stand> stands = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) stands.add(new Stand(array.getJSONObject(i)));
            return stands;
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void pose(JSONObject spec, String key, Consumer<Vec> setter) {
        @Nullable JSONArray angles = spec.optJSONArray(key);
        if (angles != null) setter.accept(new Vec(angles.getDouble(0), angles.getDouble(1), angles.getDouble(2)));
    }
}
