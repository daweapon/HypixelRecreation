package dev.daweapon.type.skyblockgeneric.rabbits;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum FactionRabbit {
    PEON("Peon", RabbitFaction.CITY, RabbitRarity.COMMON),
    SPEEDY("Speedy", RabbitFaction.CITY, RabbitRarity.COMMON),
    COBBLE("Cobble", RabbitFaction.CITY, RabbitRarity.COMMON),
    ALLEY("Alley", RabbitFaction.CITY, RabbitRarity.COMMON),
    SUBWAY("Subway", RabbitFaction.CITY, RabbitRarity.COMMON),
    SKYRISE("Skyrise", RabbitFaction.CITY, RabbitRarity.UNCOMMON),
    TRAFFIC("Traffic", RabbitFaction.CITY, RabbitRarity.UNCOMMON),
    TUNNEL("Tunnel", RabbitFaction.CITY, RabbitRarity.UNCOMMON),
    GRAFFITI("Graffiti", RabbitFaction.CITY, RabbitRarity.UNCOMMON),
    TAXI("Taxi", RabbitFaction.CITY, RabbitRarity.RARE),
    SHORTCUT("Shortcut", RabbitFaction.CITY, RabbitRarity.RARE),
    WALKER("Walker", RabbitFaction.CITY, RabbitRarity.RARE),
    MINISTER("Minister", RabbitFaction.CITY, RabbitRarity.EPIC),
    NEON("Neon", RabbitFaction.CITY, RabbitRarity.EPIC),
    MAYOR("Mayor", RabbitFaction.CITY, RabbitRarity.LEGENDARY),
    GRANITE("Granite", RabbitFaction.MOUNTAIN, RabbitRarity.COMMON),
    MISTRAL("Mistral", RabbitFaction.MOUNTAIN, RabbitRarity.COMMON),
    CHASM("Chasm", RabbitFaction.MOUNTAIN, RabbitRarity.COMMON),
    TIMBER("Timber", RabbitFaction.MOUNTAIN, RabbitRarity.COMMON),
    GLACIER("Glacier", RabbitFaction.MOUNTAIN, RabbitRarity.COMMON),
    CANYON("Canyon", RabbitFaction.MOUNTAIN, RabbitRarity.UNCOMMON),
    ICEFUR("Icefur", RabbitFaction.MOUNTAIN, RabbitRarity.UNCOMMON),
    VALLEY("Valley", RabbitFaction.MOUNTAIN, RabbitRarity.UNCOMMON),
    SCREE("Scree", RabbitFaction.MOUNTAIN, RabbitRarity.UNCOMMON),
    EAGLE("Eagle", RabbitFaction.MOUNTAIN, RabbitRarity.RARE),
    TUNDRA("Tundra", RabbitFaction.MOUNTAIN, RabbitRarity.RARE),
    CRAG("Crag", RabbitFaction.MOUNTAIN, RabbitRarity.RARE),
    AVALANCHE("Avalanche", RabbitFaction.MOUNTAIN, RabbitRarity.EPIC),
    SUMMIT("Summit", RabbitFaction.MOUNTAIN, RabbitRarity.EPIC),
    HORIZON("Horizon", RabbitFaction.MOUNTAIN, RabbitRarity.LEGENDARY),
    HAZEL("Hazel", RabbitFaction.COUNTRY, RabbitRarity.COMMON),
    DAISY("Daisy", RabbitFaction.COUNTRY, RabbitRarity.COMMON),
    ROOT("Root", RabbitFaction.COUNTRY, RabbitRarity.COMMON),
    TWIGY("Twigy", RabbitFaction.COUNTRY, RabbitRarity.COMMON),
    HAYBALE("Haybale", RabbitFaction.COUNTRY, RabbitRarity.COMMON),
    MOSS("Moss", RabbitFaction.COUNTRY, RabbitRarity.UNCOMMON),
    MARIGOLD("Marigold", RabbitFaction.COUNTRY, RabbitRarity.UNCOMMON),
    NECTAR("Nectar", RabbitFaction.COUNTRY, RabbitRarity.UNCOMMON),
    CLOVER("Clover", RabbitFaction.COUNTRY, RabbitRarity.UNCOMMON),
    SILO("Silo", RabbitFaction.COUNTRY, RabbitRarity.RARE),
    IRIS("Iris", RabbitFaction.COUNTRY, RabbitRarity.RARE),
    ASTER("Aster", RabbitFaction.COUNTRY, RabbitRarity.RARE),
    LAVENDER("Lavender", RabbitFaction.COUNTRY, RabbitRarity.EPIC),
    JUNIPER("Juniper", RabbitFaction.COUNTRY, RabbitRarity.EPIC),
    ROSEBUD("Rosebud", RabbitFaction.COUNTRY, RabbitRarity.LEGENDARY),
    TAN("Tan", RabbitFaction.BEACH, RabbitRarity.COMMON),
    PALM("Palm", RabbitFaction.BEACH, RabbitRarity.COMMON),
    SPLASH("Splash", RabbitFaction.BEACH, RabbitRarity.COMMON),
    SEASHELL("Seashell", RabbitFaction.BEACH, RabbitRarity.COMMON),
    SANDY("Sandy", RabbitFaction.BEACH, RabbitRarity.COMMON),
    SEABUN("Seabun", RabbitFaction.BEACH, RabbitRarity.UNCOMMON),
    LAGOON("Lagoon", RabbitFaction.BEACH, RabbitRarity.UNCOMMON),
    MOCKTAIL("Mocktail", RabbitFaction.BEACH, RabbitRarity.UNCOMMON),
    ANCHOR("Anchor", RabbitFaction.BEACH, RabbitRarity.UNCOMMON),
    COCONUT("Coconut", RabbitFaction.BEACH, RabbitRarity.RARE),
    STARFISH("Starfish", RabbitFaction.BEACH, RabbitRarity.RARE),
    OASIS("Oasis", RabbitFaction.BEACH, RabbitRarity.RARE),
    DUNE("Dune", RabbitFaction.BEACH, RabbitRarity.EPIC),
    SURF("Surf", RabbitFaction.BEACH, RabbitRarity.EPIC),
    SUNSET("Sunset", RabbitFaction.BEACH, RabbitRarity.LEGENDARY);

    private static final String ITEM_PREFIX = "FACTION_RABBIT_";

    private final String displayName;
    private final RabbitFaction faction;
    private final RabbitRarity rarity;

    FactionRabbit(String displayName, RabbitFaction faction, RabbitRarity rarity) {
        this.displayName = displayName;
        this.faction = faction;
        this.rarity = rarity;
    }

    public String getItemId() {
        return ITEM_PREFIX + name();
    }

    public @Nullable ItemType getItemType() {
        return ItemType.get(getItemId());
    }

    public int getChocolateBonus() {
        return rarity.getChocolateBonus();
    }

    public double getMultiplierBonus() {
        return rarity.getMultiplierBonus();
    }

    public Text getFormattedName() {
        return Text.of("<color:{}>{}", rarity.getColorCode(), displayName);
    }

    public static @Nullable FactionRabbit byId(String id) {
        if (id == null) return null;
        try {
            return valueOf(id);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static @Nullable FactionRabbit byItemId(String itemId) {
        if (itemId == null || !itemId.startsWith(ITEM_PREFIX)) return null;
        return byId(itemId.substring(ITEM_PREFIX.length()));
    }

    public static List<FactionRabbit> of(RabbitFaction faction, RabbitRarity rarity) {
        List<FactionRabbit> rabbits = new ArrayList<>();
        for (FactionRabbit rabbit : values()) {
            if (rabbit.faction == faction && rabbit.rarity == rarity) rabbits.add(rabbit);
        }
        return rabbits;
    }

    public static List<FactionRabbit> of(RabbitFaction faction) {
        List<FactionRabbit> rabbits = new ArrayList<>();
        for (FactionRabbit rabbit : values()) {
            if (rabbit.faction == faction) rabbits.add(rabbit);
        }
        return rabbits;
    }
}
