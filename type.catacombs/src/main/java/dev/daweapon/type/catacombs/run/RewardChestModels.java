package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hypixel's in-world reward chests, copied part by part from both captured Floor I runs (identical): each chest is a
 * stack of invisible armor stands wearing a block or a textured head, standing on a sea lantern in a + around Bonzo's
 * spawn (Wood west, Gold east, Diamond north, Emerald south, four blocks out) and turned to face it. Offsets are from
 * Bonzo's spawn (-42.5, 71, 20.5). Obsidian and Bedrock were never captured.
 */
public final class RewardChestModels {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";
    static final String WOODEN_PLANK_CHEST = TEXTURE + "NWE3MzZlYjFhN2NlNWE1ZjVkNWIxNjg5MzFmYTMxMzk2Mzg2YzE2MDU2OGI0MTk2NGJhODZjZGI5ZWQ2YmUifX19";
    static final String SKULL = TEXTURE + "MWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19";
    static final String GOLD_CHEST = TEXTURE + "ZjM3Y2FlNWM1MWViMTU1OGVhODI4ZjU4ZTBkZmY4ZTZiN2IwYjFhMTgzZDczN2VlY2Y3MTQ2NjE3NjEifX19";
    static final String GOLDEN_SKULL = TEXTURE + "NGFkMDQ3NmU4NjcxNjk2YWYzYTg5NDlhZmEyYTgxNGI5YmRkZTY1ZWNjZDFhOGI1OTNhZWVmZjVhMDMxOGQifX19";
    static final String DIAMOND_SKULL = TEXTURE + "NzdiOWRmZDI4MWRlYWVmMjYyOGFkNTg0MGQ0NWJjZGE0MzZkNjYyNjg0NzU4N2YzYWM3NjQ5OGE1MWM4NjEifX19";
    static final String CHEST_DIAMOND_LEFT = TEXTURE + "MmJiN2IzODVlMGFhNzQ4YTdjNmVmZGYzYmNmMjA5OTI4ZTgyNDcxY2MzNmU5NTQzMzc3NjdjNWUzZDJlMTUwZSJ9fX0K";
    static final String CHEST_DIAMOND_RIGHT = TEXTURE + "NzI2ZWMzZDg0NDM4MGEyN2Q1ODU2OWJkYTU4YjRmYTIxYTQ2ZDU2MDI4MjUyNzM0MWNmMWUwYTIyODI2MWFhMyJ9fX0K";
    static final String EMERALD_SKULL = TEXTURE + "NzNlY2RiODQ3ZTEwMTg0OWYzMzU0NzJhYzAyZGIwZDg4OTk2YTY5MThlZWU5NTc5NmRjZjg2Y2I0N2YyMTdlIn19fQ==";
    static final String CHEST_EMERALD_LEFT = TEXTURE + "MTgyNGQ0NTRlMDUwOGM1N2UzZmU4MzM3MDMwYTBiYzUyZTIwNjVlMTlmMDJlMzBiNDU3ZDM1MmQ0YTMxMjUzNyJ9fX0K";
    static final String CHEST_EMERALD_RIGHT = TEXTURE + "ZTBlZDk0YmY1ZmNjZGQ5ZDNiN2NhN2ZhMzhlMmNlYjUwMTMzMmU2YjMzNTM0MDQyNzY1NjZiNTIxNmMxMzA0ZiJ9fX0K";

    /** One armor stand: where it stands, whether it is small, its head tilt, and the block or head it wears. */
    public record Part(double dx, double dy, double dz, boolean small, float headPitch, Material material,
                       @Nullable String name, @Nullable String texture) {
    }

    /** A whole chest: its sea lantern (block offset from the centre block, one below its feet), its facing and parts. */
    public record Model(int lanternX, int lanternZ, float yaw, List<Part> parts) {
    }

    private static final float SKULL_TILT = -16;

    static final Map<DungeonChestTier, Model> MODELS = Map.of(
            DungeonChestTier.WOOD, new Model(-4, 0, -90, List.of(
                    block(-4.0625, -1.40625, -0.0625, false, Material.SPRUCE_SLAB),
                    block(-4.0625, -0.46875, 0, true, Material.OAK_LOG),
                    block(-4.03125, -0.875, -0.03125, false, Material.SPRUCE_SLAB),
                    head(-4, 0.3125, 0, true, 0, "<9>Wooden Plank Chest", WOODEN_PLANK_CHEST),
                    head(-3.84375, -0.71875, -0.03125, true, SKULL_TILT, "<9>Skull", SKULL))),
            DungeonChestTier.GOLD, new Model(4, 0, 90, List.of(
                    block(4.03125, -0.46875, -0.0625, true, Material.GOLD_BLOCK),
                    block(4.03125, -1.40625, -0.0625, false, Material.SMOOTH_STONE_SLAB),
                    head(4, -0.40625, -0.03125, false, 0, null, GOLD_CHEST),
                    block(4.0625, -0.875, -0.0625, false, Material.SMOOTH_STONE_SLAB),
                    head(3.84375, -0.6875, -0.0625, true, SKULL_TILT, "<9>Golden Skull", GOLDEN_SKULL))),
            DungeonChestTier.DIAMOND, new Model(0, -4, 0, List.of(
                    block(-0.4375, -1.40625, -4, false, Material.SMOOTH_STONE_SLAB),
                    block(0.1875, -1.40625, -4, false, Material.SMOOTH_STONE_SLAB),
                    block(-0.375, -0.46875, -4, true, Material.DIAMOND_BLOCK),
                    block(0.09375, -0.46875, -4, true, Material.DIAMOND_BLOCK),
                    head(-0.125, -0.71875, -3.8125, true, SKULL_TILT, "<9>Diamond Skull", DIAMOND_SKULL),
                    block(0.15625, -0.875, -4, false, Material.SMOOTH_STONE_SLAB),
                    block(-0.46875, -0.875, -4, false, Material.SMOOTH_STONE_SLAB),
                    head(-0.40625, -0.375, -4, false, 0, "<9>Chest Diamond Left", CHEST_DIAMOND_LEFT),
                    head(0.125, -0.375, -4, false, 0, "<9>Chest Diamond Right", CHEST_DIAMOND_RIGHT))),
            DungeonChestTier.EMERALD, new Model(0, 4, 180, List.of(
                    block(-0.34375, -1.40625, 4.09375, false, Material.SMOOTH_STONE_SLAB),
                    head(0, -0.71875, 3.875, true, SKULL_TILT, "<9>Emerald Skull", EMERALD_SKULL),
                    block(0.3125, -1.40625, 4.09375, false, Material.SMOOTH_STONE_SLAB),
                    block(-0.34375, -0.875, 4.09375, false, Material.SMOOTH_STONE_SLAB),
                    head(0.28125, -0.375, 4.0625, false, 0, "<9>Chest Emerald Left", CHEST_EMERALD_LEFT),
                    block(0.1875, -0.46875, 4.09375, true, Material.EMERALD_BLOCK),
                    block(-0.21875, -0.46875, 4.09375, true, Material.EMERALD_BLOCK),
                    block(0.28125, -0.875, 4.0625, false, Material.SMOOTH_STONE_SLAB),
                    head(-0.3125, -0.375, 4.0625, false, 0, "<9>Chest Emerald Right", CHEST_EMERALD_RIGHT))));

    /**
     * Where the uncaptured chests go: the diagonals of the +, estimated, with our pack's model on a sea lantern since
     * no Hypixel model for them was captured.
     */
    static final Map<DungeonChestTier, int[]> UNCAPTURED_LANTERNS = Map.of(
            DungeonChestTier.OBSIDIAN, new int[]{-4, -4},
            DungeonChestTier.BEDROCK, new int[]{4, -4});

    private RewardChestModels() {
    }

    public static Optional<Model> model(DungeonChestTier tier) {
        return Optional.ofNullable(MODELS.get(tier));
    }

    private static Part block(double dx, double dy, double dz, boolean small, Material material) {
        return new Part(dx, dy, dz, small, 0, material, null, null);
    }

    private static Part head(double dx, double dy, double dz, boolean small, float pitch, @Nullable String name, String texture) {
        return new Part(dx, dy, dz, small, pitch, Material.PLAYER_HEAD, name, texture);
    }
}
