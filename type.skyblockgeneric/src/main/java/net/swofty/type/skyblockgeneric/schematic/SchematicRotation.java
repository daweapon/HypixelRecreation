package net.swofty.type.skyblockgeneric.schematic;

import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SchematicRotation {
    private static final List<String> SIDES = List.of("north", "east", "south", "west");

    private SchematicRotation() {
    }

    public static int quarterTurns(int degrees) {
        if (degrees % 90 != 0) throw new IllegalArgumentException("Rotation must be a multiple of 90, got " + degrees);
        return Math.floorMod(degrees / 90, 4);
    }

    public static int rotatedX(int x, int z, int width, int length, int turns) {
        return switch (turns) {
            case 1 -> length - 1 - z;
            case 2 -> width - 1 - x;
            case 3 -> z;
            default -> x;
        };
    }

    public static int rotatedZ(int x, int z, int width, int length, int turns) {
        return switch (turns) {
            case 1 -> x;
            case 2 -> length - 1 - z;
            case 3 -> width - 1 - x;
            default -> z;
        };
    }

    static String turn(String side, int turns) {
        int index = SIDES.indexOf(side);
        return index < 0 ? side : SIDES.get((index + turns) % 4);
    }

    public static Block rotate(Block block, int turns) {
        if (turns == 0) return block;
        Map<String, String> properties = block.properties();
        if (properties.isEmpty()) return block;
        Map<String, String> rotated = new HashMap<>(properties);
        String facing = properties.get("facing");
        if (facing != null) rotated.put("facing", turn(facing, turns));
        String axis = properties.get("axis");
        if (axis != null && turns % 2 == 1) rotated.put("axis", axis.equals("x") ? "z" : axis.equals("z") ? "x" : axis);
        String rotation = properties.get("rotation");
        if (rotation != null) {
            try {
                rotated.put("rotation", String.valueOf(Math.floorMod(Integer.parseInt(rotation) + turns * 4, 16)));
            } catch (NumberFormatException ignored) {
            }
        }
        for (String side : SIDES) rotated.remove(side);
        for (String side : SIDES) {
            String value = properties.get(side);
            if (value != null) rotated.put(turn(side, turns), value);
        }
        try {
            return block.withProperties(rotated);
        } catch (IllegalArgumentException e) {
            return block;
        }
    }
}
