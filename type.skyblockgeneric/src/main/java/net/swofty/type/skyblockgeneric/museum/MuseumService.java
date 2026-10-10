package net.swofty.type.skyblockgeneric.museum;

import net.swofty.type.skyblockgeneric.achievement.SkyBlockAchievementSync;
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

    public static Optional<MuseumEntry> entryFor(DatapointMuseum.MuseumData data, SkyBlockItem item) {
        return ownerOf(catalogue(), data, item);
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
        List<MuseumEntry> candidates = catalogue().entriesFor(clickedType);
        if (candidates.isEmpty()) {
            player.sendMessage("<c>This item can't be donated to the Museum!");
            return;
        }

        DatapointMuseum.MuseumData data = player.getMuseumData();
        adopt(catalogue(), data);
        MuseumEntry entry = chooseEntry(player, data, candidates, clickedItem, inventorySlot);
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
            bindOnDonation(data, entry, item);
            player.getInventory().setItemStack(slots.get(index), ItemStack.AIR);
            data.add(entry, item);
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
        adopt(catalogue(), data);
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
            if (data.isStaffGranted(item)) unbind(item);
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
        adopt(catalogue(), data);
        for (SkyBlockItem item : data.getAllItems()) {
            entryFor(data, item).ifPresent(entry -> credit(data, entry));
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
        SkyBlockAchievementSync.syncMuseum(player);
    }

    public static void maxOut(SkyBlockPlayer player) {
        DatapointMuseum.MuseumData data = player.getMuseumData();
        catalogue().progressEntries().forEach(entry -> data.credit(entry.id()));
        data.setClaimedMilestone(MuseumMilestones.MAX_MILESTONE);
        grantCopies(catalogue(), data);
        if (data.isHasBoughtAppraisalService()) {
            for (SkyBlockItem item : data.getCurrentlyInMuseum()) {
                data.getCalculatedPrices().computeIfAbsent(trackedId(item), _ -> new ItemPriceCalculator(item).calculateCleanPrice());
            }
        }
        player.setMuseumData(data);
        sync(player);
    }

    public static int grantCopies(MuseumCatalogue catalogue, DatapointMuseum.MuseumData data) {
        adopt(catalogue, data);
        int granted = 0;
        for (MuseumEntry entry : catalogue.all()) {
            if (!entry.isAvailable() || !data.itemsFor(entry, true).isEmpty() || !data.itemsFor(entry, false).isEmpty()) {
                continue;
            }
            for (ItemType type : entry.requiredItemTypes()) {
                data.grant(entry, staffCopy(type));
                granted++;
            }
            credit(catalogue, data, entry);
        }
        return granted;
    }

    static void bindOnDonation(DatapointMuseum.MuseumData data, MuseumEntry entry, SkyBlockItem item) {
        if (!entry.category().isSpecial() && !data.isStaffGranted(item)) item.getAttributeHandler().setSoulBound(true);
    }

    static void adopt(MuseumCatalogue catalogue, DatapointMuseum.MuseumData data) {
        for (SkyBlockItem item : data.getAllItems()) {
            if (data.ownerId(item) != null) continue;
            catalogue.entryFor(item.getAttributeHandler().getPotentialType())
                    .ifPresent(entry -> data.assignOwner(item, entry.id()));
        }
    }

    private static Optional<MuseumEntry> ownerOf(MuseumCatalogue catalogue, DatapointMuseum.MuseumData data, SkyBlockItem item) {
        String owner = data.ownerId(item);
        if (owner != null) return catalogue.entry(owner);
        return catalogue.entryFor(item.getAttributeHandler().getPotentialType());
    }

    private static MuseumEntry chooseEntry(SkyBlockPlayer player, DatapointMuseum.MuseumData data,
                                           List<MuseumEntry> candidates, SkyBlockItem clicked, int clickedSlot) {
        String owner = data.ownerId(clicked);
        Optional<MuseumEntry> returning = candidates.stream().filter(entry -> entry.id().equals(owner)).findFirst();
        if (returning.isPresent()) return returning.get();

        List<MuseumEntry> open = candidates.stream()
                .filter(MuseumEntry::isAvailable)
                .filter(entry -> data.itemsFor(entry, true).isEmpty())
                .toList();
        if (open.isEmpty()) {
            return candidates.stream().filter(MuseumEntry::isAvailable).findFirst().orElse(candidates.getFirst());
        }
        ItemType clickedType = clicked.getAttributeHandler().getPotentialType();
        return open.stream()
                .filter(entry -> collectSlots(player, entry, clickedSlot, clickedType).size() >= entry.requiredItemIds().size())
                .findFirst()
                .orElse(open.getFirst());
    }

    private static SkyBlockItem staffCopy(ItemType type) {
        SkyBlockItem item = new SkyBlockItem(type);
        item.getAttribute("unique-tracked-id").setValue(UUID.randomUUID().toString());
        return item;
    }

    private static void unbind(SkyBlockItem item) {
        item.getAttribute("soul_bound").setValue(null);
    }

    private static void credit(DatapointMuseum.MuseumData data, MuseumEntry entry) {
        credit(catalogue(), data, entry);
    }

    private static void credit(MuseumCatalogue catalogue, DatapointMuseum.MuseumData data, MuseumEntry entry) {
        if (data.isCredited(entry.id()) && catalogue.childrenOf(entry).stream().allMatch(child -> data.isCredited(child.id()))) {
            return;
        }
        data.credit(entry.id());
        catalogue.childrenOf(entry).forEach(child -> credit(catalogue, data, child));
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
