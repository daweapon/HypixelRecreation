package net.swofty.type.skyblockgeneric.museum;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.ItemPriceCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MuseumService {
    private static final int INVENTORY_SIZE = 36;

    private MuseumService() {}

    public static MuseumCatalogue catalogue() {
        return MuseumCatalogue.get();
    }

    public static int museumXp(DatapointMuseum.MuseumData data) {
        return catalogue().progressEntries().stream()
                .filter(entry -> data.isCredited(entry.id()))
                .mapToInt(MuseumEntry::xp)
                .sum();
    }

    public static int milestone(DatapointMuseum.MuseumData data) {
        return MuseumMilestones.milestoneFor(museumXp(data));
    }

    public static long donatedCount(DatapointMuseum.MuseumData data, MuseumCategory category) {
        return catalogue().entries(category).stream().filter(entry -> data.isCredited(entry.id())).count();
    }

    public static long donatedCount(DatapointMuseum.MuseumData data) {
        return catalogue().progressEntries().stream().filter(entry -> data.isCredited(entry.id())).count();
    }

    public static double museumValue(DatapointMuseum.MuseumData data) {
        return data.getCurrentlyInMuseum().stream()
                .map(item -> data.getCalculatedPrices().get(trackedId(item)))
                .filter(price -> price != null)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public static Optional<MuseumEntry> entryFor(SkyBlockItem item) {
        return catalogue().entryFor(item.getAttributeHandler().getPotentialType());
    }

    public static MuseumState stateOf(DatapointMuseum.MuseumData data, MuseumEntry entry) {
        if (!data.itemsFor(entry, true).isEmpty()) return MuseumState.IN_MUSEUM;
        if (!data.itemsFor(entry, false).isEmpty()) return MuseumState.RETRIEVED;
        if (data.isCredited(entry.id())) return MuseumState.CREDITED;
        return entry.isAvailable() ? MuseumState.MISSING : MuseumState.UNAVAILABLE;
    }

    public static void donate(SkyBlockPlayer player, int inventorySlot) {
        ItemStack clicked = player.getInventory().getItemStack(inventorySlot);
        if (clicked.material() == Material.AIR) return;
        SkyBlockItem clickedItem = new SkyBlockItem(clicked);
        ItemType clickedType = clickedItem.getAttributeHandler().getPotentialType();
        Optional<MuseumEntry> found = catalogue().entryFor(clickedType);
        if (found.isEmpty()) {
            player.sendMessage("<c>This item can't be donated to the Museum!");
            return;
        }

        MuseumEntry entry = found.get();
        DatapointMuseum.MuseumData data = player.getMuseumData();
        if (!entry.isAvailable()) {
            player.sendMessage("<c>The rest of the {} isn't in this server yet!", entry.name());
            return;
        }
        if (!data.itemsFor(entry, true).isEmpty()) {
            player.sendMessage("<c>You already have the {} in your Museum!", entry.name());
            return;
        }
        if (entry.category().isSpecial() && !data.isCredited(entry.id())
                && donatedCount(data, MuseumCategory.SPECIAL) >= MuseumCategory.SPECIAL_ITEM_LIMIT) {
            player.sendMessage("<c>You can only donate {} Special Items!", MuseumCategory.SPECIAL_ITEM_LIMIT);
            return;
        }

        List<Integer> slots = collectSlots(player, entry, inventorySlot, clickedType);
        if (slots.size() < entry.requiredItemIds().size()) {
            player.sendMessage("<c>You are missing some of the items from the {} in your inventory! ({}/{})",
                    entry.name(), slots.size(), entry.requiredItemIds().size());
            return;
        }

        List<SkyBlockItem> retrieved = data.itemsFor(entry, false);
        List<SkyBlockItem> donating = slots.stream()
                .map(slot -> new SkyBlockItem(player.getInventory().getItemStack(slot)))
                .toList();
        if (!retrieved.isEmpty() && !sameItems(retrieved, donating)) {
            player.sendMessage("<c>You can only return the {} that you retrieved from your Museum!", entry.name());
            return;
        }

        for (int index = 0; index < slots.size(); index++) {
            SkyBlockItem item = donating.get(index);
            if (item.getAttributeHandler().getUniqueTrackedID() == null) {
                item.getAttributeHandler().setUniqueTrackedID(UUID.randomUUID().toString(), player);
            }
            if (!entry.category().isSpecial()) item.getAttributeHandler().setSoulBound(true);
            player.getInventory().setItemStack(slots.get(index), ItemStack.AIR);
            data.add(item);
            if (data.isHasBoughtAppraisalService()) {
                data.getCalculatedPrices().put(trackedId(item), new ItemPriceCalculator(item).calculateCleanPrice());
            }
        }

        credit(data, entry);
        player.setMuseumData(data);
        player.sendMessage("<a>You donated your <f>{}<a> to the Museum!", entry.name());
        sync(player);
    }

    public static boolean retrieve(SkyBlockPlayer player, MuseumEntry entry) {
        DatapointMuseum.MuseumData data = player.getMuseumData();
        List<SkyBlockItem> items = data.itemsFor(entry, true);
        if (items.isEmpty()) return false;
        if (freeSlots(player) < items.size()) {
            player.sendMessage("<c>You need at least {} empty inventory slot{} to retrieve this!",
                    items.size(), items.size() == 1 ? "" : "s");
            return false;
        }

        for (SkyBlockItem item : items) {
            data.moveToRetrieved(item);
            data.getCalculatedPrices().remove(trackedId(item));
            player.addAndUpdateItem(item);
        }
        player.setMuseumData(data);
        player.sendMessage("<a>You retrieved your <f>{}<a> from the <3>Museum<a>!", entry.name());
        player.sendMessage("<7>It still counts towards your <3>Museum <7>progress, but not towards your <6>total item value<7>.");
        return true;
    }

    public static boolean buyAppraisal(SkyBlockPlayer player) {
        DatapointMuseum.MuseumData data = player.getMuseumData();
        if (data.isHasBoughtAppraisalService()) return false;
        if (milestone(data) < MuseumMilestones.APPRAISAL_MILESTONE) {
            player.sendMessage("<c>You need Museum Milestone {} to unlock the Appraisal Service!",
                    MuseumMilestones.APPRAISAL_MILESTONE);
            return false;
        }
        if (player.getCoins() < MuseumMilestones.APPRAISAL_COST) {
            player.sendMessage("<c>You don't have enough Coins!");
            return false;
        }

        player.removeCoins(MuseumMilestones.APPRAISAL_COST);
        data.setHasBoughtAppraisalService(true);
        for (SkyBlockItem item : data.getCurrentlyInMuseum()) {
            data.getCalculatedPrices().put(trackedId(item), new ItemPriceCalculator(item).calculateCleanPrice());
        }
        player.setMuseumData(data);
        player.sendMessage("<a>You unlocked the <6>Museum Appraisal Service<a>!");
        return true;
    }

    public static void sync(SkyBlockPlayer player) {
        DatapointMuseum.MuseumData data = player.getMuseumData();
        for (SkyBlockItem item : data.getAllItems()) {
            entryFor(item).ifPresent(entry -> credit(data, entry));
        }

        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        for (MuseumEntry entry : catalogue().progressEntries()) {
            if (data.isCredited(entry.id())) grant(experience, SkyBlockLevelCause.getMuseumCause(entry));
        }

        int reached = milestone(data);
        for (int milestone = 1; milestone <= reached; milestone++) {
            grant(experience, SkyBlockLevelCause.getMuseumMilestoneCause(milestone));
            if (milestone > data.getClaimedMilestone()) announce(player, milestone);
        }
        data.setClaimedMilestone(Math.max(data.getClaimedMilestone(), reached));

        player.setMuseumData(data);
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SKYBLOCK_EXPERIENCE,
                DatapointSkyBlockExperience.class).setValue(experience);
        writeBankMilestone(player, reached);
    }

    public static void maxOut(SkyBlockPlayer player) {
        DatapointMuseum.MuseumData data = player.getMuseumData();
        catalogue().progressEntries().forEach(entry -> data.credit(entry.id()));
        data.setClaimedMilestone(MuseumMilestones.MAX_MILESTONE);
        player.setMuseumData(data);
        sync(player);
    }

    private static void credit(DatapointMuseum.MuseumData data, MuseumEntry entry) {
        if (data.isCredited(entry.id()) && catalogue().childrenOf(entry).stream().allMatch(child -> data.isCredited(child.id()))) {
            return;
        }
        data.credit(entry.id());
        catalogue().childrenOf(entry).forEach(child -> credit(data, child));
    }

    private static void grant(DatapointSkyBlockExperience.PlayerSkyBlockExperience experience, SkyBlockLevelCauseAbstr cause) {
        if (cause != null && !experience.hasExperienceFor(cause)) experience.addExperience(cause);
    }

    private static void announce(SkyBlockPlayer player, int milestone) {
        player.sendMessage("<a>You have reached <e>Museum Milestone {}<a>!", milestone);
        for (MuseumMilestoneReward reward : MuseumMilestoneReward.forMilestone(milestone)) {
            if (!reward.isAvailable()) continue;
            player.sendMessage("<8>+ " + reward.getDisplay());
            if (reward.itemType() != null) player.addAndUpdateItem(reward.itemType());
        }
        player.sendMessage("<8>+<b>1% Bits Multiplier");
        player.sendMessage("<8>+<6>2% Bank Interest");
    }

    private static void writeBankMilestone(SkyBlockPlayer player, int milestone) {
        DatapointBankData datapoint = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class);
        synchronized (datapoint) {
            DatapointBankData.BankData bank = datapoint.getValue();
            if (bank.getMuseumMilestone() == milestone) return;
            bank.setMuseumMilestone(milestone);
            datapoint.setValue(bank);
        }
    }

    private static List<Integer> collectSlots(SkyBlockPlayer player, MuseumEntry entry, int clickedSlot, ItemType clickedType) {
        List<Integer> slots = new ArrayList<>();
        if (!entry.isSet()) {
            slots.add(clickedSlot);
            return slots;
        }
        for (ItemType piece : entry.requiredItemTypes()) {
            if (piece == clickedType) {
                slots.add(clickedSlot);
                continue;
            }
            for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
                ItemStack stack = player.getInventory().getItemStack(slot);
                if (stack.material() == Material.AIR || slots.contains(slot)) continue;
                if (new SkyBlockItem(stack).getAttributeHandler().getPotentialType() == piece) {
                    slots.add(slot);
                    break;
                }
            }
        }
        return slots;
    }

    private static boolean sameItems(List<SkyBlockItem> retrieved, List<SkyBlockItem> donating) {
        List<UUID> expected = retrieved.stream().map(MuseumService::trackedId).toList();
        return donating.stream().allMatch(item -> item.getAttributeHandler().getUniqueTrackedID() != null
                && expected.contains(trackedId(item)));
    }

    private static int freeSlots(SkyBlockPlayer player) {
        int free = 0;
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            if (player.getInventory().getItemStack(slot).material() == Material.AIR) free++;
        }
        return free;
    }

    static UUID trackedId(SkyBlockItem item) {
        return UUID.fromString(item.getAttributeHandler().getUniqueTrackedID());
    }
}
