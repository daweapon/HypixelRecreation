package net.swofty.type.skyblockgeneric.garden;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class GardenModels {
    private static final String RESOURCE = "/garden_models.json";

    private GardenModels() {
    }

    public static void spawn(Instance instance, String model) {
        for (Object stand : load().getJSONArray(model)) {
            spawnStand(instance, (JSONObject) stand);
        }
    }

    private static JSONObject load() {
        try (InputStream stream = GardenModels.class.getResourceAsStream(RESOURCE)) {
            return new JSONObject(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void spawnStand(Instance instance, JSONObject spec) {
        LivingEntity stand = new LivingEntity(EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setSmall(spec.optBoolean("small"));
            meta.setHasArms(spec.optBoolean("arms"));
            meta.setHasNoBasePlate(true);
            meta.setMarker(true);
            apply(spec, "head", meta::setHeadRotation);
            apply(spec, "larm", meta::setLeftArmRotation);
            apply(spec, "rarm", meta::setRightArmRotation);
        });
        equip(stand, spec, "head", EquipmentSlot.HELMET);
        equip(stand, spec, "mainhand", EquipmentSlot.MAIN_HAND);
        stand.setInstance(instance, new Pos(spec.getDouble("x"), spec.getDouble("y"), spec.getDouble("z"), (float) spec.optDouble("yaw", 0), 0));
    }

    private static void apply(JSONObject spec, String key, java.util.function.Consumer<Vec> setter) {
        JSONArray angles = spec.optJSONArray(key);
        if (angles != null) setter.accept(new Vec(angles.getDouble(0), angles.getDouble(1), angles.getDouble(2)));
    }

    private static void equip(LivingEntity stand, JSONObject spec, String key, EquipmentSlot slot) {
        JSONObject item = spec.optJSONObject(key);
        if (item == null) return;
        ItemStack stack = item.has("texture")
                ? ItemStacks.head(item.getString("texture"), "").build()
                : ItemStack.of(Material.fromKey("minecraft:" + item.getString("item")));
        stand.setEquipment(slot, stack);
    }
}
