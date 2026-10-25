package dev.daweapon.type.skyblockgeneric.spidersden;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;

import java.time.LocalTime;
import java.util.List;

/**
 * The two Arachne fights. Numbers and dialogue are the wiki's current (Aug 2026) values; the older 1M and 20M HP
 * lines are not used.
 */
public enum ArachneTier {
    LESSER("ARACHNE_KEEPER_FRAGMENT", 4, 0, SpiderDenMobs.ARACHNE_LESSER, SpiderDenMobs.BROOD_LESSER, 2_000, 8, 30,
            List.of("Ahhhh...A calling...", "For me...", "That smell...human flesh...and blood.", "Fresh meat, raw and juicy."),
            List.of("A befitting welcome!"),
            List.of("Spiders in my Den, can you count to ten?"),
            List.of("A rough fight!", "I have to stretch my legs.", "Arise my children!"),
            List.of(),
            List.of("No, this is impossible...", "I will be back, even stronger!")),
    GREATER("ARACHNE_CRYSTAL", 1, 15, SpiderDenMobs.ARACHNE_GREATER, SpiderDenMobs.BROOD_GREATER, 10_000, 30, 30,
            List.of("So it is time.", "I have been waiting.", "The Era of Spiders begins now.", "Let us complete the ritual."),
            List.of("With your sacrifice."),
            List.of("A combative breakfast, I wouldn't have it any other way!"),
            List.of("Can you compete with this!"),
            List.of("Don't you see? You are trapped.", "Stop resisting."),
            List.of("How can this be...What a humiliation.", "This isn't the end. You will learn how resourceful spiders are."));

    private final String summonItem;
    private final int itemsRequired;
    private final int combatLevel;
    private final CrimsonMobProfile arachne;
    private final CrimsonMobProfile brood;
    private final double broodHitCap;
    private final int essence;
    private final double firstKillSkyBlockXP;
    private final List<String> spawning;
    private final List<String> spawned;
    private final List<String> firstSwarm;
    private final List<String> secondSwarm;
    private final List<String> afterSecondSwarm;
    private final List<String> defeated;

    ArachneTier(String summonItem, int itemsRequired, int combatLevel, CrimsonMobProfile arachne, CrimsonMobProfile brood,
                double broodHitCap, int essence, double firstKillSkyBlockXP, List<String> spawning, List<String> spawned,
                List<String> firstSwarm, List<String> secondSwarm, List<String> afterSecondSwarm, List<String> defeated) {
        this.summonItem = summonItem;
        this.itemsRequired = itemsRequired;
        this.combatLevel = combatLevel;
        this.arachne = arachne;
        this.brood = brood;
        this.broodHitCap = broodHitCap;
        this.essence = essence;
        this.firstKillSkyBlockXP = firstKillSkyBlockXP;
        this.spawning = spawning;
        this.spawned = spawned;
        this.firstSwarm = firstSwarm;
        this.secondSwarm = secondSwarm;
        this.afterSecondSwarm = afterSecondSwarm;
        this.defeated = defeated;
    }

    public String summonItem() { return summonItem; }
    public int itemsRequired() { return itemsRequired; }
    public int combatLevel() { return combatLevel; }
    public CrimsonMobProfile arachne() { return arachne; }
    public CrimsonMobProfile brood() { return brood; }
    public double broodHitCap() { return broodHitCap; }
    public int essence() { return essence; }
    public double firstKillSkyBlockXP() { return firstKillSkyBlockXP; }
    public List<String> spawning() { return spawning; }
    public List<String> spawned() { return spawned; }
    public List<String> secondSwarm() { return secondSwarm; }
    public List<String> afterSecondSwarm() { return afterSecondSwarm; }
    public List<String> defeated() { return defeated; }

    public List<String> firstSwarm(LocalTime time) {
        if (this == LESSER) return firstSwarm;
        String meal = time.getHour() < 11 ? "breakfast" : time.getHour() < 17 ? "lunch" : "dinner";
        return List.of("A combative " + meal + ", I wouldn't have it any other way!");
    }

    public int broodFirst() {
        return 10;
    }

    public int broodSecond() {
        return 20;
    }
}
