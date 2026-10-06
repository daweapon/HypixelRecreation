package net.swofty.type.skyblockgeneric.gifts;

import lombok.Getter;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The gifts players hand to each other, with the wiki's coin and Skill XP rewards and North Star
 * odds. Both the giver and the receiver get what the gift holds. The wiki's item rewards - XP
 * boost potions, runes, discs, minion skins - are left out until those items exist, so the coin and
 * XP rows are drawn in their own proportions.
 */
@Getter
public enum Gift {
    WHITE(ItemType.WHITE_GIFT, 0.10, 1, 1, List.of(
            Reward.coins(2000, 2_000), Reward.coins(1000, 5_000), Reward.coins(500, 10_000),
            Reward.xp(1500, 250), Reward.xp(200, 500))),
    GREEN(ItemType.GREEN_GIFT, 0.30, 1, 3, List.of(
            Reward.coins(2000, 5_000), Reward.coins(1000, 10_000), Reward.coins(20, 20_000),
            Reward.xp(1500, 250), Reward.xp(500, 500), Reward.xp(1, 2_500))),
    RED(ItemType.RED_GIFT, 0.50, 1, 5, List.of(
            Reward.coins(1032, 5_000), Reward.coins(1935, 20_000), Reward.coins(52, 100_000), Reward.coins(3, 1_000_000),
            Reward.xp(258, 250), Reward.xp(2580, 500), Reward.xp(13, 2_500)));

    // Gift Skill XP is not tied to one skill on the wiki; it goes to one of the gathering and
    // combat skills at random here.
    private static final SkillCategories[] XP_SKILLS = {
            SkillCategories.FARMING, SkillCategories.MINING, SkillCategories.COMBAT,
            SkillCategories.FORAGING, SkillCategories.FISHING};

    private final ItemType item;
    private final double northStarChance;
    private final int minNorthStars;
    private final int maxNorthStars;
    private final List<Reward> rewards;

    Gift(ItemType item, double northStarChance, int minNorthStars, int maxNorthStars, List<Reward> rewards) {
        this.item = item;
        this.northStarChance = northStarChance;
        this.minNorthStars = minNorthStars;
        this.maxNorthStars = maxNorthStars;
        this.rewards = rewards;
    }

    record Reward(int weight, long coins, long skillXp) {
        static Reward coins(int weight, long amount) {
            return new Reward(weight, amount, 0);
        }

        static Reward xp(int weight, long amount) {
            return new Reward(weight, 0, amount);
        }
    }

    public static Gift of(ItemType type) {
        for (Gift gift : values()) {
            if (gift.item == type) return gift;
        }
        return null;
    }

    public void give(SkyBlockPlayer giver, SkyBlockPlayer receiver) {
        Reward reward = roll();
        SkillCategories skill = XP_SKILLS[ThreadLocalRandom.current().nextInt(XP_SKILLS.length)];
        for (SkyBlockPlayer player : List.of(giver, receiver)) {
            if (reward.coins() > 0) {
                player.addCoins(reward.coins());
                player.sendMessage("<a><l>GIFT! <7>You received <6>{} coins <7>from the gift!", StringUtility.commaify(reward.coins()));
            } else {
                player.getSkills().increase(player, skill, (double) reward.skillXp());
                player.sendMessage("<a><l>GIFT! <7>You received <3>{} {} XP <7>from the gift!",
                        StringUtility.commaify(reward.skillXp()), StringUtility.toNormalCase(skill.name()));
            }
            awardNorthStars(player);
        }
    }

    private void awardNorthStars(SkyBlockPlayer player) {
        if (ThreadLocalRandom.current().nextDouble() >= northStarChance) return;
        int amount = ThreadLocalRandom.current().nextInt(minNorthStars, maxNorthStars + 1);
        DatapointInteger stars = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.NORTH_STARS, DatapointInteger.class);
        stars.setValue(stars.getValue() + amount);
        player.sendMessage("<b>+{} North Star{}", amount, amount == 1 ? "" : "s");
    }

    private Reward roll() {
        int roll = ThreadLocalRandom.current().nextInt(rewards.stream().mapToInt(Reward::weight).sum());
        for (Reward reward : rewards) {
            roll -= reward.weight();
            if (roll < 0) return reward;
        }
        return rewards.getFirst();
    }
}
