import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class BlockStates {
    private static final List<String> SIDES = List.of("north", "east", "south", "west");

    private BlockStates() {
    }

    static String name(String state) {
        return AnvilWorld.bare(state);
    }

    static Map<String, String> properties(String state) {
        Map<String, String> properties = new TreeMap<>();
        int open = state.indexOf('[');
        if (open < 0) return properties;
        for (String pair : state.substring(open + 1, state.length() - 1).split(",")) {
            int equals = pair.indexOf('=');
            properties.put(pair.substring(0, equals), pair.substring(equals + 1));
        }
        return properties;
    }

    static String of(String name, Map<String, String> properties) {
        if (properties.isEmpty()) return name;
        StringBuilder out = new StringBuilder(name).append('[');
        boolean first = true;
        for (Map.Entry<String, String> property : new TreeMap<>(properties).entrySet()) {
            if (!first) out.append(',');
            out.append(property.getKey()).append('=').append(property.getValue());
            first = false;
        }
        return out.append(']').toString();
    }

    static String with(String state, String key, String value) {
        Map<String, String> properties = properties(state);
        properties.put(key, value);
        return of(name(state), properties);
    }

    static String rotate(String state, int turns) {
        if (turns % 4 == 0) return state;
        Map<String, String> properties = properties(state);
        if (properties.isEmpty()) return state;
        Map<String, String> rotated = new TreeMap<>(properties);
        String facing = properties.get("facing");
        if (facing != null) rotated.put("facing", turn(facing, turns));
        String axis = properties.get("axis");
        if (axis != null && turns % 2 == 1) rotated.put("axis", axis.equals("x") ? "z" : axis.equals("z") ? "x" : axis);
        String rotation = properties.get("rotation");
        if (rotation != null) rotated.put("rotation", String.valueOf(Math.floorMod(Integer.parseInt(rotation) + turns * 4, 16)));
        for (String side : SIDES) rotated.remove(side);
        for (String side : SIDES) {
            String value = properties.get(side);
            if (value != null) rotated.put(turn(side, turns), value);
        }
        return of(name(state), rotated);
    }

    private static String turn(String side, int turns) {
        int index = SIDES.indexOf(side);
        return index < 0 ? side : SIDES.get((index + turns) % 4);
    }

    static boolean passable(String state) {
        if (AnvilWorld.isAir(state)) return true;
        String name = name(state).substring("minecraft:".length());
        if (name.endsWith("_block") || name.equals("grass_block")) return false;
        return name.contains("torch") || name.contains("button") || name.equals("short_grass") || name.equals("tall_grass")
                || name.contains("fern") || name.contains("flower") || name.contains("vine") || name.equals("ladder")
                || name.contains("rail") || name.startsWith("tripwire") || name.contains("sign") || name.contains("banner")
                || name.contains("carpet") || name.contains("pressure_plate") || name.equals("dead_bush")
                || name.endsWith("mushroom") || name.equals("lever") || name.equals("redstone_wire") || name.equals("fire")
                || name.equals("snow") || name.equals("cobweb") || name.contains("sapling") || name.equals("water")
                || name.contains("skull") || name.contains("head");
    }

    static boolean supports(String state) {
        if (passable(state)) return false;
        String name = name(state);
        return !name.equals("minecraft:lava") && !name.equals("minecraft:barrier");
    }

    static boolean climbable(String state) {
        String name = name(state);
        return name.equals("minecraft:ladder") || name.equals("minecraft:vine") || name.equals("minecraft:scaffolding");
    }
}
