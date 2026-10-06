package net.swofty.type.skyblockgeneric.elections.jerrypocalypse;

import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;

import java.util.concurrent.ThreadLocalRandom;

/** The four hidden Jerries Jerrypocalypse reveals, with the wiki's spawn weights and health. */
@Getter
public enum HiddenJerryType {
    GREEN("Green", "<a>", 1, 12, 3, ItemType.JERRY_BOX_GREEN),
    BLUE("Blue", "<9>", 2, 6, 4, ItemType.JERRY_BOX_BLUE),
    PURPLE("Purple", "<5>", 3, 3, 5, ItemType.JERRY_BOX_PURPLE),
    GOLDEN("Golden", "<6>", 5, 1, 6, ItemType.JERRY_BOX_GOLDEN);

    private final String name;
    private final String color;
    private final int level;
    private final int weight;
    private final int health;
    private final ItemType box;

    HiddenJerryType(String name, String color, int level, int weight, int health, ItemType box) {
        this.name = name;
        this.color = color;
        this.level = level;
        this.weight = weight;
        this.health = health;
        this.box = box;
    }

    public static HiddenJerryType roll() {
        int total = 0;
        for (HiddenJerryType type : values()) total += type.weight;
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (HiddenJerryType type : values()) {
            roll -= type.weight;
            if (roll < 0) return type;
        }
        return GREEN;
    }

    public static HiddenJerryType ofBox(ItemType box) {
        for (HiddenJerryType type : values()) {
            if (type.box == box) return type;
        }
        return null;
    }
}
