package dev.daweapon.type.skyblockgeneric.gifts;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributePotionData;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.entity.drop.ItemDrops;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.potion.PotionEffectType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * Opening a placed gift: the opener's roll picks one reward that both players receive, coins and
 * Skill XP scaled by each player's own gift boosts, items dropped at the gift for only those two.
 */
public final class GiftOpening {
    // The wiki does not say which skill gift Skill XP goes to; one of these is picked per gift.
    private static final SkillCategories[] XP_SKILLS = {
            SkillCategories.FARMING, SkillCategories.MINING, SkillCategories.COMBAT,
            SkillCategories.FORAGING, SkillCategories.FISHING};
    private static final PotionEffectType[] XP_BOOST_POTIONS = {
            PotionEffectType.FARMING_XP_BOOST, PotionEffectType.MINING_XP_BOOST, PotionEffectType.COMBAT_XP_BOOST,
            PotionEffectType.FORAGING_XP_BOOST, PotionEffectType.FISHING_XP_BOOST, PotionEffectType.ALCHEMY_XP_BOOST,
            PotionEffectType.ENCHANTING_XP_BOOST};

    private GiftOpening() {
    }

    public static void open(Gift gift, SkyBlockPlayer giver, SkyBlockPlayer opener, Instance instance, Pos position) {
        RandomGenerator random = ThreadLocalRandom.current();
        GiftReward reward = gift.pick(opener.getUuid(), GiftBoosts.rareItemMultiplier(opener), random);
        SkillCategories skill = XP_SKILLS[random.nextInt(XP_SKILLS.length)];
        PotionEffectType potion = XP_BOOST_POTIONS[random.nextInt(XP_BOOST_POTIONS.length)];
        boolean hollyDye = gift.rollHollyDye(opener.getUuid(), random);

        List<SkyBlockPlayer> recipients = new ArrayList<>(List.of(opener));
        if (isPresent(giver)) recipients.add(giver);

        for (SkyBlockPlayer player : recipients) {
            SkyBlockPlayer partner = player == opener ? giver : opener;
            Text received = grant(player, reward, skill, potion, instance, position);
            player.sendMessage(Text.of("{} {} <e>gift with {}<e>!", Text.of(reward.rarity().label()), received,
                    partner.getColouredName()));
            if (hollyDye) {
                SkyBlockItem dye = new SkyBlockItem(ItemType.DYE_HOLLY);
                drop(player, dye, instance, position);
                player.sendMessage(Text.of("{} {} <e>gift with {}<e>!", Text.of(GiftDropRarity.SANTA.label()),
                        dye.getDisplayNameText(), partner.getColouredName()));
            }
            awardNorthStars(player, gift, random);
        }

        if (reward.rarity() == GiftDropRarity.SANTA) {
            broadcastSanta(giver, opener, describe(reward, skill, potion, 1), gift);
        }
        if (hollyDye) {
            broadcastSanta(giver, opener, new SkyBlockItem(ItemType.DYE_HOLLY).getDisplayNameText(), gift);
        }
        if (isPresent(giver)) {
            SnowSuit.addGiftGiven(giver);
            GiftAchievements.record(giver, opener);
        }
    }

    private static Text grant(SkyBlockPlayer player, GiftReward reward, SkillCategories skill, PotionEffectType potion,
                              Instance instance, Pos position) {
        double boost = GiftBoosts.coinAndXpMultiplier(player);
        switch (reward.kind()) {
            case COINS -> {
                long coins = Math.round(reward.amount() * boost);
                player.addCoins(coins);
                return describe(reward, skill, potion, boost);
            }
            case SKILL_XP -> {
                player.getSkills().increase(player, skill, (double) Math.round(reward.amount() * boost));
                return describe(reward, skill, potion, boost);
            }
            default -> {
                SkyBlockItem item = createItem(reward, potion);
                drop(player, item, instance, position);
                return item.getDisplayNameText();
            }
        }
    }

    private static Text describe(GiftReward reward, SkillCategories skill, PotionEffectType potion, double boost) {
        return switch (reward.kind()) {
            case COINS -> Text.of("<6>+{} coins", StringUtility.commaify(Math.round(reward.amount() * boost)));
            case SKILL_XP -> Text.of("<3>+{} {} XP", StringUtility.commaify(Math.round(reward.amount() * boost)),
                    StringUtility.toNormalCase(skill.name()));
            default -> createItem(reward, potion).getDisplayNameText();
        };
    }

    public static SkyBlockItem createItem(GiftReward reward, @Nullable PotionEffectType potion) {
        if (reward.kind() == GiftReward.Kind.XP_BOOST_POTION) {
            return xpBoostPotion(potion == null ? XP_BOOST_POTIONS[0] : potion, (int) reward.amount());
        }
        return new SkyBlockItem(reward.item());
    }

    public static SkyBlockItem xpBoostPotion(PotionEffectType effect, int level) {
        SkyBlockItem potion = new SkyBlockItem(ItemType.WATER_BOTTLE);
        potion.getAttributeHandler().setPotionData(new ItemAttributePotionData.PotionData(
                effect.name(), level, effect.getBaseSecondsDuration(), false, false));
        potion.getAttributeHandler().setRarity(level <= 2 ? Rarity.COMMON : Rarity.UNCOMMON);
        return potion;
    }

    private static void drop(SkyBlockPlayer player, SkyBlockItem item, Instance instance, Pos position) {
        if (player.getInstance() != instance) {
            player.addAndUpdateItem(item);
            return;
        }
        ItemDrops.dropAt(new DroppedItemEntityImpl(item, player), instance, position.add(0, 0.25, 0));
    }

    private static void awardNorthStars(SkyBlockPlayer player, Gift gift, RandomGenerator random) {
        int amount = gift.rollNorthStars(random);
        if (amount == 0) return;
        DatapointInteger stars = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.NORTH_STARS, DatapointInteger.class);
        stars.setValue(stars.getValue() + amount);
        player.sendMessage("<d>+{} North Star{}", amount, amount == 1 ? "" : "s");
    }

    private static void broadcastSanta(SkyBlockPlayer giver, SkyBlockPlayer opener, Text reward, Gift gift) {
        Text message = Text.of("<c><l>SANTA GIFT!</l> {} <e>and {} <e>found {} <e>in a {}<e>!",
                opener.getColouredName(), giver.getColouredName(), reward,
                new SkyBlockItem(gift.getItem()).getDisplayNameText());
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            player.sendMessage(message);
        }
    }

    static boolean isPresent(SkyBlockPlayer player) {
        return player.isOnline() && player.getSkyblockDataHandler() != null;
    }
}
