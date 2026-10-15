package dev.daweapon.dungeons.catacombs.layout;

public class RoomTemplateException extends IllegalArgumentException {
    public RoomTemplateException(String message) {
        super(message);
    }

    public static RoomTemplateException invalidField(String templateLabel, String field, String problem) {
        return new RoomTemplateException("Room template " + templateLabel + " field '" + field + "': " + problem);
    }
}
