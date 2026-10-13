package net.swofty.type.skyblockgeneric.garden;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Rotation;
import net.swofty.type.generic.block.PlayerHeadBlockHandler;
import net.swofty.type.skyblockgeneric.entity.ArmorStandModels;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
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
        for (ArmorStandModels.Stand stand : ArmorStandModels.load(RESOURCE, model)) {
            stand.create().setInstance(instance, stand.position());
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
}
