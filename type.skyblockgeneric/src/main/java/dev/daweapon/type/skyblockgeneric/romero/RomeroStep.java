package dev.daweapon.type.skyblockgeneric.romero;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import lombok.Getter;
import net.minestom.server.coordinate.Pos;

@Getter
public enum RomeroStep {
    ROCK(1, ServerType.SKYBLOCK_THE_PARK, -456.5, 100, 30.5, 65, "POORLY_WRAPPED_ROCK"),
    BOUQUET(2, ServerType.SKYBLOCK_HUB, 89.5, 75, 15.5, 90, "ROSE_BOUQUET"),
    EMERALD_ALTAR(3, ServerType.SKYBLOCK_HUB, -119, 74, -171, 0, "FAKE_EMERALD_ALTAR"),
    STEW(4, ServerType.SKYBLOCK_CRIMSON_ISLE, -388.5, 95, -478.5, 0, "WARTS_STEW"),
    MOON(5, ServerType.SKYBLOCK_HUB, -54.5, 169, 73.5, 0, "MOON_CHUNK"),
    GOLD(6, ServerType.SKYBLOCK_GOLD_MINE, -21.5, 36, -338.5, 0, "WRAPPED_GIFT_FOR_JULIETTE"),
    PRISM(7, ServerType.SKYBLOCK_HUB, 146.5, 112, 113.5, 0, "SOLVED_PRISM"),
    POEM(8, ServerType.SKYBLOCK_HUB, 130.5, 68, 36.5, 0, "POEM_OF_INFINITE_LOVE"),
    FLOWERS(9, ServerType.SKYBLOCK_THE_FARMING_ISLANDS, 297.5, 87, -562.5, 0, "FLOWER_MAELSTROM"),
    WEDDING(10, ServerType.SKYBLOCK_THE_PARK, -417.5, 130, -121.5, 0, "VERY_OFFICIAL_YELLOW_ROCK");

    public static final int FIRST = 1;
    public static final int LAST = 10;
    public static final int COMPLETED = 11;

    private final int number;
    private final ServerType server;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final String questItemId;

    RomeroStep(int number, ServerType server, double x, double y, double z, float yaw, String questItemId) {
        this.number = number;
        this.server = server;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.questItemId = questItemId;
    }

    public Pos position() {
        return new Pos(x, y, z, yaw, 0);
    }

    public ItemType questItem() {
        return ItemType.get(questItemId);
    }

    public static RomeroStep of(int number) {
        for (RomeroStep step : values()) {
            if (step.number == number) return step;
        }
        return null;
    }
}
