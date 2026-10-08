package net.swofty.type.skyblockgeneric.crimson.quest;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.BitsMultiplier;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Pays a claimed quest: the tier's Combat XP, reputation and Bits, plus one item picked from the tier pool. */
public final class FactionQuestRewards {
    private FactionQuestRewards() {
    }

    public static void grant(SkyBlockPlayer player, CrimsonFaction faction, FactionQuest quest) {
        FactionQuestTier tier = quest.tier();
        FactionQuestTier.RewardItem item = LootRoll.pick(player.getUuid(), tier.pool(quest.type()),
                FactionQuestTier.RewardItem::type, entry -> 1, ThreadLocalRandom.current());
        int bits = (int) Math.round(tier.getBits() * BitsMultiplier.of(player));

        player.sendMessage("<6><l>QUEST REWARDS! " + tier.coloured() + " <f>" + quest.tabName());
        player.getSkills().increase(player, SkillCategories.COMBAT, (double) tier.getCombatXP());
        player.addBits(bits);
        ItemType type = item.type();
        if (type != null) player.addAndUpdateItem(type, item.amount());

        for (String line : rewardLines(tier, faction, item, bits)) player.sendMessage("  " + line);
        CrimsonReputationService.add(player, faction, tier.getReputation(), "Faction Quest");
    }

    public static List<String> previewLines(FactionQuestTier tier, CrimsonFaction faction, FactionQuestType type) {
        List<String> lines = new ArrayList<>();
        lines.add("<8>+<3>" + StringUtility.commaify(tier.getCombatXP()) + " <7>Combat Experience");
        lines.add("<8>+<a>" + tier.getReputation() + " " + faction.coloured() + " Reputation");
        lines.add("<8>+<b>" + tier.getBits() + " Bits");
        lines.add("<8>+<7>One of " + tier.pool(type).size() + " random " + (type == FactionQuestType.FISHING ? "fishing " : "") + "items");
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
