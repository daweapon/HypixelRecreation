package gg.itzkatze.thehypixelrecreationmod.features.schematic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class SchematicExporter {
    public static final long MAX_VOLUME = 2_000_000L;
    private static final int SCHEMATIC_VERSION = 3;
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9._-]+");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SchematicExporter() {
    }

    public record Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public static Bounds of(BlockPos a, BlockPos b) {
            return new Bounds(
                Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()),
                Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ())
            );
        }

        public int width() {
            return maxX - minX + 1;
        }

        public int height() {
            return maxY - minY + 1;
        }

        public int length() {
            return maxZ - minZ + 1;
        }

        public long volume() {
            return (long) width() * height() * length();
        }

        public int chunkCount() {
            return (SectionPos.blockToSectionCoord(maxX) - SectionPos.blockToSectionCoord(minX) + 1)
                * (SectionPos.blockToSectionCoord(maxZ) - SectionPos.blockToSectionCoord(minZ) + 1);
        }
    }

    public record SaveResult(
        Path schematicPath,
        Path metadataPath,
        Bounds bounds,
        long blockCount,
        long nonAirBlockCount,
        int blockEntityCount,
        int entityCount,
        int unloadedChunkCount,
        int paletteSize,
        long bytes
    ) {
    }

    public static int countUnloadedChunks(ClientLevel level, Bounds bounds) {
        int unloaded = 0;
        for (int chunkX = SectionPos.blockToSectionCoord(bounds.minX()); chunkX <= SectionPos.blockToSectionCoord(bounds.maxX()); chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(bounds.minZ()); chunkZ <= SectionPos.blockToSectionCoord(bounds.maxZ()); chunkZ++) {
                if (loadedChunk(level, chunkX, chunkZ) == null) {
                    unloaded++;
                }
            }
        }
        return unloaded;
    }

    public static SaveResult save(String name) throws IOException {
        if (!VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Name may only contain letters, numbers, dots, dashes and underscores.");
        }

        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (client.player == null || level == null) {
            throw new IllegalStateException("No world is currently loaded.");
        }
        if (!SchematicSelection.isComplete()) {
            throw new IllegalStateException("Set both pos1 and pos2 first.");
        }
        String dimension = level.dimension().identifier().toString();
        if (!dimension.equals(SchematicSelection.dimension())) {
            throw new IllegalStateException("The selection was made in " + SchematicSelection.dimension() + ", you are in " + dimension + ".");
        }

        Bounds bounds = Bounds.of(SchematicSelection.pos1(), SchematicSelection.pos2());
        if (bounds.volume() > MAX_VOLUME) {
            throw new IllegalStateException("Selection is " + bounds.volume() + " blocks, above the " + MAX_VOLUME + " block limit.");
        }
        if (bounds.minY() < level.getMinY() || bounds.maxY() > level.getMaxY()) {
            throw new IllegalStateException("Selection leaves the world height range " + level.getMinY() + " to " + level.getMaxY() + ".");
        }

        int width = bounds.width();
        int height = bounds.height();
        int length = bounds.length();
        int unloaded = 0;
        long nonAir = 0;
        Map<String, Integer> palette = new LinkedHashMap<>();
        Map<BlockState, Integer> stateIndex = new HashMap<>();
        ByteArrayOutputStream data = new ByteArrayOutputStream((int) Math.min(bounds.volume(), 8_000_000L));
        int[] indices = new int[(int) bounds.volume()];
        ListTag blockEntities = new ListTag();
        int airIndex = paletteIndex(palette, "minecraft:air");

        for (int chunkX = SectionPos.blockToSectionCoord(bounds.minX()); chunkX <= SectionPos.blockToSectionCoord(bounds.maxX()); chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(bounds.minZ()); chunkZ <= SectionPos.blockToSectionCoord(bounds.maxZ()); chunkZ++) {
                LevelChunk chunk = loadedChunk(level, chunkX, chunkZ);
                int fromX = Math.max(bounds.minX(), chunkX << 4);
                int toX = Math.min(bounds.maxX(), (chunkX << 4) + 15);
                int fromZ = Math.max(bounds.minZ(), chunkZ << 4);
                int toZ = Math.min(bounds.maxZ(), (chunkZ << 4) + 15);
                if (chunk == null) {
                    unloaded++;
                    for (int z = fromZ; z <= toZ; z++) {
                        for (int x = fromX; x <= toX; x++) {
                            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                                indices[index(bounds, x, y, z)] = airIndex;
                            }
                        }
                    }
                    continue;
                }

                BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
                for (int z = fromZ; z <= toZ; z++) {
                    for (int x = fromX; x <= toX; x++) {
                        for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                            cursor.set(x, y, z);
                            BlockState state = chunk.getBlockState(cursor);
                            int paletteIndex = stateIndex.computeIfAbsent(
                                state,
                                key -> paletteIndex(palette, BlockStateParser.serialize(key))
                            );
                            indices[index(bounds, x, y, z)] = paletteIndex;
                            if (!state.isAir()) {
                                nonAir++;
                            }
                        }
                    }
                }

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (pos.getX() < bounds.minX() || pos.getX() > bounds.maxX()
                        || pos.getY() < bounds.minY() || pos.getY() > bounds.maxY()
                        || pos.getZ() < bounds.minZ() || pos.getZ() > bounds.maxZ()) {
                        continue;
                    }
                    blockEntities.add(blockEntityTag(level, blockEntity, bounds));
                }
            }
        }

        for (int blockIndex : indices) {
            writeVarInt(data, blockIndex);
        }

        ListTag entities = captureEntities(level, bounds);

        CompoundTag paletteTag = new CompoundTag();
        palette.forEach(paletteTag::putInt);

        CompoundTag blocks = new CompoundTag();
        blocks.put("Palette", paletteTag);
        blocks.putByteArray("Data", data.toByteArray());
        blocks.put("BlockEntities", blockEntities);

        CompoundTag schematic = new CompoundTag();
        schematic.putInt("Version", SCHEMATIC_VERSION);
        schematic.putInt("DataVersion", SharedConstants.getCurrentVersion().dataVersion().version());
        schematic.putShort("Width", (short) width);
        schematic.putShort("Height", (short) height);
        schematic.putShort("Length", (short) length);
        schematic.putIntArray("Offset", new int[]{bounds.minX(), bounds.minY(), bounds.minZ()});
        schematic.put("Blocks", blocks);
        if (!entities.isEmpty()) {
            schematic.put("Entities", entities);
        }

        CompoundTag metadata = new CompoundTag();
        metadata.putString("Name", name);
        metadata.putString("Date", Instant.now().toString());
        metadata.putString("Generator", "thehypixelrecreationmod");
        schematic.put("Metadata", metadata);

        CompoundTag root = new CompoundTag();
        root.put("Schematic", schematic);

        Path directory = client.gameDirectory.toPath().resolve("schematics");
        Files.createDirectories(directory);
        Path schematicPath = directory.resolve(name + ".schem");
        Path metadataPath = directory.resolve(name + ".json");
        NbtIo.writeCompressed(root, schematicPath);

        SaveResult result = new SaveResult(
            schematicPath, metadataPath, bounds, bounds.volume(), nonAir,
            blockEntities.size(), entities.size(), unloaded, palette.size(), Files.size(schematicPath)
        );
        Files.writeString(metadataPath, GSON.toJson(metadataJson(client, level, dimension, result)));
        return result;
    }

    private static JsonObject metadataJson(Minecraft client, ClientLevel level, String dimension, SaveResult result) {
        Bounds bounds = result.bounds();
        JsonObject json = new JsonObject();
        json.addProperty("schematic", result.schematicPath().getFileName().toString());
        json.addProperty("spongeVersion", SCHEMATIC_VERSION);
        json.add("min", point(bounds.minX(), bounds.minY(), bounds.minZ()));
        json.add("max", point(bounds.maxX(), bounds.maxY(), bounds.maxZ()));
        JsonObject size = point(bounds.width(), bounds.height(), bounds.length());
        json.add("size", size);
        json.addProperty("dimension", dimension);
        JsonObject player = new JsonObject();
        player.addProperty("x", client.player.getX());
        player.addProperty("y", client.player.getY());
        player.addProperty("z", client.player.getZ());
        player.addProperty("yaw", client.player.getYRot());
        player.addProperty("pitch", client.player.getXRot());
        json.add("player", player);
        ServerData server = client.getCurrentServer();
        json.addProperty("server", client.hasSingleplayerServer() ? "singleplayer" : server == null ? "unknown" : server.ip);
        json.addProperty("timestamp", Instant.now().toString());
        json.addProperty("blockCount", result.blockCount());
        json.addProperty("nonAirBlockCount", result.nonAirBlockCount());
        json.addProperty("blockEntityCount", result.blockEntityCount());
        json.addProperty("entityCount", result.entityCount());
        json.addProperty("unloadedChunkCount", result.unloadedChunkCount());
        json.addProperty("minecraftVersion", SharedConstants.getCurrentVersion().name());
        json.addProperty("dataVersion", SharedConstants.getCurrentVersion().dataVersion().version());
        return json;
    }

    private static JsonObject point(int x, int y, int z) {
        JsonObject point = new JsonObject();
        point.addProperty("x", x);
        point.addProperty("y", y);
        point.addProperty("z", z);
        return point;
    }

    private static CompoundTag blockEntityTag(ClientLevel level, BlockEntity blockEntity, Bounds bounds) {
        CompoundTag full = blockEntity.saveWithFullMetadata(level.registryAccess());
        String id = full.getStringOr("id", "");
        BlockPos pos = blockEntity.getBlockPos();
        full.remove("id");
        full.remove("x");
        full.remove("y");
        full.remove("z");

        CompoundTag tag = new CompoundTag();
        tag.putIntArray("Pos", new int[]{pos.getX() - bounds.minX(), pos.getY() - bounds.minY(), pos.getZ() - bounds.minZ()});
        tag.putString("Id", id);
        tag.put("Data", full);
        return tag;
    }

    private static ListTag captureEntities(ClientLevel level, Bounds bounds) {
        ListTag entities = new ListTag();
        AABB box = new AABB(
            bounds.minX(), bounds.minY(), bounds.minZ(),
            bounds.maxX() + 1, bounds.maxY() + 1, bounds.maxZ() + 1
        );
        for (Entity entity : level.getEntities((Entity) null, box, SchematicExporter::isExportableEntity)) {
            if (!box.contains(entity.getX(), entity.getY(), entity.getZ())) {
                continue;
            }
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            if (!entity.save(output)) {
                continue;
            }

            CompoundTag data = output.buildResult();
            String id = data.getStringOr("id", "");
            data.remove("id");

            ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(entity.getX() - bounds.minX()));
            pos.add(DoubleTag.valueOf(entity.getY() - bounds.minY()));
            pos.add(DoubleTag.valueOf(entity.getZ() - bounds.minZ()));

            CompoundTag tag = new CompoundTag();
            tag.put("Pos", pos);
            tag.putString("Id", id);
            tag.put("Data", data);
            entities.add(tag);
        }
        return entities;
    }

    private static boolean isExportableEntity(Entity entity) {
        return entity instanceof Display || entity instanceof ArmorStand;
    }

    private static LevelChunk loadedChunk(ClientLevel level, int chunkX, int chunkZ) {
        if (!level.hasChunk(chunkX, chunkZ)) {
            return null;
        }
        return level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
    }

    private static int index(Bounds bounds, int x, int y, int z) {
        return ((y - bounds.minY()) * bounds.length() + (z - bounds.minZ())) * bounds.width() + (x - bounds.minX());
    }

    private static int paletteIndex(Map<String, Integer> palette, String state) {
        Integer existing = palette.get(state);
        if (existing != null) {
            return existing;
        }
        int next = palette.size();
        palette.put(state, next);
        return next;
    }

    private static void writeVarInt(ByteArrayOutputStream out, int value) {
        while ((value & -128) != 0) {
            out.write(value & 127 | 128);
            value >>>= 7;
        }
        out.write(value);
    }
}
