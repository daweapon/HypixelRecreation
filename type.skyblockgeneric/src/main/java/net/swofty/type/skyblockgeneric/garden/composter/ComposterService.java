package net.swofty.type.skyblockgeneric.garden.composter;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenProgress;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.quest.GardenQuests;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import net.swofty.type.skyblockgeneric.mission.missions.garden.*;

import java.util.Random;

public final class ComposterService {
    private static final Random RANDOM = new Random();

    public enum UpgradeResult {
        UPGRADED, MAXED, LEVEL_TOO_LOW, MISSING_COPPER, MISSING_ITEMS
    }

    private ComposterService() {
    }

    public static ComposterState state(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        ComposterState state = profile.composter();
        ComposterEngine.initialise(state, ComposterConfig.get(), System.currentTimeMillis());
        return state;
    }

    public static void tick(SkyBlockPlayer player) {
        ComposterState state = state(player);
        int produced = ComposterEngine.advance(state, ComposterConfig.get(), System.currentTimeMillis(),
                chance -> LootRoll.roll(player.getUuid(), ItemType.COMPOST, chance, RANDOM));
        if (produced > 0) {
            Gardens.save(player, Gardens.profile(player));
        }
    }

    public static boolean tutorialLocked(SkyBlockPlayer player) {
        var data = player.getMissionData();
        boolean reached = data.isCurrentlyActive(MissionRefuelComposter.class) || data.hasCompleted(MissionRefuelComposter.class)
                || data.isCurrentlyActive(MissionTakeCompost.class) || data.hasCompleted(MissionTakeCompost.class)
                || data.hasCompleted(MissionPurchaseFuel.class);
        boolean tutorialStarted = data.isCurrentlyActive(MissionTalkToSam.class) || data.isCurrentlyActive(MissionBringWheatToSam.class)
                || data.isCurrentlyActive(MissionTalkToSamAtComposter.class) || data.isCurrentlyActive(MissionPurchaseFuel.class);
        return tutorialStarted && !reached;
    }

    public enum Source {
        INVENTORY, SACKS
    }

    public static int available(SkyBlockPlayer player, ItemType type, Source source) {
        return source == Source.INVENTORY ? player.getAmountInInventory(type) : player.getSackItems().getAmount(type);
    }

    private static boolean take(SkyBlockPlayer player, ItemType type, int count, Source source) {
        if (source == Source.SACKS) {
            player.getSackItems().decrease(type, count);
            return true;
        }
        return player.takeItem(type, count) != null;
    }

    public static int addMatter(SkyBlockPlayer player, ItemType type, int amount, Source source) {
        ComposterConfig config = ComposterConfig.get();
        double value = config.organicMatterValue(type.name());
        if (value <= 0) return 0;
        ComposterState state = state(player);
        double space = ComposterEngine.organicMatterCap(state, config) - state.getOrganicMatter();
        int count = (int) Math.min(Math.min(amount, available(player, type, source)), Math.floor(space / value));
        if (count <= 0 || !take(player, type, count, source)) return 0;
        state.setOrganicMatter(state.getOrganicMatter() + count * value);
        Gardens.save(player, Gardens.profile(player));
        tick(player);
        return count;
    }

    public static int addFuel(SkyBlockPlayer player, ItemType type, int amount, Source source) {
        ComposterConfig config = ComposterConfig.get();
        double value = config.fuelValue(type.name());
        if (value <= 0) return 0;
        ComposterState state = state(player);
        double space = ComposterEngine.fuelCap(state, config) - state.getFuel();
        int count = (int) Math.min(Math.min(amount, available(player, type, source)), Math.floor(space / value));
        if (count <= 0 || !take(player, type, count, source)) return 0;
        state.setFuel(state.getFuel() + count * value);
        Gardens.save(player, Gardens.profile(player));
        player.sendMessage(Text.of("<a>Added {} fuel to the composter!", StringUtility.commaify((long) (count * value))));
        GardenQuests.composterFuelled(player);
        tick(player);
        return count;
    }

    public static int collect(SkyBlockPlayer player) {
        tick(player);
        ComposterState state = state(player);
        int compost = state.getStoredCompost();
        if (compost <= 0) return 0;
        state.setStoredCompost(0);
        player.addAndUpdateItem(new SkyBlockItem(ItemType.COMPOST, compost));
        Gardens.save(player, Gardens.profile(player));
        player.sendMessage(Text.of("<a>Picked up {} Compost!", StringUtility.commaify(compost)));
        GardenQuests.compostCollected(player);
        return compost;
    }

    public static UpgradeResult upgrade(SkyBlockPlayer player, ComposterUpgrade upgrade) {
        ComposterConfig config = ComposterConfig.get();
        GardenProfile profile = Gardens.profile(player);
        ComposterState state = state(player);
        int next = state.tier(upgrade) + 1;
        ComposterConfig.Tier tier = config.tier(upgrade, next);
        if (tier == null) return UpgradeResult.MAXED;
        if (GardenProgress.level(profile) < config.levelRequired(upgrade)) return UpgradeResult.LEVEL_TOO_LOW;
        if (profile.getCopper() < tier.copper()) return UpgradeResult.MISSING_COPPER;
        for (ComposterConfig.Item item : required(tier)) {
            ItemType type = ItemType.get(item.item());
            if (type == null || player.getAmountInInventory(type) + player.getSackItems().getAmount(type) < item.amount()) {
                return UpgradeResult.MISSING_ITEMS;
            }
        }
        for (ComposterConfig.Item item : required(tier)) {
            player.removeItemFromPlayer(ItemType.get(item.item()), item.amount());
        }
        profile.spendCopper(tier.copper());
        state.setTier(upgrade, next);
        Gardens.save(player, profile);
        GardenService.syncExperience(player);
        player.sendMessage(Text.of("<b><l>COMPOSTER UPGRADED <a>{} {}", upgrade.getDisplayName(), StringUtility.getAsRomanNumeral(next)));
        return UpgradeResult.UPGRADED;
    }

    public static java.util.List<ComposterConfig.Item> required(ComposterConfig.Tier tier) {
        java.util.List<ComposterConfig.Item> items = new java.util.ArrayList<>(tier.items());
        if (tier.rareCrop() != null) items.add(tier.rareCrop());
        return items;
    }

    /** Inserts everything that fits from the chosen source; returns the organic matter added. */
    public static double insertAllMatter(SkyBlockPlayer player, Source source) {
        double before = state(player).getOrganicMatter();
        for (String id : ComposterConfig.get().organicMatter().keySet()) {
            ItemType type = ItemType.get(id);
            if (type != null) {
                int have = available(player, type, source);
                if (have > 0) addMatter(player, type, have, source);
            }
        }
        return state(player).getOrganicMatter() - before;
    }

    public static double insertAllFuel(SkyBlockPlayer player, Source source) {
        double before = state(player).getFuel();
        for (String id : ComposterConfig.get().fuel().keySet()) {
            ItemType type = ItemType.get(id);
            if (type != null) {
                int have = available(player, type, source);
                if (have > 0) addFuel(player, type, have, source);
            }
        }
        return state(player).getFuel() - before;
    }

    public static double worth(SkyBlockPlayer player, Source source, boolean fuel) {
        double total = 0;
        ComposterConfig config = ComposterConfig.get();
        for (var entry : (fuel ? config.fuel() : config.organicMatter()).entrySet()) {
            ItemType type = ItemType.get(entry.getKey());
            if (type != null) total += available(player, type, source) * entry.getValue();
        }
        return total;
    }
}
