package dev.daweapon.type.skyblockgeneric.abiphone.ninef;

import dev.daweapon.commons.ServerType;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum NineFRelay {
    WILDERNESS_TREE(1, ServerType.SKYBLOCK_HUB, new Pos(143.5, 108, 93.5), 1, 0, 60, 0, List.of(
            "Hey! It's you! On the Abiphone!!!",
            "The first relay is on a branch of the large tree on the north-east of the fairy pond.",
            "Make sure to wear Fairy Armor!",
            "I don't personally know what the maintenance procedure is, but I'm sure you can figure it out!")),
    CASTLE_RUINS(2, ServerType.SKYBLOCK_HUB, new Pos(-245.5, 123, 55.5), 5, 0, 56, 0, List.of(
            "Wow, you figured out the maintenance plan!",
            "That's really smart!",
            "The next relay is in the castle ruins!")),
    ROYAL_PALACE(3, ServerType.SKYBLOCK_DWARVEN_MINES, new Pos(129.5, 232, 201.5), 9, 0, 52, 0.15, List.of(
            "The next relay is in the <b>Royal Palace <f>within the <2>Dwarven Mines<f>.",
            "It's in a very high place to reverb the signal for the royal council.",
            "Although I don't think technology can help with their communication problems...")),
    END_SPIKE(4, ServerType.SKYBLOCK_THE_END, new Pos(-557.5, 163, -285.5), 13, 1, 49, 0.25, List.of(
            "The next relay is on the highest spike of <d>The End<f>.",
            "It's on the face which looks at the moon rising.",
            "Or the sun! If you're <o>that <r><f>kind of person...")),
    BLAZING_VOLCANO(5, ServerType.SKYBLOCK_CRIMSON_ISLE, new Pos(-373.5, 207, -797.5), 17, 1, 45, 0.35, List.of(
            "The next relay was placed by our consultant, Odger.",
            "He said \"Travel too far home. Bye.\" after installing it and never called again.",
            "Maybe he can tell you where he put the relay?")),
    CATHEDRAL(6, ServerType.SKYBLOCK_CRIMSON_ISLE, new Pos(-67.5, 157, -877.5), 21, 1, 41, 0.45, List.of(
            "Scarleton itself has one of the most robust connection to the 9f™ Network.",
            "If you face the cathedral, there is a relay at the top of the left tower.")),
    SHADY_SHACK(7, ServerType.SKYBLOCK_HUB, new Pos(93.5, 86, 187.5), 25, 1, 38, 0.55, List.of(
            "The next relay is on top of the shack next to the shady inn right here close to the pond.",
            "Usually we wouldn't have a relay serve so few people, but we were paid generously by anonymous contributors!",
            "So generously in fact that we also share some of the data from the 9f™ Network with one of the mayor candidates.",
            "But hey, let's keep this between us!",
            "<o>They'll know <r><f>if you tell anyone.")),
    DUNGEON_HUB_STATUE(8, ServerType.SKYBLOCK_DUNGEON_HUB, new Pos(0.5, 146, -74.5), 29, 2, 34, 0.65, List.of(
            "The next relay is on top of a statue in the dungeon hub.",
            "The one that doesn't have a fairy soul...")),
    AUCTION_HOUSE(9, ServerType.SKYBLOCK_HUB, new Pos(-22.5, 86, -11.5), 33, 2, 30, 0.75, List.of(
            "The next relay is on top of the <6>Auction House<f>.",
            "It offers service to the whole village!"));

    public static final int HEALTH_PER_RELAY = 2;

    private final int number;
    private final ServerType server;
    private final Pos position;
    private final int contactsNeeded;
    private final int bonusContacts;
    private final int timerSeconds;
    private final double bombChance;
    private final List<String> tip;

    NineFRelay(int number, ServerType server, Pos position, int contactsNeeded, int bonusContacts, int timerSeconds,
               double bombChance, List<String> tip) {
        this.number = number;
        this.server = server;
        this.position = position;
        this.contactsNeeded = contactsNeeded;
        this.bonusContacts = bonusContacts;
        this.timerSeconds = timerSeconds;
        this.bombChance = bombChance;
        this.tip = tip;
    }

    public int number() {
        return number;
    }

    public ServerType server() {
        return server;
    }

    public Pos position() {
        return position;
    }

    public int contactsNeeded() {
        return contactsNeeded;
    }

    public int timerSeconds() {
        return timerSeconds;
    }

    public double bombChance() {
        return bombChance;
    }

    public List<String> tip() {
        return tip;
    }

    public static @Nullable NineFRelay byNumber(int number) {
        for (NineFRelay relay : values()) if (relay.number == number) return relay;
        return null;
    }

    public static int healthFor(int repaired) {
        return Math.max(0, Math.min(repaired, values().length)) * HEALTH_PER_RELAY;
    }

    public static int bonusContactsFor(int repaired) {
        int total = 0;
        for (NineFRelay relay : values()) if (relay.number <= repaired) total += relay.bonusContacts;
        return total;
    }
}
