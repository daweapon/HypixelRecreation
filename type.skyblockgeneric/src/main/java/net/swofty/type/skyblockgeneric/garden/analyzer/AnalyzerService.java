package net.swofty.type.skyblockgeneric.garden.analyzer;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.chip.ChipService;
import net.swofty.type.skyblockgeneric.garden.chip.GardenChip;
import net.swofty.type.skyblockgeneric.garden.greenhouse.GreenhouseData;
import net.swofty.type.skyblockgeneric.garden.greenhouse.MutationCatalogue;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AnalyzerService {
    public static final int[] MILESTONE_TARGETS = {1, 10, 15, 20, 30, 40};
    public static final int FORTUNE_PER_MILESTONE = 5;
    public static final int SKYBLOCK_XP_PER_MILESTONE = 15;
    private static final String[][] MILESTONE_ITEMS = {
            {"BIOANALYSIS_TALISMAN", "SMALL_MUTATIONS_SACK"}, {"OVERGROWN_GRASS"}, {"MEDIUM_MUTATIONS_SACK"},
            {"BIOANALYSIS_RING"}, {"LARGE_MUTATIONS_SACK", "ESTATE_GREENHOUSE_SKIN"}, {"BIOANALYSIS_ARTIFACT"}};

    private AnalyzerService() {
    }

    public static @Nullable MutationCatalogue.Mutation heldMutation(SkyBlockPlayer player) {
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        if (held.isNA()) return null;
        return MutationCatalogue.get().ofItem(held.getAttributeHandler().getPotentialType());
    }

    public static boolean analyzed(SkyBlockPlayer player, MutationCatalogue.Mutation mutation) {
        return Gardens.profile(player).greenhouse().getAnalyzed().contains(mutation.id());
    }

    public static int copperFor(SkyBlockPlayer player, MutationCatalogue.Mutation mutation, boolean first) {
        double synthesis = 1 + ChipService.bonus(player, GardenChip.SYNTHESIS) / 100.0;
        return (int) Math.round((mutation.copperRepeat() + (first ? mutation.copperFirst() : 0)) * synthesis);
    }

    public static int owned(SkyBlockPlayer player, ItemType item) {
        return player.getAmountInInventory(item) + player.getSackItems().getAmount(item);
    }

    public static boolean canAfford(SkyBlockPlayer player, long coins) {
        return player.getCoins() >= coins;
    }

    public static boolean complete(SkyBlockPlayer player, MutationCatalogue.Mutation mutation) {
        ItemType item = mutation.item();
        if (item == null || owned(player, item) < 1 || !canAfford(player, mutation.coinsCost())) return false;
        GardenProfile profile = Gardens.profile(player);
        boolean first = profile.greenhouse().getAnalyzed().add(mutation.id());
        player.removeItemFromPlayer(item, 1);
        player.addCoins(-mutation.coinsCost());
        int copper = copperFor(player, mutation, first);
        profile.addCopper(copper);
        Gardens.save(player, profile);
        player.sendMessage(Text.of("<d><m>                              "));
        player.sendMessage(Text.of("{} <6><l>ANALYSIS COMPLETE", "<a>" + mutation.name().toUpperCase()));
        player.sendMessage(Text.of("<7>REWARDS"));
        player.sendMessage(Text.of(" <8>+<c>{} Copper{}", StringUtility.commaify(copper), first ? " <e>(First Time Bonus)" : ""));
        player.sendMessage(Text.of("<d><m>                              "));
        return true;
    }

    public record MassEntry(MutationCatalogue.Mutation mutation, int count) {
    }

    public static List<MassEntry> massCandidates(SkyBlockPlayer player) {
        List<MassEntry> entries = new ArrayList<>();
        Map<String, Boolean> seen = new LinkedHashMap<>();
        for (MutationCatalogue.Mutation mutation : MutationCatalogue.get().all()) {
            ItemType item = mutation.item();
            if (item == null || !analyzed(player, mutation) || seen.put(mutation.id(), true) != null) continue;
            int count = owned(player, item);
            if (count > 0) entries.add(new MassEntry(mutation, count));
        }
        return entries;
    }

    public static long massCost(List<MassEntry> entries) {
        long total = 0;
        for (MassEntry entry : entries) total += (long) entry.mutation().coinsCost() * entry.count();
        return total;
    }

    public static int massCopper(SkyBlockPlayer player, List<MassEntry> entries) {
        int total = 0;
        for (MassEntry entry : entries) total += copperFor(player, entry.mutation(), false) * entry.count();
        return total;
    }

    public static boolean massAnalyze(SkyBlockPlayer player) {
        List<MassEntry> entries = massCandidates(player);
        if (entries.isEmpty()) return false;
        long cost = massCost(entries);
        if (!canAfford(player, cost)) return false;
        int copper = massCopper(player, entries);
        for (MassEntry entry : entries) player.removeItemFromPlayer(entry.mutation().item(), entry.count());
        player.addCoins(-cost);
        GardenProfile profile = Gardens.profile(player);
        profile.addCopper(copper);
        Gardens.save(player, profile);
        player.sendMessage(Text.of("<a>Mass analysis complete! You received <c>{} Copper<a>.", StringUtility.commaify(copper)));
        return true;
    }

    public static int milestoneProgress(GreenhouseData data) {
        return data.getAnalyzed().size();
    }

    public static boolean milestoneClaimable(GreenhouseData data) {
        return data.getDnaClaimed() < MILESTONE_TARGETS.length && milestoneProgress(data) >= MILESTONE_TARGETS[data.getDnaClaimed()];
    }

    public static boolean claimMilestone(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData data = profile.greenhouse();
        if (!milestoneClaimable(data)) return false;
        int tier = data.getDnaClaimed();
        data.setDnaClaimed(tier + 1);
        List<String> lines = new ArrayList<>();
        for (String id : MILESTONE_ITEMS[tier]) {
            ItemType type = ItemType.get(id);
            if (type != null) {
                player.addAndUpdateItem(new SkyBlockItem(type, 1));
                lines.add(new SkyBlockItem(type).getDisplayName());
            }
        }
        Gardens.save(player, profile);
        GardenService.syncExperience(player);
        player.sendMessage(Text.of("<a>You claimed DNA Analysis Milestone {}<a>! <8>+{} Farming Fortune, +{} SkyBlock XP{}",
                StringUtility.getAsRomanNumeral(tier + 1), FORTUNE_PER_MILESTONE, SKYBLOCK_XP_PER_MILESTONE,
                lines.isEmpty() ? "" : ", " + String.join(", ", lines)));
        return true;
    }

    public static int fortune(GreenhouseData data) {
        return data.getDnaClaimed() * FORTUNE_PER_MILESTONE;
    }

    public static String[] milestoneItems(int tier) {
        return MILESTONE_ITEMS[tier];
    }
}
