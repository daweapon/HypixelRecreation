package net.swofty.type.skyblockgeneric.garden;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Rotation;
import net.swofty.type.generic.block.PlayerHeadBlockHandler;
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

    public static void spawnFrames(Instance instance) {
        for (Object frame : load().getJSONArray("frames")) {
            JSONObject spec = (JSONObject) frame;
            Material material = Material.fromKey("minecraft:" + spec.getString("item"));
            if (material == null || material == Material.AIR) continue;
            Entity entity = new Entity(EntityType.ITEM_FRAME);
            ItemFrameMeta meta = (ItemFrameMeta) entity.getEntityMeta();
            meta.setDirection(Direction.values()[spec.getInt("dir")]);
            meta.setItem(ItemStack.of(material));
            meta.setRotation(Rotation.values()[spec.optInt("rot") % Rotation.values().length]);
            entity.setInstance(instance, new Pos(spec.getInt("x"), spec.getInt("y"), spec.getInt("z")));
        }
    }

    public static void applySkulls(Instance instance) {
        for (Object skull : load().getJSONArray("skulls")) {
            JSONObject spec = (JSONObject) skull;
            int x = spec.getInt("x"), y = spec.getInt("y"), z = spec.getInt("z");
            if (instance.getChunkAt(x, z) == null) continue;
            Block block = instance.getBlock(x, y, z);
            if (!block.name().endsWith("_head") && !block.name().endsWith("_skull")) continue;
            CompoundBinaryTag profile = CompoundBinaryTag.builder().put("properties",
                    ListBinaryTag.builder(BinaryTagTypes.COMPOUND).add(CompoundBinaryTag.builder()
                            .putString("name", "textures")
                            .putString("value", spec.getString("value"))
                            .build()).build()).build();
            instance.setBlock(x, y, z, block.withHandler(new PlayerHeadBlockHandler()).withTag(Tag.NBT("profile"), profile));
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
