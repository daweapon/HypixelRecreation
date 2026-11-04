package dev.daweapon.type.theend.race;

import dev.daweapon.commons.skyblock.item.ItemType;

import java.util.List;

public enum EndRaceTier {
    THREE_MINUTES(180, ItemType.SILENT_PEARL, 8,
            List.of("Give it a go! Can you finish the race in 3 minutes or less?",
                    "Run over the pressure plate to start racing!"),
            List.of("What are you talking to me for? Get back in the race!",
                    "Run across The End to the <e>opposite side of the island <f>to complete the race!"),
            List.of("Not bad, <7>{player}<f>!",
                    "But you can get a much quicker time if you use the items that I do.",
                    "Take these <5>Silent Pearls<f>, they allow you to teleport without angering Endermen.",
                    "Try again, but this time come back in 2 minutes or less!")),
    TWO_MINUTES(120, ItemType.ENDERMAN_MASK, 1,
            List.of("Keep it up! Can you finish the race in 2 minutes or less?",
                    "Run over the pressure plate to start racing!"),
            List.of("You shouldn't be talking to me if you want to beat 2 minutes!"),
            List.of("You're getting much faster!",
                    "Have this <5>Enderman Mask<f>! I wear it sometimes to make me feel like an Enderman.",
                    "See if you can beat 80 seconds!")),
    EIGHTY_SECONDS(80, ItemType.NOPE_THE_FISH, 1,
            List.of("Keep at it! Can you finish the race in 80 seconds or less?",
                    "Run over the pressure plate to start racing!"),
            List.of("How do you suppose you'll beat 80 seconds by talking to me?",
                    "Get back in the race! You only have 80 seconds!"),
            List.of("Now we're talking! You're getting close to my record!",
                    "Here's a weird item that I have no use for. It won't help you beat my personal best, but it's your item now.",
                    "Finish the race in under 48 seconds and come talk to me! I promise the reward will be better!")),
    FORTY_EIGHT_SECONDS(48, ItemType.PIGS_FOOT, 1,
            List.of("You're so close!! Can you finish the race in 48 seconds or less and beat my personal record?",
                    "Run over the pressure plate to start racing!"),
            List.of("Get back in the race! You only have 48 seconds!",
                    "Get back in the race! 48 seconds isn't enough time for there to be chit chat!",
                    "You'll never beat my personal best if you waste this much time! Yikes!"),
            List.of("You did it! Congratulations, <7>{player}<f>. You are now faster than <d>Guber<f>!",
                    "A legendary speedster once ran this island, some say he finished the race in mere seconds!",
                    "He left behind this <d>Pig's Foot<f>. I want you to have it, as you have proven to be the best of the best!",
                    "As you can see, this item is quite powerful. It makes you incredibly fast!"));

    public static final long ACHIEVEMENT_MILLIS = 42_000;

    private final int seconds;
    private final ItemType reward;
    private final int amount;
    private final List<String> idleLines;
    private final List<String> racingLines;
    private final List<String> rewardLines;

    EndRaceTier(int seconds, ItemType reward, int amount, List<String> idleLines, List<String> racingLines,
                List<String> rewardLines) {
        this.seconds = seconds;
        this.reward = reward;
        this.amount = amount;
        this.idleLines = idleLines;
        this.racingLines = racingLines;
        this.rewardLines = rewardLines;
    }

    public boolean beatenBy(long millis) {
        return millis < seconds * 1000L;
    }

    public ItemType reward() { return reward; }
    public int amount() { return amount; }
    public List<String> idleLines() { return idleLines; }
    public List<String> racingLines() { return racingLines; }
    public List<String> rewardLines() { return rewardLines; }
}
