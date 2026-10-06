package net.swofty.type.skyblockgeneric.elections.jerrypocalypse;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** What each Jerry Box can hold, with the wiki's weights. */
public final class JerryBoxLoot {

    private record Reward(int weight, long coins, SkillCategories skill, long skillXp, ItemType item) {
        static Reward coins(int weight, long amount) {
            return new Reward(weight, amount, null, 0, null);
        }

        static Reward xp(int weight, SkillCategories skill, long amount) {
            return new Reward(weight, 0, skill, amount, null);
        }

        static Reward item(int weight, ItemType item) {
            return new Reward(weight, 0, null, 0, item);
        }
    }

    private JerryBoxLoot() {
    }

    private static List<Reward> table(HiddenJerryType type) {
        List<Reward> rewards = new ArrayList<>();
        if (type == HiddenJerryType.GOLDEN) {
            rewards.add(Reward.coins(100, 500_000));
            rewards.add(Reward.xp(2, SkillCategories.FARMING, 100_000));
            rewards.add(Reward.xp(2, SkillCategories.FORAGING, 100_000));
            rewards.add(Reward.xp(2, SkillCategories.MINING, 100_000));
            rewards.add(Reward.coins(10, 1_000_000));
            rewards.add(Reward.item(3, ItemType.JERRY_STAFF));
            rewards.add(Reward.item(2, ItemType.JERRY_STONE));
            rewards.add(Reward.coins(1, 10_000_000));
            rewards.add(Reward.item(1, ItemType.JERRY_RUNE));
            rewards.add(Reward.item(1, ItemType.PET_ITEM_TOY_JERRY));
            return rewards;
        }

        long coins = switch (type) {
            case BLUE -> 50_000;
            case PURPLE -> 150_000;
            default -> 20_000;
        };
        long xp = switch (type) {
            case BLUE -> 5_000;
            case PURPLE -> 20_000;
            default -> 2_500;
        };
        rewards.add(Reward.coins(100, coins));
        rewards.add(Reward.xp(30, SkillCategories.FARMING, xp));
        rewards.add(Reward.xp(30, SkillCategories.FORAGING, xp));
        rewards.add(Reward.xp(30, SkillCategories.MINING, xp));
        rewards.add(Reward.item(15, ItemType.JERRY_CANDY));
        rewards.add(Reward.item(15, ItemType.JERRY_TALISMAN_GREEN));
        rewards.add(Reward.item(3, ItemType.JERRY_STAFF));
        rewards.add(Reward.item(2, ItemType.JERRY_STONE));
        rewards.add(Reward.item(1, ItemType.JERRY_RUNE));
        rewards.add(Reward.item(1, ItemType.PET_ITEM_TOY_JERRY));
        if (type == HiddenJerryType.PURPLE) rewards.add(Reward.coins(5, 300_000));
        return rewards;
    }

    public static void open(SkyBlockPlayer player, HiddenJerryType type) {
        List<Reward> table = table(type);
        int roll = ThreadLocalRandom.current().nextInt(table.stream().mapToInt(Reward::weight).sum());
        Reward reward = table.getLast();
        for (Reward candidate : table) {
            roll -= candidate.weight();
            if (roll < 0) {
                reward = candidate;
                break;
            }
        }

        String header = "<a><l>JERRY BOX! " + type.getColor() + type.getName() + " Jerry Box <7>contained ";
        if (reward.item() != null) {
            SkyBlockItem item = new SkyBlockItem(reward.item());
            player.addAndUpdateItem(item);
            player.sendMessage(header + "{}<7>!", item.getDisplayName());
        } else if (reward.skill() != null) {
            player.getSkills().increase(player, reward.skill(), (double) reward.skillXp());
            player.sendMessage(header + "<3>" + StringUtility.commaify(reward.skillXp()) + " "
                    + StringUtility.toNormalCase(reward.skill().name()) + " XP<7>!");
        } else {
            player.addCoins(reward.coins());
            player.sendMessage(header + "<6>" + StringUtility.commaify(reward.coins()) + " coins<7>!");
        }
    }
}
