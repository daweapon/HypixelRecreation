package dev.daweapon.type.skyblockgeneric.foraging;

import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.skyblockgeneric.event.custom.ShardCaughtEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.TreeGiftReceivedEvent;
import dev.daweapon.type.skyblockgeneric.hotf.HotfCurrency;
import dev.daweapon.type.skyblockgeneric.hotf.HotfRewards;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.item.components.AttributeShardComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.user.pets.PetEvents;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

// Distributes Tree Gift rewards (essence, foraging XP, wood, bonus drops) to players who
// contributed enough logs to a felled foraging tree, scaled by tree size and their share of the cut.
public final class TreeGiftService {
    private TreeGiftService() {
    }

    public static void award(Instance instance, String treeType, Map<UUID, Integer> contributions, int treeLogs) {
        int sizeMultiplier = Math.clamp((int) Math.round(treeLogs / 25D), 1, 20);
        int total = contributions.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<UUID, Integer> entry : contributions.entrySet()) {
            Player rawPlayer = instance.getPlayerByUuid(entry.getKey());
            if (!(rawPlayer instanceof SkyBlockPlayer player)) continue;
            double share = total == 0 ? 0 : entry.getValue() / (double) total;
            HotfRewards.recordTree(player);
            double rewardScale = share < 0.10 ? 0 : share < 0.20 ? 1 : share < 0.33 ? 1.5 : 2;
            if (rewardScale == 0) {
                player.sendMessage("<c>You need to cut at least 10% of the tree to receive a Tree Gift!");
                continue;
            }
            awardPlayer(player, treeType, sizeMultiplier, rewardScale, share);
        }
    }

    private static void awardPlayer(SkyBlockPlayer player, String treeType, int multiplier,
                                    double rewardScale, double share) {
        List<Text> rewards = new ArrayList<>();
        List<Text> bonusMessages = new ArrayList<>();
        int scaled = Math.max(1, (int) Math.round(multiplier * rewardScale));
        double lootBonus = 1 + HotfRewards.lootBonusPercent(player) / 100D;
        int essence = (int) Math.round(switch (treeType) {
            case "MANGROVE" -> 3;
            case "HELIX" -> 4;
            default -> 2;
        } * scaled * lootBonus);
        if (HotfRewards.doubleEssence(player)) essence *= 2;
        int foragingXp = switch (treeType) {
            case "MANGROVE" -> 500;
            case "HELIX" -> 1500;
            default -> 100;
        } * scaled;
        long hotfExperience = Math.round(switch (treeType) {
            case "MANGROVE" -> 20;
            case "HELIX" -> 30;
            default -> 10;
        } * scaled * HotfService.experienceMultiplier(player));
        TreeGiftMilestones.recordGift(player, treeType);
        HypixelEventHandler.callCustomEvent(new TreeGiftReceivedEvent(player, treeType));
        dev.daweapon.type.skyblockgeneric.hunting.creature.trigger.CreatureTriggers.onTreeGift(player, treeType);
        increase(player, SkyBlockDataHandler.Data.FOREST_ESSENCE, essence);
        HotfService.addExperience(player, Math.round(PetEvents.xpGained(player, PetEvent.XpType.HOTF, null, hotfExperience)));
        player.getSkills().increase(player, SkillCategories.FORAGING, (double) foragingXp);
        HotfRewards.recordGift(player);
        rewards.add(Text.of("<a>Forest Essence <7>x<f>{}", essence));
        rewards.add(Text.of("<a>Foraging Experience <7>x<f>{}", foragingXp));
        rewards.add(Text.of("<a>HOTF Experience <7>x<f>{}", hotfExperience));
        if (treeType.equals("FIG")) {
            int tender = ThreadLocalRandom.current().nextInt(scaled + 1);
            if (LootRoll.claim(player.getUuid(), List.of(ItemType.TENDER_WOOD)) != null) tender = Math.max(1, tender);
            if (tender > 0) player.addAndUpdateItem(ItemType.TENDER_WOOD, tender);
            rewards.add(Text.of("<a>Tender Wood <7>x<f>0-{}", scaled));
        } else if (treeType.equals("MANGROVE")) {
            int vinesap = ThreadLocalRandom.current().nextInt(scaled + 1);
            if (LootRoll.claim(player.getUuid(), List.of(ItemType.VINESAP)) != null) vinesap = Math.max(1, vinesap);
            if (vinesap > 0) player.addAndUpdateItem(ItemType.VINESAP, vinesap);
            rewards.add(Text.of("<a>Vinesap <7>x<f>0-{}", scaled));
        }
        if (treeType.equals("HELIX")) {
            long whispers = HotfRewards.giftWhispers(player, HotfCurrency.DESERT, ThreadLocalRandom.current().nextLong(75L * scaled, 150L * scaled + 1));
            rewards.add(Text.of("<6>Desert Whispers <7>x<f>{}", whispers));
        } else {
            long whispers = HotfRewards.giftWhispers(player, HotfCurrency.FOREST, ThreadLocalRandom.current().nextLong(20L * scaled, 40L * scaled + 1));
            rewards.add(Text.of("<3>Forest Whispers <7>x<f>{}", whispers));
        }
        HotfService.syncSkyBlockXp(player);
        List<Bonus> bonuses = bonuses(treeType);
        int guaranteedRewards = rewards.size();
        double treeLurker = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("C24")) / 100D;
        for (Bonus bonus : bonuses) {
            double chance = bonus.chance * scaled * lootBonus;
            if (bonus.shard != null) chance *= 1 + treeLurker;
            AttributeDefinition definition = bonus.shard == null ? null : AttributeRegistry.findByShard(bonus.shard).orElse(null);
            ItemType bonusItem = definition != null ? AttributeShardComponent.itemType(definition) : bonus.item;
            if (!LootRoll.roll(player.getUuid(), bonusItem, Math.min(1, chance) * 100)) continue;
            if (bonus.shard != null) {
                if (definition == null) continue;
                player.getHuntingData().addShards(definition.id(), 1);
                HypixelEventHandler.callCustomEvent(new ShardCaughtEvent(player, definition, 1, ShardCaughtEvent.Source.TREE_GIFT, false));
                Text shardText = Text.of("<color:{}>{}", definition.rarity().itemRarity().getColor(), definition.shardName());
                rewards.add(shardText);
                bonusMessages.add(shardText.append(" <7>(<a>{}<7>)", chance(bonus.chance)));
            } else if (bonus.item != null) {
                player.addAndUpdateItem(bonus.item);
                if (bonus.item == ItemType.TREE_THE_FISH) dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers.fire(player, "find.tree_the_fish");
                Text itemText = Text.of("<color:{}>{}", bonus.item.rarity.getColor(), bonus.item.getDisplayName());
                rewards.add(itemText);
                bonusMessages.add(itemText.append(" <7>(<a>{}<7>)", chance(bonus.chance)));
            }
        }
        double signalChance = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("R7")) / 100D;
        if (LootRoll.roll(player.getUuid(), ItemType.SIGNAL_ENHANCER, signalChance * 100)) {
            player.addAndUpdateItem(ItemType.SIGNAL_ENHANCER);
            rewards.add(Text.of("<6>Signal Enhancer"));
            bonusMessages.add(Text.of("<6>Signal Enhancer <7>(<a>{}<7>)", chance(signalChance)));
        }
        if (HotfRewards.claimFreeTrial(player)) {
            rewards.add(Text.of("<2>Basic Safari Ticket"));
            bonusMessages.add(Text.of("<2>Basic Safari Ticket <7>(<a>Free Trial<7>)"));
        }
        int percent = (int) Math.round(share * 100);
        Text body = Text.of("<2><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n")
                .append("""
                        <a><l>TREE GIFT</l>
                        <f>You helped cut <a>{0}% <f>of the {1} Tree.
                        <6>+{2} rewards gained! <7>(hover)
                        """, percent, pretty(treeType), guaranteedRewards);
        if (!bonusMessages.isEmpty()) {
            body = body.append("<d><l>BONUS GIFT</l>\n")
                    .append(Text.join(Text.literal("\n"), bonusMessages))
                    .append(Text.literal("\n"));
        }
        body = body.append("<2><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        player.sendMessage(Text.of("<hover:'{0}'>{1}</hover>",
                Text.join(Text.literal("\n"), rewards), body));
    }

    private static List<Bonus> bonuses(String treeType) {
        if (treeType.equals("FIG")) return List.of(
                new Bonus(null, ItemType.STRETCHING_STICKS, .10),
                new Bonus(null, ItemType.SWEEP_BOOSTER, .005), new Bonus(null, ItemType.FORAGING_WISDOM_BOOSTER, .003),
                new Bonus("Hummingbird", null, .0015), new Bonus("Chameleon", null, .0008),
                new Bonus(null, ItemType.TREE_THE_FISH, .0005));
        if (treeType.equals("MANGROVE")) return List.of(
                new Bonus(null, ItemType.DEEP_ROOT, .04),
                new Bonus(null, ItemType.SWEEP_BOOSTER, .01), new Bonus(null, ItemType.FORAGING_WISDOM_BOOSTER, .005),
                new Bonus("Hummingbird", null, .0025), new Bonus("Chameleon", null, .0008),
                new Bonus(null, ItemType.TREE_THE_FISH, .0005));
        return List.of(new Bonus("Hummingbird", null, .003),
                new Bonus("Chameleon", null, .001), new Bonus(null, ItemType.TREE_THE_FISH, .0005));
    }

    private static void increase(SkyBlockPlayer player, SkyBlockDataHandler.Data key, long amount) {
        DatapointLong data = player.getSkyblockDataHandler().get(key, DatapointLong.class);
        data.setValue(data.getValue() + amount);
    }

    private static String pretty(String value) {
        return value.charAt(0) + value.substring(1).toLowerCase();
    }

    private static String chance(double chance) {
        return "%s%%".formatted(chance * 100D % 1 == 0 ? Integer.toString((int) (chance * 100D))
                : Double.toString(chance * 100D));
    }

    private record Bonus(String shard, ItemType item, double chance) {
    }
}
