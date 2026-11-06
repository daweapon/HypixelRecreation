package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The doorways that start closed, filled through their whole 3 deep passage as in the captured runs: the Entrance
 * room's exit behind infested chiseled stone bricks until the run starts, Wither doors in coal and the Blood door in red
 * terracotta until a key opens them.
 */
public final class DoorGates {
    public static final Block ENTRANCE = Block.INFESTED_CHISELED_STONE_BRICKS;
    public static final Block WITHER = Block.COAL_BLOCK;
    public static final Block BLOOD = Block.RED_TERRACOTTA;

    private final Map<LayoutDoor, Block> closed;
    private final Map<Long, Block> blocks = new HashMap<>();

    private DoorGates(Map<LayoutDoor, Block> closed) {
        this.closed = closed;
        closed.forEach((door, block) -> {
            for (int[] at : DoorFrame.passage(door).blocks()) blocks.put(CatacombsArenaBlocks.pack(at[0], at[1], at[2]), block);
        });
    }

    public static DoorGates of(FloorLayout layout) {
        int start = layout.startRoom().index();
        Map<LayoutDoor, Block> closed = new LinkedHashMap<>();
        for (LayoutDoor door : layout.doors()) {
            appearance(door, start).ifPresent(block -> closed.put(door, block));
        }
        return new DoorGates(closed);
    }

    private static Optional<Block> appearance(LayoutDoor door, int startRoom) {
        return switch (door.type()) {
            case WITHER -> Optional.of(WITHER);
            case BLOOD -> Optional.of(BLOOD);
            case PLAIN -> door.connects(startRoom) ? Optional.of(ENTRANCE) : Optional.empty();
        };
    }

    public List<LayoutDoor> entranceDoors() {
        return closed.entrySet().stream().filter(entry -> entry.getValue() == ENTRANCE).map(Map.Entry::getKey).toList();
    }

    public Optional<Block> appearanceOf(LayoutDoor door) {
        return Optional.ofNullable(closed.get(door));
    }

    /** The block a closed gate puts at this position, or null where no gate stands. */
    public Block blockAt(int x, int y, int z) {
        return blocks.get(CatacombsArenaBlocks.pack(x, y, z));
    }
}
