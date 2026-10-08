package net.swofty.type.crimsonisle.quest.gui;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.faction.ReputationTier;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import net.swofty.type.skyblockgeneric.crimson.quest.FactionQuest;
import net.swofty.type.skyblockgeneric.crimson.quest.FactionQuestBoard;
import net.swofty.type.skyblockgeneric.crimson.quest.FactionQuestRewards;
import net.swofty.type.skyblockgeneric.crimson.quest.FactionQuestService;
import net.swofty.type.skyblockgeneric.crimson.quest.FactionQuestType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Town Board icons. Quests are map icons and a claimed one reads COMPLETE (wiki / SkyHanni); the rest of the
 * wording, the reputation header and the daily checklists are estimates, since Hypixel has no published layout.
 */
final class FactionQuestItems {
    private FactionQuestItems() {
    }

    static ItemStack.Builder quest(SkyBlockPlayer player, FactionQuestBoard.Slot slot, boolean onBoard) {
        FactionQuest quest = slot.quest();
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        StringBuilder lore = new StringBuilder(quest.tier().getColour() + quest.tabName() + "\n")
                .append("<8>").append(quest.type().getCategoryName()).append(" Quest\n\n")
                .append(quest.objective()).append("\n");
        String progress = FactionQuestService.progressText(slot);
        if (quest.type() == FactionQuestType.FETCH && !slot.isCompleted()) {
            progress = "<7>You have: <e>" + StringUtility.commaify(FactionQuestService.fetchAvailable(player, quest))
                    + "<6>/<e>" + quest.amount();
        }
        if (!progress.isEmpty() && !slot.isClaimed()) lore.append(progress).append("\n");
        lore.append("\n<7>Rewards:\n");
        if (faction != null) FactionQuestRewards.previewLines(quest.tier(), faction, slot.getReward()).forEach(line -> lore.append(" ").append(line).append("\n"));
        lore.append("\n").append(quest.tier().coloured()).append(" ").append(quest.tier().getRarityName().toUpperCase()).append(" QUEST\n\n");
        lore.append(status(player, slot, onBoard));
        return ItemStacks.item(slot.isClaimed() ? Material.MAP : Material.FILLED_MAP, lore.toString());
    }

    private static String status(SkyBlockPlayer player, FactionQuestBoard.Slot slot, boolean onBoard) {
        FactionQuest quest = slot.quest();
        if (slot.isClaimed()) return "<a><l>COMPLETE";
        if (slot.isCompleted()) return "<e>Click to claim your rewards!";
        if (quest.type() == FactionQuestType.FETCH) {
            return FactionQuestService.fetchAvailable(player, quest) >= quest.amount()
                    ? "<e>Click to hand in the items and claim!" : "<c>You don't have enough items yet!";
        }
        return onBoard ? "<e>Click to view details!" : "<c>Not completed yet!";
    }

    static ItemStack.Builder header(SkyBlockPlayer player) {
        CrimsonFaction faction = CrimsonReputationService.faction(player);
        StringBuilder lore = new StringBuilder(faction == null ? "<7>No Faction" : faction.coloured() + " Faction");
        lore.append("\n<7>Complete requests to earn <a>Reputation<7>.\n<7>Quests get harder as your\n<7>reputation grows.\n\n");
        for (CrimsonFaction each : CrimsonFaction.values()) {
            int reputation = CrimsonReputationService.get(player, each);
            ReputationTier tier = ReputationTier.of(reputation);
            ReputationTier next = tier.next();
            lore.append(each.coloured()).append(" Reputation: <e>").append(StringUtility.commaify(reputation))
                    .append(next == null ? "" : "<8>/" + StringUtility.commaify(next.getRequiredReputation()))
                    .append("\n <7>Title: ").append(tier.coloured()).append("\n");
        }
        lore.append("\n<7>New quests in: <e>").append(FactionQuestService.describeReset());
        return ItemStacks.item(faction == null ? Material.WHITE_BANNER : faction.getBannerMaterial(), lore.toString());
    }

    static ItemStack.Builder dailyMinibosses(SkyBlockPlayer player) {
        StringBuilder lore = new StringBuilder("<c>Daily Minibosses\n<7>Each miniboss gives <a>+" + CrimsonMiniboss.DAILY_REPUTATION
                + " Reputation\n<7>the first time you kill it each day.\n\n");
        int done = 0;
        for (CrimsonMiniboss miniboss : CrimsonMiniboss.values()) {
            boolean killed = FactionQuestService.hasDailyMiniboss(player, miniboss);
            if (killed) done++;
            lore.append(killed ? "<a>✔ " : "<c>✖ ").append("<f>").append(miniboss.getDisplayName()).append("\n");
        }
        lore.append("\n<7>Progress: <e>").append(done).append("<6>/<e>").append(CrimsonMiniboss.values().length);
        return ItemStacks.item(Material.WITHER_SKELETON_SKULL, lore.toString());
    }

    static ItemStack.Builder dailyKuudra(SkyBlockPlayer player) {
        StringBuilder lore = new StringBuilder("<c>Daily Kuudra\n<7>Each Kuudra tier gives Reputation\n<7>the first time you defeat it each day.\n\n");
        for (KuudraTier tier : KuudraTier.values()) {
            boolean done = FactionQuestService.hasDailyKuudra(player, tier);
            lore.append(done ? "<a>✔ " : "<c>✖ ").append("<f>").append(tier.getDisplayName()).append(" <8>(<a>+")
                    .append(FactionQuestService.dailyKuudraReputation(tier)).append("<8>)\n");
        }
        lore.append("\n<7>Kuudra quests appear on the board\n<7>once you can fight Kuudra.");
        KuudraAvailability.lore().forEach(line -> lore.append("\n").append(line));
        return ItemStacks.item(Material.MAGMA_CREAM, lore.toString());
    }
}
