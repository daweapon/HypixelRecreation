package dev.daweapon.type.skyblockgeneric.century;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import org.jetbrains.annotations.Nullable;

public enum CenturyCake {
    PINK("Crab-Colored Century Cake", ItemStatistic.HEALTH, 10, "<c>Health", CakeSoulColour.PINK),
    GREEN("Pet Rock Century Cake", ItemStatistic.DEFENSE, 3, "<a>Defense", CakeSoulColour.LIME),
    RED("Fruit Century Cake", ItemStatistic.STRENGTH, 2, "<c>Strength", CakeSoulColour.RED),
    YELLOW("Potato-Style Century Cake", ItemStatistic.SPEED, 10, "<f>Speed", CakeSoulColour.YELLOW),
    AQUA("Barry Century Cake", ItemStatistic.INTELLIGENCE, 5, "<b>Intelligence", CakeSoulColour.AQUA),
    BLUE("Sea Emperor Century Cake", ItemStatistic.SEA_CREATURE_CHANCE, 1, "<3>Sea Creature Chance", CakeSoulColour.BLUE),
    BLACK("Century Cake of the Next Dungeon Floor", ItemStatistic.MAGIC_FIND, 1, "<b>Magic Find", CakeSoulColour.BLACK),
    ORANGE("Latest Update Century Cake", ItemStatistic.FEROCITY, 2, "<c>Ferocity", CakeSoulColour.ORANGE),
    PURPLE("Streamer's Century Cake", ItemStatistic.PET_LUCK, 1, "<d>Pet Luck", CakeSoulColour.PURPLE),
    BROWN("Chocolate Century Cake", ItemStatistic.FARMING_FORTUNE, 5, "<6>Farming Fortune", CakeSoulColour.BROWN),
    WHITE("Cloudy Century Cake", ItemStatistic.FORAGING_FORTUNE, 5, "<6>Foraging Fortune", CakeSoulColour.WHITE),
    CYAN("Century Cake of Hype", ItemStatistic.MINING_FORTUNE, 5, "<6>Mining Fortune", CakeSoulColour.CYAN),
    DARK_GREEN("Undead Century Cake", ItemStatistic.VITALITY, 1, "<4>Vitality", CakeSoulColour.GREEN),
    GRAY("Not-a-lie Century Cake", ItemStatistic.TRUE_DEFENSE, 1, "<f>True Defense", CakeSoulColour.GRAY),
    MAGENTA("ekaC yrutneC tfiR", null, 10, "<a>Rift Time", CakeSoulColour.MAGENTA),
    SILVER("Corpse-Flavored Century Cake", ItemStatistic.COLD_RESISTANCE, 1, "<b>Cold Resistance", CakeSoulColour.LIGHT_GRAY),
    SEVEN_SEAS("Century Cake of the Seven Seas", ItemStatistic.TREASURE_CHANCE, 1, "<6>Treasure Chance", CakeSoulColour.AQUA),
    STARBORN("Starborn Century Cake", ItemStatistic.HUNTER_FORTUNE, 1, "<d>Hunting Fortune", CakeSoulColour.PINK),
    EXPIRED("Long-Expired Century Cake", ItemStatistic.SWEEP, 5, "<2>Sweep", CakeSoulColour.GREEN),
    HEPHAESTUS("Hephaestus' Century Cake", ItemStatistic.TRACKING, 1, "<d>Tracking", CakeSoulColour.MAGENTA);

    public static final String ID_PREFIX = "EPOCH_CAKE_";

    private final String displayName;
    private final @Nullable ItemStatistic statistic;
    private final int amount;
    private final String statLabel;
    private final CakeSoulColour soulColour;

    CenturyCake(String displayName, @Nullable ItemStatistic statistic, int amount, String statLabel, CakeSoulColour soulColour) {
        this.displayName = displayName;
        this.statistic = statistic;
        this.amount = amount;
        this.statLabel = statLabel;
        this.soulColour = soulColour;
    }

    public String itemId() {
        return ID_PREFIX + name();
    }

    public @Nullable ItemType itemType() {
        return ItemType.get(itemId());
    }

    public String displayName() {
        return displayName;
    }

    public @Nullable ItemStatistic statistic() {
        return statistic;
    }

    public int amount() {
        return amount;
    }

    public String bonus() {
        return statLabel.substring(0, 3) + "+" + amount + " " + statLabel;
    }

    public CakeSoulColour soulColour() {
        return soulColour;
    }

    public static @Nullable CenturyCake fromItemId(String itemId) {
        if (!itemId.startsWith(ID_PREFIX)) return null;
        try {
            return valueOf(itemId.substring(ID_PREFIX.length()));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
