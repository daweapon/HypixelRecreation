package dev.daweapon.type.skyblockgeneric.crimson.quest;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.BitsMultiplier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * Pays a claimed quest: the tier's Combat XP, reputation and Bits, plus the item rolled from the tier pool when
 * the board was generated, so the board can name it in advance.
 */
public final class FactionQuestRewards {
    private FactionQuestRewards() {
    }

    public static FactionQuestTier.RewardItem roll(@Nullable UUID player, FactionQuest quest, RandomGenerator random) {
        return LootRoll.pick(player, quest.tier().pool(quest.type()), FactionQuestTier.RewardItem::type, entry -> 1, random);
    }

    public static void grant(SkyBlockPlayer player, CrimsonFaction faction, FactionQuest quest,
                             @Nullable FactionQuestTier.RewardItem rolled) {
        FactionQuestTier tier = quest.tier();
        FactionQuestTier.RewardItem item = rolled != null ? rolled : roll(player.getUuid(), quest, ThreadLocalRandom.current());
        int bits = (int) Math.round(tier.getBits() * BitsMultiplier.of(player));

        player.sendMessage("<6><l>QUEST REWARDS! " + tier.coloured() + " <f>" + quest.tabName());
        player.getSkills().increase(player, SkillCategories.COMBAT, (double) tier.getCombatXP());
        player.addBits(bits);
        ItemType type = item.type();
        if (type != null) player.addAndUpdateItem(type, item.amount());

        for (String line : rewardLines(tier, faction, item, bits)) player.sendMessage("  " + line);
        CrimsonReputationService.add(player, faction, tier.getReputation(), "Faction Quest");
    }

    public static List<String> previewLines(FactionQuestTier tier, CrimsonFaction faction,
                                            @Nullable FactionQuestTier.RewardItem item) {
        List<String> lines = new ArrayList<>();
        lines.add("<8>+<3>" + StringUtility.commaify(tier.getCombatXP()) + " <7>Combat Experience");
        lines.add("<8>+<a>" + tier.getReputation() + " " + faction.coloured() + " Reputation");
        lines.add("<8>+<b>" + tier.getBits() + " Bits");
        if (item != null) lines.add("<8>+<f>" + item.amount() + "x " + item.displayName());
        return lines;
    }

    private static List<String> rewardLines(FactionQuestTier tier, CrimsonFaction faction, FactionQuestTier.RewardItem item, int bits) {
        List<String> lines = new ArrayList<>();
        lines.add("<8>+<3>" + StringUtility.commaify(tier.getCombatXP()) + " <7>Combat Experience");
        lines.add("<8>+<b>" + bits + " Bits");
        lines.add("<8>+<f>" + item.amount() + "x " + item.displayName());
        return lines;
    }
}
