package dev.daweapon.type.skyblockgeneric.carnival.games;

import lombok.Getter;

/**
 * What lies under one sand block in Fruit Digging, with the wiki's points and counts per board. Declared in the order
 * Hypixel lists them in the end-of-game summary: fruits by rarity colour, then Bomb and Rum.
 */
@Getter
public enum FruitTile {
    APPLE("Apple", "<a>", 0, 8),
    MANGO("Mango", "<a>", 300, 10),
    POMEGRANATE("Pomegranate", "<9>", 200, 4),
    WATERMELON("Watermelon", "<9>", 100, 4),
    COCONUT("Coconut", "<9>", 200, 3),
    CHERRY("Cherry", "<5>", 200, 2),
    DURIAN("Durian", "<5>", 800, 2),
    DRAGONFRUIT("Dragonfruit", "<d>", 1200, 1),
    BOMB("Bomb", "<c>", 0, 10),
    RUM("Rum", "<e>", 0, 5);

    public static final int APPLE_BONUS = 100;
    public static final int CHERRY_BONUS = 300;

    private final String displayName;
    private final String color;
    private final int points;
    private final int perBoard;

    FruitTile(String displayName, String color, int points, int perBoard) {
        this.displayName = displayName;
        this.color = color;
        this.points = points;
        this.perBoard = perBoard;
    }

    public boolean isFruit() {
        return this != BOMB && this != RUM;
    }

    public String colored() {
        return color + displayName;
    }

    public String article() {
        return "AEIOU".indexOf(displayName.charAt(0)) >= 0 ? "an" : "a";
    }
}
