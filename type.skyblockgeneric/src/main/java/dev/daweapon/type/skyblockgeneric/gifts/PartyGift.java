package dev.daweapon.type.skyblockgeneric.gifts;

import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributePotionData;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalPerk;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalStatistics;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Party Gift, dropped by Winter Sea Creatures while Foxy's "A Time for Giving" perk is active.
 * Like other Gifts, it is used on another player and both players get the reward. Weights are the
 * wiki's (total 1,674). Party Gifts give no North Stars.
 */
public final class PartyGift {
    private static final double FEELIN_FESTIVE_PERCENT = 10;
    private static final int XP_BOOST_DURATION_SECONDS = 1800;
    private static final List<String> XP_BOOST_EFFECTS = List.of("FARMING_XP_BOOST", "MINING_XP_BOOST",
            "COMBAT_XP_BOOST", "FORAGING_XP_BOOST", "FISHING_XP_BOOST", "ALCHEMY_XP_BOOST", "ENCHANTING_XP_BOOST");
    private static final SkillCategories[] XP_SKILLS = {
            SkillCategories.FARMING, SkillCategories.MINING, SkillCategories.COMBAT,
            SkillCategories.FORAGING, SkillCategories.FISHING};

    public enum Tier {
        COMMON("<f><l>COMMON!"),
        RARE("<9><l>RARE!"),
        SWEET("<6><l>SWEET!"),
        PARTY_TIER("<c><l>PARTY TIER!");

        private final String label;

        Tier(String label) {
            this.label = label;
        }
    }

    public record Reward(Tier tier, int weight, long coins, long skillXp, int xpBoostLevel,
                         @Nullable ItemType item) {
        static Reward coins(Tier tier, int weight, long amount) {
            return new Reward(tier, weight, amount, 0, 0, null);
        }

        static Reward xp(Tier tier, int weight, long amount) {
            return new Reward(tier, weight, 0, amount, 0, null);
        }

        static Reward xpBoost(Tier tier, int weight, int level) {
            return new Reward(tier, weight, 0, 0, level, null);
        }

        static Reward item(Tier tier, int weight, ItemType item) {
            return new Reward(tier, weight, 0, 0, 0, item);
        }
    }

    public static final List<Reward> REWARDS = List.of(
            Reward.coins(Tier.COMMON, 460, 25_000),
            Reward.coins(Tier.RARE, 75, 75_000),
            Reward.coins(Tier.SWEET, 75, 150_000),
            Reward.coins(Tier.PARTY_TIER, 1, 1_500_000),
            Reward.xp(Tier.COMMON, 480, 1_000),
            Reward.xp(Tier.COMMON, 80, 2_500),
            Reward.xp(Tier.COMMON, 25, 5_000),
            Reward.xpBoost(Tier.COMMON, 75, 2),
            Reward.xpBoost(Tier.RARE, 350, 3),
            Reward.item(Tier.PARTY_TIER, 3, ItemType.PARTY_THE_FISH),
            Reward.item(Tier.PARTY_TIER, 5, ItemType.PARTY_HAT),
            Reward.item(Tier.PARTY_TIER, 15, ItemType.PARTY_CLOAK),
            Reward.item(Tier.PARTY_TIER, 15, ItemType.PARTY_GLOVES),
            Reward.item(Tier.PARTY_TIER, 15, ItemType.SNOWMAN_MASK));

    private PartyGift() {
    }

    public static boolean isPartyGift(ItemType type) {
        return type == ItemType.PARTY_GIFT;
    }

    public static Reward roll(@Nullable UUID player, RandomGenerator random) {
        return LootRoll.pick(player, REWARDS, Reward::item, Reward::weight, random);
    }

    public static void give(SkyBlockPlayer giver, SkyBlockPlayer receiver) {
        Reward reward = roll(giver.getUuid(), ThreadLocalRandom.current());
        SkillCategories skill = XP_SKILLS[ThreadLocalRandom.current().nextInt(XP_SKILLS.length)];
        String boostEffect = XP_BOOST_EFFECTS.get(ThreadLocalRandom.current().nextInt(XP_BOOST_EFFECTS.size()));
        for (SkyBlockPlayer player : List.of(giver, receiver)) {
            award(player, reward, skill, boostEffect);
        }
    }

    private static void award(SkyBlockPlayer player, Reward reward, SkillCategories skill, String boostEffect) {
        String prefix = reward.tier().label + " </l>";
        double boost = boost(player);
        if (reward.coins() > 0) {
            long coins = Math.round(reward.coins() * boost);
            player.addCoins(coins);
            player.sendMessage(prefix + "<7>You received <6>{} coins <7>from the Party Gift!", StringUtility.commaify(coins));
        } else if (reward.skillXp() > 0) {
            long xp = Math.round(reward.skillXp() * boost);
            player.getSkills().increase(player, skill, (double) xp);
            player.sendMessage(prefix + "<7>You received <3>{} {} XP <7>from the Party Gift!",
                    StringUtility.commaify(xp), StringUtility.toNormalCase(skill.name()));
        } else if (reward.xpBoostLevel() > 0) {
            SkyBlockItem potion = xpBoostPotion(boostEffect, reward.xpBoostLevel());
            player.addAndUpdateItem(potion);
            player.sendMessage(prefix + "<7>You received a {} <7>from the Party Gift!", potion.getDisplayNameText());
        } else if (reward.item() != null) {
            SkyBlockItem item = new SkyBlockItem(reward.item());
            player.addAndUpdateItem(item);
            player.sendMessage(prefix + "<7>You received a {} <7>from the Party Gift!", item.getDisplayNameText());
        }
    }

    private static SkyBlockItem xpBoostPotion(String effect, int level) {
        SkyBlockItem potion = new SkyBlockItem(Material.POTION);
        potion.getAttributeHandler().setPotionData(
                new ItemAttributePotionData.PotionData(effect, level, XP_BOOST_DURATION_SECONDS));
        potion.getAttributeHandler().setRarity(level >= 3 ? Rarity.UNCOMMON : Rarity.COMMON);
        return potion;
    }

    private static double boost(SkyBlockPlayer player) {
        double percent = Carnivals.perkValue(player, CarnivalPerk.PROFESSIONAL_GIFTER)
                + (CarnivalStatistics.isWearing(player, ItemType.SNOWMAN_MASK) ? FEELIN_FESTIVE_PERCENT : 0);
        return 1 + percent / 100D;
    }
}
