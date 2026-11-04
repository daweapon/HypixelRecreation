package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.type.skyblockgeneric.century.raffle.RaffleAction.Trigger;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;

import java.util.function.Predicate;

public enum RaffleTask {
    CATCH_SEA_CREATURE(Difficulty.EASY, "Catch a Sea Creature.", action -> action.trigger() == Trigger.SEA_CREATURE),
    DWARVEN_COMMISSION(Difficulty.EASY, "Complete a Commission in the Dwarven Mines.", action -> action.is(Trigger.COMMISSION, "DWARVEN_MINES")),
    EAT_CAKE(Difficulty.EASY, "Eat a slice of Century Cake.", action -> action.trigger() == Trigger.EAT_CAKE),
    FEED_SLICE(Difficulty.EASY, "Feed another player a Slice of Century Cake.", action -> action.trigger() == Trigger.FEED_SLICE),
    BRONZE_TROPHY(Difficulty.EASY, "Fish up a BRONZE or higher tier Trophy.", action -> action.trigger() == Trigger.TROPHY_FISH),
    ANY_TREASURE(Difficulty.EASY, "Fish up any Treasure.", action -> action.trigger() == Trigger.TREASURE),
    KILL_CUBIC(Difficulty.EASY, "Kill a Cubic mob.", action -> action.killed(MobType.CUBIC)),
    KILL_SKELETAL(Difficulty.EASY, "Kill a Skeletal mob.", action -> action.killed(MobType.SKELETAL)),
    KILL_ANIMAL(Difficulty.EASY, "Kill an Animal mob.", action -> action.killed(MobType.ANIMAL)),
    KILL_AQUATIC(Difficulty.EASY, "Kill an Aquatic mob.", action -> action.killed(MobType.AQUATIC)),
    KILL_ARTHROPOD(Difficulty.EASY, "Kill an Arthropod mob.", action -> action.killed(MobType.ARTHROPOD)),
    KILL_ENDER(Difficulty.EASY, "Kill an Ender mob.", action -> action.killed(MobType.ENDER)),
    KILL_UNDEAD(Difficulty.EASY, "Kill an Undead mob.", action -> action.killed(MobType.UNDEAD)),
    SLAYER_TIER_ONE(Difficulty.EASY, "Kill Tier I or higher Slayer Boss.", action -> action.trigger() == Trigger.SLAYER_BOSS && action.value() >= 1),
    MINE_DWARVEN_METAL(Difficulty.EASY, "Mine a Dwarven Metal.", action -> action.trigger() == Trigger.ITEM_MINED
            && RaffleTask.isDwarvenMetal(action.subject())),
    MINE_GEMSTONE(Difficulty.EASY, "Mine a Gemstone.", action -> action.trigger() == Trigger.ITEM_MINED
            && action.subject().startsWith("ROUGH_") && action.subject().endsWith("_GEM")),
    MINE_BLOCK(Difficulty.EASY, "Mine a Block.", action -> action.trigger() == Trigger.BLOCK_MINED),
    OBTAIN_TICKET(Difficulty.EASY, "Obtain a Raffle Ticket.", action -> action.trigger() == Trigger.TICKET),
    FIG_GIFT(Difficulty.EASY, "Open a Fig Tree Gift.", action -> action.is(Trigger.TREE_GIFT, "FIG")),

    CATCH_MITHRIL_GRUBBER(Difficulty.MEDIUM, "Catch a Mithril Grubber.", action -> action.is(Trigger.SEA_CREATURE, "MITHRIL_GRUBBER")),
    HOLLOWS_COMMISSION(Difficulty.MEDIUM, "Complete a Commission in the Crystal Hollows.", action -> action.is(Trigger.COMMISSION, "CRYSTAL_HOLLOWS")),
    BASIC_KUUDRA(Difficulty.MEDIUM, "Complete a Basic or higher Kuudra run.", action -> action.trigger() == Trigger.KUUDRA),
    GREAT_TREASURE(Difficulty.MEDIUM, "Fish up a GREAT Treasure.", action -> action.trigger() == Trigger.TREASURE
            && (action.subject().equals("GREAT") || action.subject().equals("OUTSTANDING"))),
    SILVER_TROPHY(Difficulty.MEDIUM, "Fish up a SILVER or higher tier Trophy.", action -> action.trigger() == Trigger.TROPHY_FISH && action.value() >= 2),
    KILL_ARACHNE(Difficulty.MEDIUM, "Kill Arachne.", action -> action.killedNamed("Arachne")),
    KILL_BARBARIAN_DUKE(Difficulty.MEDIUM, "Kill Barbarian Duke X.", action -> action.killedNamed("Barbarian Duke X")),
    KILL_BLADESOUL(Difficulty.MEDIUM, "Kill Bladesoul.", action -> action.killedNamed("Bladesoul")),
    KILL_STAR_SENTRY(Difficulty.MEDIUM, "Kill a Star Sentry.", action -> action.killedNamed("Star Sentry")),
    KILL_MAGE_OUTLAW(Difficulty.MEDIUM, "Kill the Mage Outlaw.", action -> action.killedNamed("Mage Outlaw")),
    KILL_CONSTRUCT(Difficulty.MEDIUM, "Kill a Construct mob.", action -> action.killed(MobType.CONSTRUCT)),
    KILL_GLACIAL(Difficulty.MEDIUM, "Kill a Glacial mob.", action -> action.killed(MobType.GLACIAL)),
    KILL_HUMANOID(Difficulty.MEDIUM, "Kill a Humanoid mob.", action -> action.killed(MobType.HUMANOID)),
    KILL_MAGMATIC(Difficulty.MEDIUM, "Kill a Magmatic mob.", action -> action.killed(MobType.MAGMATIC)),
    KILL_SUBTERRANEAN(Difficulty.MEDIUM, "Kill a Subterranean mob.", action -> action.killed(MobType.SUBTERRANEAN)),
    KILL_WOODLAND(Difficulty.MEDIUM, "Kill a Woodland mob.", action -> action.killed(MobType.WOODLAND)),
    KILL_ARCANE(Difficulty.MEDIUM, "Kill an Arcane mob.", action -> action.killed(MobType.ARCANE)),
    KILL_INFERNAL(Difficulty.MEDIUM, "Kill an Infernal mob.", action -> action.killed(MobType.INFERNAL)),
    SLAYER_TIER_THREE(Difficulty.MEDIUM, "Kill Tier III or higher Slayer Boss.", action -> action.trigger() == Trigger.SLAYER_BOSS && action.value() >= 3),
    MANGROVE_GIFT(Difficulty.MEDIUM, "Open a Mangrove Tree Gift.", action -> action.is(Trigger.TREE_GIFT, "MANGROVE")),

    CATCH_CARROT_KING(Difficulty.HARD, "Catch a Carrot King.", action -> action.is(Trigger.SEA_CREATURE, "CARROT_KING")),
    CATCH_LOCH_EMPEROR(Difficulty.HARD, "Catch a Loch Emperor.", action -> action.is(Trigger.SEA_CREATURE, "THE_LOCH_EMPEROR")),
    CATCH_AGARIMOO(Difficulty.HARD, "Catch an Agarimoo.", action -> action.is(Trigger.SEA_CREATURE, "AGARIMOO")),
    BURNING_KUUDRA(Difficulty.HARD, "Complete a Burning or higher Kuudra run.", action -> action.trigger() == Trigger.KUUDRA && action.value() >= 2),
    GOLD_TROPHY(Difficulty.HARD, "Fish up a GOLD or higher tier Trophy.", action -> action.trigger() == Trigger.TROPHY_FISH && action.value() >= 3),
    OUTSTANDING_TREASURE(Difficulty.HARD, "Fish up an OUTSTANDING Treasure.", action -> action.is(Trigger.TREASURE, "OUTSTANDING")),
    KILL_ASHFANG(Difficulty.HARD, "Kill Ashfang.", action -> action.killedNamed("Ashfang")),
    KILL_LITTLEFOOT(Difficulty.HARD, "Kill Littlefoot.", action -> action.killedNamed("Littlefoot")),
    KILL_SHIELDED(Difficulty.HARD, "Kill a Shielded mob.", action -> action.killed(MobType.SHIELDED)),
    KILL_WITHER(Difficulty.HARD, "Kill a Wither mob.", action -> action.killed(MobType.WITHER)),
    KILL_ELUSIVE(Difficulty.HARD, "Kill an Elusive mob.", action -> action.killed(MobType.ELUSIVE)),
    SLAYER_TIER_FIVE(Difficulty.HARD, "Kill a Tier V Slayer Boss.", action -> action.trigger() == Trigger.SLAYER_BOSS && action.value() >= 5),
    MINE_JASPER(Difficulty.HARD, "Mine a Jasper Gemstone.", action -> action.is(Trigger.ITEM_MINED, "ROUGH_JASPER_GEM"));

    public enum Difficulty {
        EASY("<a>Easy"),
        MEDIUM("<e>Medium"),
        HARD("<c>Hard");

        private final String display;

        Difficulty(String display) {
            this.display = display;
        }

        public String display() {
            return display;
        }
    }

    private final Difficulty difficulty;
    private final String description;
    private final Predicate<RaffleAction> matcher;

    RaffleTask(Difficulty difficulty, String description, Predicate<RaffleAction> matcher) {
        this.difficulty = difficulty;
        this.description = description;
        this.matcher = matcher;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public String description() {
        return description;
    }

    public boolean matches(RaffleAction action) {
        return matcher.test(action);
    }

    static boolean isDwarvenMetal(String itemId) {
        return switch (itemId) {
            case "MITHRIL", "TITANIUM", "UMBER", "TUNGSTEN" -> true;
            default -> false;
        };
    }
}
