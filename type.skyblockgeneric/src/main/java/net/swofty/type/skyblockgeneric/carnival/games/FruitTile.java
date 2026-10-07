package net.swofty.type.skyblockgeneric.carnival.games;

import lombok.Getter;

/** What lies under one sand block in Fruit Digging, with the wiki's points and counts per board. */
@Getter
public enum FruitTile {
    DRAGONFRUIT("Dragonfruit", "<d>", 1200, 1),
    MANGO("Mango", "<6>", 300, 10),
    COCONUT("Coconut", "<6>", 200, 3),
    APPLE("Apple", "<c>", 0, 8),
    CHERRY("Cherry", "<c>", 200, 2),
    POMEGRANATE("Pomegranate", "<5>", 200, 4),
    DURIAN("Durian", "<e>", 800, 2),
    WATERMELON("Watermelon", "<a>", 100, 4),
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
}
