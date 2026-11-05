package dev.daweapon.dungeons.catacombs.layout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

final class RoomTemplateParser {
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_]+");

    private RoomTemplateParser() {
    }

    static List<RoomTemplate> parse(String source, String json) {
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (JsonParseException exception) {
            throw new RoomTemplateException("Room template file " + source + " is not valid JSON: " + exception.getMessage());
        }
        if (!root.isJsonObject() || !root.getAsJsonObject().has("templates") || !root.getAsJsonObject().get("templates").isJsonArray()) {
            throw new RoomTemplateException("Room template file " + source + " must be an object with a 'templates' array");
        }
        JsonArray entries = root.getAsJsonObject().getAsJsonArray("templates");
        List<RoomTemplate> templates = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            templates.add(parseTemplate(entries.get(index), "#" + index + " in " + source));
        }
        return templates;
    }

    private static RoomTemplate parseTemplate(JsonElement element, String position) {
        if (!element.isJsonObject()) {
            throw new RoomTemplateException("Room template " + position + " must be a JSON object");
        }
        JsonObject object = element.getAsJsonObject();
        String id = requiredString(object, "id", position);
        if (!ID_PATTERN.matcher(id).matches()) {
            throw RoomTemplateException.invalidField(position, "id", "'" + id + "' must match [a-z0-9_]+");
        }
        String label = "'" + id + "'";
        String name = requiredString(object, "name", label);
        RoomShape shape = requiredEnum(RoomShape.class, object, "shape", label);
        RoomKind kind = requiredEnum(RoomKind.class, object, "kind", label);
        if (kind.requiresSingleCell() && shape != RoomShape.ONE_BY_ONE) {
            throw RoomTemplateException.invalidField(label, "shape", "kind " + kind + " must use ONE_BY_ONE, not " + shape);
        }
        int secretCount = requiredInt(object, "secretCount", label, 0);
        boolean placeholder = object.has("placeholder") && requiredBoolean(object, "placeholder", label);
        List<String> tags = parseTags(object, label);
        List<DoorSocket> sockets = parseSockets(object, shape, label);
        List<MobSpawnPoint> spawns = parseSpawns(object, shape, label);
        List<SecretAnchor> anchors = parseAnchors(object, shape, label);
        if (anchors.size() > secretCount) {
            throw RoomTemplateException.invalidField(label, "secretAnchors", anchors.size() + " anchors exceed secretCount " + secretCount);
        }
        RoomStructure structure = parseStructure(object, label);
        StandPoint stand = parseStand(object, shape, label);
        List<CryptSpot> crypts = parseCrypts(object, shape, label);
        Rotation fixedRotation = object.has("fixedRotation") ? requiredEnum(Rotation.class, object, "fixedRotation", label) : null;
        if (fixedRotation != null && shape != RoomShape.ONE_BY_ONE) {
            throw RoomTemplateException.invalidField(label, "fixedRotation", "only a ONE_BY_ONE room can lock its rotation");
        }
        return new RoomTemplate(id, name, shape, kind, secretCount, placeholder, tags, sockets, spawns, anchors,
                structure, stand, crypts, fixedRotation);
    }

    private static RoomStructure parseStructure(JsonObject object, String label) {
        if (!object.has("structure")) {
            return null;
        }
        JsonObject structure = requiredObject(object.get("structure"), "structure", label);
        String structureLabel = label + " structure";
        String file = requiredString(structure, "file", structureLabel);
        if (file.contains("..") || file.startsWith("/") || file.contains("\\")) {
            throw RoomTemplateException.invalidField(label, "structure", "file '" + file + "' must be a relative path inside the rooms folder");
        }
        return new RoomStructure(file,
                requiredInt(structure, "x", structureLabel, Integer.MIN_VALUE),
                requiredInt(structure, "y", structureLabel, Integer.MIN_VALUE),
                requiredInt(structure, "z", structureLabel, Integer.MIN_VALUE));
    }

    private static StandPoint parseStand(JsonObject object, RoomShape shape, String label) {
        if (!object.has("standPoint")) {
            return null;
        }
        JsonObject point = requiredObject(object.get("standPoint"), "standPoint", label);
        String pointLabel = label + " standPoint";
        StandPoint stand = new StandPoint(requiredNumber(point, "x", pointLabel), requiredNumber(point, "y", pointLabel),
                requiredNumber(point, "z", pointLabel));
        if (!insideShape(shape, stand.x(), stand.z())) {
            throw RoomTemplateException.invalidField(label, "standPoint", "is outside the room footprint");
        }
        return stand;
    }

    private static List<CryptSpot> parseCrypts(JsonObject object, RoomShape shape, String label) {
        List<CryptSpot> crypts = new ArrayList<>();
        for (JsonElement element : optionalArray(object, "crypts", label)) {
            JsonObject cryptObject = requiredObject(element, "crypts", label);
            String cryptLabel = label + " crypts entry";
            CryptSpot crypt = new CryptSpot(
                    requiredInt(cryptObject, "x", cryptLabel, Integer.MIN_VALUE),
                    requiredInt(cryptObject, "y", cryptLabel, Integer.MIN_VALUE),
                    requiredInt(cryptObject, "z", cryptLabel, Integer.MIN_VALUE));
            if (!insideShape(shape, crypt.x(), crypt.z())) {
                throw RoomTemplateException.invalidField(label, "crypts", "crypt at " + crypt.x() + "," + crypt.z() + " is outside shape " + shape);
            }
            crypts.add(crypt);
        }
        return crypts;
    }

    private static List<String> parseTags(JsonObject object, String label) {
        List<String> tags = new ArrayList<>();
        for (JsonElement tag : optionalArray(object, "tags", label)) {
            if (!tag.isJsonPrimitive() || !tag.getAsJsonPrimitive().isString()) {
                throw RoomTemplateException.invalidField(label, "tags", "every tag must be a string");
            }
            tags.add(tag.getAsString());
        }
        return tags;
    }

    private static List<DoorSocket> parseSockets(JsonObject object, RoomShape shape, String label) {
        List<DoorSocket> sockets = new ArrayList<>();
        Set<DoorSocket> seen = new HashSet<>();
        for (JsonElement element : optionalArray(object, "doorSockets", label)) {
            JsonObject socketObject = requiredObject(element, "doorSockets", label);
            Cell cell = new Cell(
                    requiredInt(socketObject, "col", label + " doorSockets entry", 0),
                    requiredInt(socketObject, "row", label + " doorSockets entry", 0));
            Side side = requiredEnum(Side.class, socketObject, "side", label + " doorSockets entry");
            if (!shape.containsBaseCell(cell)) {
                throw RoomTemplateException.invalidField(label, "doorSockets", "cell " + cell.col() + "," + cell.row() + " is outside shape " + shape);
            }
            DoorSocket socket = new DoorSocket(cell, side);
            if (!seen.add(socket)) {
                throw RoomTemplateException.invalidField(label, "doorSockets", "duplicate socket " + cell.col() + "," + cell.row() + " " + side);
            }
            sockets.add(socket);
        }
        return sockets;
    }

    private static List<MobSpawnPoint> parseSpawns(JsonObject object, RoomShape shape, String label) {
        List<MobSpawnPoint> spawns = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (JsonElement element : optionalArray(object, "mobSpawns", label)) {
            JsonObject spawnObject = requiredObject(element, "mobSpawns", label);
            String spawnLabel = label + " mobSpawns entry";
            String id = requiredString(spawnObject, "id", spawnLabel);
            double x = requiredNumber(spawnObject, "x", spawnLabel);
            double y = requiredNumber(spawnObject, "y", spawnLabel);
            double z = requiredNumber(spawnObject, "z", spawnLabel);
            String mobId = requiredString(spawnObject, "mobId", spawnLabel);
            boolean starred = requiredBoolean(spawnObject, "starred", spawnLabel);
            if (!ids.add(id)) {
                throw RoomTemplateException.invalidField(label, "mobSpawns", "duplicate spawn id '" + id + "'");
            }
            if (!insideShape(shape, x, z)) {
                throw RoomTemplateException.invalidField(label, "mobSpawns", "spawn '" + id + "' is outside the room footprint");
            }
            spawns.add(new MobSpawnPoint(id, x, y, z, mobId, starred));
        }
        return spawns;
    }

    private static boolean insideShape(RoomShape shape, double x, double z) {
        if (x < 0 || z < 0) {
            return false;
        }
        return shape.containsBaseCell(new Cell((int) (x / CellGeometry.CELL_PITCH), (int) (z / CellGeometry.CELL_PITCH)));
    }

    private static List<SecretAnchor> parseAnchors(JsonObject object, RoomShape shape, String label) {
        List<SecretAnchor> anchors = new ArrayList<>();
        for (JsonElement element : optionalArray(object, "secretAnchors", label)) {
            JsonObject anchorObject = requiredObject(element, "secretAnchors", label);
            String anchorLabel = label + " secretAnchors entry";
            SecretAnchor anchor = new SecretAnchor(
                    requiredEnum(SecretType.class, anchorObject, "type", anchorLabel),
                    requiredInt(anchorObject, "x", anchorLabel, Integer.MIN_VALUE),
                    requiredInt(anchorObject, "y", anchorLabel, Integer.MIN_VALUE),
                    requiredInt(anchorObject, "z", anchorLabel, Integer.MIN_VALUE));
            if (!insideShape(shape, anchor.x(), anchor.z())) {
                throw RoomTemplateException.invalidField(label, "secretAnchors", "anchor at " + anchor.x() + "," + anchor.z() + " is outside shape " + shape);
            }
            anchors.add(anchor);
        }
        return anchors;
    }

    private static JsonArray optionalArray(JsonObject object, String field, String label) {
        if (!object.has(field)) {
            return new JsonArray();
        }
        if (!object.get(field).isJsonArray()) {
            throw RoomTemplateException.invalidField(label, field, "must be an array");
        }
        return object.getAsJsonArray(field);
    }

    private static JsonObject requiredObject(JsonElement element, String field, String label) {
        if (!element.isJsonObject()) {
            throw RoomTemplateException.invalidField(label, field, "every entry must be an object");
        }
        return element.getAsJsonObject();
    }

    private static String requiredString(JsonObject object, String field, String label) {
        JsonElement value = object.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString() || value.getAsString().isBlank()) {
            throw RoomTemplateException.invalidField(label, field, "is required and must be a non-blank string");
        }
        return value.getAsString();
    }

    private static boolean requiredBoolean(JsonObject object, String field, String label) {
        JsonElement value = object.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw RoomTemplateException.invalidField(label, field, "is required and must be true or false");
        }
        return value.getAsBoolean();
    }

    private static double requiredNumber(JsonObject object, String field, String label) {
        JsonElement value = object.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw RoomTemplateException.invalidField(label, field, "is required and must be a number");
        }
        return value.getAsDouble();
    }

    private static int requiredInt(JsonObject object, String field, String label, int minimum) {
        JsonElement value = object.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw RoomTemplateException.invalidField(label, field, "is required and must be a whole number");
        }
        BigDecimal number = value.getAsBigDecimal();
        if (number.stripTrailingZeros().scale() > 0) {
            throw RoomTemplateException.invalidField(label, field, "must be a whole number, got " + number);
        }
        if (number.compareTo(BigDecimal.valueOf(minimum)) < 0 || number.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
            throw RoomTemplateException.invalidField(label, field, "must be at least " + minimum + ", got " + number);
        }
        return number.intValueExact();
    }

    private static <E extends Enum<E>> E requiredEnum(Class<E> type, JsonObject object, String field, String label) {
        String raw = requiredString(object, field, label);
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equals(raw)) {
                return constant;
            }
        }
        throw RoomTemplateException.invalidField(label, field, "unknown value '" + raw + "', expected one of " + Arrays.toString(type.getEnumConstants()));
    }
}
