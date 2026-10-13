package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.farm.GardenFarm;
import net.swofty.type.skyblockgeneric.garden.pest.PestRewards;
import net.swofty.type.skyblockgeneric.garden.quest.GardenQuests;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class GreenhouseService {
    public interface StageListener {
        void onStage(SkyBlockPlayer player, GardenProfile profile, int plot, GreenhouseData.Bed bed, long now);
    }

    public interface HarvestListener {
        void onHarvest(SkyBlockPlayer player, GardenProfile profile, int plot, int slot, PlantType type, List<GreenhouseEffect> effects);
    }

    private static final long MISSED_STAGE_GRACE_MILLIS = 120_000;
    private static final int MAX_CATCH_UP = 96;
    private static final List<StageListener> STAGE_LISTENERS = new ArrayList<>();
    private static final List<HarvestListener> HARVEST_LISTENERS = new ArrayList<>();

    public enum UpgradeResult {
        UPGRADED, MAX_TIER, NOT_ENOUGH_COPPER, NOT_ENOUGH_BUNDLES, NOT_ENOUGH_VINES, SLOTS_LOCKED
    }

    private GreenhouseService() {
    }

    public static void addStageListener(StageListener listener) {
        STAGE_LISTENERS.add(listener);
    }

    public static void addHarvestListener(HarvestListener listener) {
        HARVEST_LISTENERS.add(listener);
    }

    public static GreenhouseData data(SkyBlockPlayer player) {
        return Gardens.profile(player).greenhouse();
    }

    public static boolean isGreenhouse(GardenProfile profile, int plot) {
        return profile.greenhouse().bed(plot) != null;
    }

    public static boolean canConvert(GardenProfile profile) {
        GreenhouseData data = profile.greenhouse();
        return data.isBlueprintDelivered() && data.isResourcesDelivered() && data.getBeds().size() < data.greenhouseLimit();
    }

    public static boolean convert(SkyBlockPlayer player, int plot) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData data = profile.greenhouse();
        Instance instance = player.getInstance();
        if (instance == null || plot == GardenLayout.NONE || plot == GardenLayout.BARN || !profile.isPlotUnlocked(plot)) return false;
        if (isGreenhouse(profile, plot)) return false;
        if (!data.isBlueprintDelivered() || !data.isResourcesDelivered()) {
            player.sendMessage(Text.of("<c>You need a Greenhouse Blueprint and Sam's help to build a Greenhouse!"));
            return false;
        }
        if (data.getBeds().size() >= data.greenhouseLimit()) {
            player.sendMessage(Text.of("<c>Already at max Greenhouse plots!"));
            return false;
        }
        GreenhouseData.Bed bed = new GreenhouseData.Bed();
        boolean first = data.getBeds().isEmpty();
        GreenhouseConfig config = GreenhouseConfig.get();
        int[] columns = config.starterColumns();
        int[] rows = config.starterRows();
        for (int slot = 0; slot < GreenhouseData.SLOTS; slot++) {
            int column = slot % GreenhouseData.SIZE;
            int row = slot / GreenhouseData.SIZE;
            bed.getUnlocked()[slot] = !first || (column >= columns[0] && column <= columns[1] && row >= rows[0] && row <= rows[1]);
        }
        data.getBeds().put(plot, bed);
        profile.setPlotPreset(plot, "Greenhouse");
        profile.markPlotCleaned(plot);
        Gardens.save(player, profile);
        player.sendMessage(Text.of("<a>Started pasting Greenhouse preset on Garden Plot - {}!", plot));
        GardenQuests.greenhouseConverted(player);
        GreenhousePreset.get().paste(instance, plot, () -> {
            GreenhouseWorld.render(instance, plot, bed);
            player.sendMessage(Text.of("<a>Your Greenhouse on Plot - {} <a>is ready!", plot));
            GardenQuests.greenhouseBuilt(player);
        });
        return true;
    }

    public static @Nullable GreenhouseLayout.Spot spotAt(GardenProfile profile, Point point) {
        int plot = GardenLayout.plotAt(point.x(), point.z());
        if (plot == GardenLayout.NONE || !isGreenhouse(profile, plot)) return null;
        return GreenhouseLayout.spotAt(point, plot);
    }

    public static boolean handleBreak(SkyBlockPlayer player, Point point) {
        GardenProfile profile = Gardens.profile(player);
        int plot = GardenLayout.plotAt(point.x(), point.z());
        if (plot == GardenLayout.NONE || !isGreenhouse(profile, plot)) return false;
        GreenhouseLayout.Spot spot = GreenhouseLayout.spotAt(point, plot);
        if (spot == null) return true;
        GreenhouseData.Plant plant = profile.greenhouse().bed(plot).getPlants().get(spot.slot());
        PlantType type = plant == null ? null : GreenhouseConfig.get().plant(plant.getType());
        if (type == null) return true;
        if (plant.grown(type)) {
            harvest(player, spot);
        } else {
            player.openView(new net.swofty.type.skyblockgeneric.gui.inventories.garden.GUIGreenhouseDestroy(spot));
        }
        return true;
    }

    public static boolean plant(SkyBlockPlayer player, GreenhouseLayout.Spot spot, SkyBlockItem held) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData.Bed bed = profile.greenhouse().bed(spot.plot());
        if (bed == null) return false;
        ItemType heldType = held.getAttributeHandler().getPotentialType();
        PlantType type = plantFor(heldType);
        if (type == null) return false;
        if (!bed.getUnlocked()[spot.slot()]) {
            player.sendMessage(Text.of("<c>This slot is currently locked! Use an Ethereal Vine to unlock this slot!"));
            return true;
        }
        if (bed.getPlants().containsKey(spot.slot())) {
            player.sendMessage(Text.of("<c>This block is already occupied!"));
            return true;
        }
        player.removeItemFromPlayer(heldType, 1);
        GreenhouseData.Plant plant = new GreenhouseData.Plant(type.id(), System.currentTimeMillis());
        bed.getPlants().put(spot.slot(), plant);
        Gardens.save(player, profile);
        render(player, spot.plot());
        return true;
    }

    public static @Nullable PlantType plantFor(@Nullable ItemType item) {
        GreenhouseConfig config = GreenhouseConfig.get();
        GardenCrop crop = GardenCrop.ofItem(item);
        if (crop != null) return config.plantOf(crop);
        for (PlantType type : config.plants()) {
            if (type.mutation() && item != null && type.id().equals(item.name())) return type;
        }
        return null;
    }

    public static void destroyUngrown(SkyBlockPlayer player, GreenhouseLayout.Spot spot) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData.Bed bed = profile.greenhouse().bed(spot.plot());
        if (bed == null || bed.getPlants().remove(spot.slot()) == null) return;
        Gardens.save(player, profile);
        render(player, spot.plot());
    }

    public static boolean harvest(SkyBlockPlayer player, GreenhouseLayout.Spot spot) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData data = profile.greenhouse();
        GreenhouseData.Bed bed = data.bed(spot.plot());
        GreenhouseConfig config = GreenhouseConfig.get();
        GreenhouseData.Plant plant = bed == null ? null : bed.getPlants().get(spot.slot());
        PlantType type = plant == null ? null : config.plant(plant.getType());
        if (type == null) return false;
        if (!plant.grown(type)) return false;
        List<GreenhouseEffect> effects = GreenhouseRules.appliedEffects(bed, config).getOrDefault(spot.slot(), List.of());
        bed.getPlants().remove(spot.slot());
        double unique = GreenhouseRules.uniquePercent(data, config);
        if (type.baseCrop()) {
            double fortune = GardenFarm.fortune(player, profile, type.crop());
            double multiplier = GreenhouseRules.yieldMultiplier(effects, config.plantYieldPercent(data.getPlantYieldTier()), unique);
            int amount = GreenhouseRules.harvestAmount(type, fortune, multiplier);
            ItemType item = type.crop() == GardenCrop.MUSHROOM ? ItemType.RED_MUSHROOM : itemOf(type.crop());
            if (item != null && amount > 0) {
                PestRewards.give(player, new SkyBlockItem(item, amount));
                player.sendMessage(Text.of("<7>You harvested <a>{}x {}<7>!", StringUtility.commaify(amount), new SkyBlockItem(item).getDisplayNameText()));
            }
            double xp = config.baseXp() * GreenhouseRules.xpMultiplier(effects);
            player.getSkills().increase(player, SkillCategories.FARMING, xp);
            GardenService.recordCrops(player, type.crop(), Math.max(1, amount));
        }
        rollBounty(player, effects);
        for (HarvestListener listener : HARVEST_LISTENERS) listener.onHarvest(player, profile, spot.plot(), spot.slot(), type, effects);
        Gardens.save(player, profile);
        render(player, spot.plot());
        return true;
    }

    private static @Nullable ItemType itemOf(GardenCrop crop) {
        return switch (crop) {
            case WHEAT -> ItemType.WHEAT;
            case CARROT -> ItemType.CARROT;
            case POTATO -> ItemType.POTATO;
            case PUMPKIN -> ItemType.PUMPKIN;
            case SUGAR_CANE -> ItemType.SUGAR_CANE;
            case MELON -> ItemType.MELON_SLICE;
            case CACTUS -> ItemType.CACTUS;
            case COCOA_BEANS -> ItemType.COCOA_BEANS;
            case MUSHROOM -> ItemType.RED_MUSHROOM;
            case NETHER_WART -> ItemType.NETHER_WART;
            case SUNFLOWER -> ItemType.SUNFLOWER;
            case MOONFLOWER -> ItemType.MOONFLOWER;
            case WILD_ROSE -> ItemType.WILD_ROSE;
        };
    }

    private static void rollBounty(SkyBlockPlayer player, List<GreenhouseEffect> effects) {
        if (!effects.contains(GreenhouseEffect.BONUS_DROPS)) return;
        for (GreenhouseConfig.Bounty drop : GreenhouseConfig.get().bounty()) {
            ItemType item = ItemType.get(drop.item());
            if (item == null || !LootRoll.roll(player.getUuid(), item, drop.chancePercent(), ThreadLocalRandom.current())) continue;
            SkyBlockItem reward = new SkyBlockItem(item, 1);
            PestRewards.give(player, reward);
            player.sendMessage(Text.of("<6><l>HARVEST BOUNTY!</l> {}", reward.getDisplayNameText()));
        }
    }

    public static void water(SkyBlockPlayer player, GreenhouseLayout.Spot spot, double levels) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData.Bed bed = profile.greenhouse().bed(spot.plot());
        GreenhouseData.Plant plant = bed == null ? null : bed.getPlants().get(spot.slot());
        if (plant == null) return;
        plant.setWater(Math.min(GreenhouseRules.MAX_WATER, plant.getWater() + levels));
        plant.setLastWateredAt(System.currentTimeMillis());
    }

    public static boolean unlockSlot(SkyBlockPlayer player, GreenhouseLayout.Spot spot, SkyBlockItem held) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData.Bed bed = profile.greenhouse().bed(spot.plot());
        if (bed == null || !ItemType.ETHEREAL_VINE.equals(held.getAttributeHandler().getPotentialType())) return false;
        if (bed.getUnlocked()[spot.slot()]) {
            player.sendMessage(Text.of("<c>This slot is already unlocked!"));
            return true;
        }
        if (!adjacentToUnlocked(bed, spot.slot())) {
            player.sendMessage(Text.of("<c>You can only unlock slots next to an unlocked slot!"));
            return true;
        }
        player.removeItemFromPlayer(ItemType.ETHEREAL_VINE, 1);
        bed.getUnlocked()[spot.slot()] = true;
        Gardens.save(player, profile);
        render(player, spot.plot());
        player.sendMessage(Text.of("<a>You unlocked a plant slot! <8>({}/{})", bed.unlockedCount(), GreenhouseData.SLOTS));
        return true;
    }

    public static boolean adjacentToUnlocked(GreenhouseData.Bed bed, int slot) {
        for (int[] cell : GreenhouseLayout.neighbours(slot)) {
            if (bed.getUnlocked()[GreenhouseData.slot(cell[0], cell[1])]) return true;
        }
        return false;
    }

    public static void render(SkyBlockPlayer player, int plot) {
        Instance instance = player.getInstance();
        GreenhouseData.Bed bed = data(player).bed(plot);
        if (instance != null && bed != null) GreenhouseWorld.render(instance, plot, bed);
    }

    public static long stageMillis(GreenhouseData data) {
        GreenhouseConfig config = GreenhouseConfig.get();
        return GreenhouseRules.stageMillis(config, data.getGrowthSpeedTier(), GreenhouseRules.uniquePercent(data, config));
    }

    public static void tick(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData data = profile.greenhouse();
        if (data.getBeds().isEmpty()) return;
        long now = System.currentTimeMillis();
        boolean changed = false;
        if (data.getNextStageAt() == 0) {
            data.setNextStageAt(now + stageMillis(data));
            changed = true;
        }
        int guard = 0;
        while (now >= data.getNextStageAt() && guard++ < MAX_CATCH_UP) {
            boolean missed = now - data.getNextStageAt() > MISSED_STAGE_GRACE_MILLIS;
            advanceStage(player, profile, now);
            long step = missed ? Math.round(GreenhouseConfig.get().stageHours() * 3_600_000L) : stageMillis(data);
            data.setNextStageAt(data.getNextStageAt() + step);
            changed = true;
        }
        if (guard > MAX_CATCH_UP) data.setNextStageAt(now + stageMillis(data));
        if (decay(profile, now)) changed = true;
        if (changed) Gardens.save(player, profile);
        Instance instance = player.getInstance();
        if (instance != null) data.getBeds().forEach((plot, bed) -> GreenhouseWorld.render(instance, plot, bed));
    }

    public static void advanceStage(SkyBlockPlayer player, GardenProfile profile, long now) {
        GreenhouseData data = profile.greenhouse();
        GreenhouseConfig config = GreenhouseConfig.get();
        data.setStageCount(data.getStageCount() + 1);
        data.setLastStageAt(now);
        for (var entry : data.getBeds().entrySet()) {
            GreenhouseData.Bed bed = entry.getValue();
            var effects = GreenhouseRules.appliedEffects(bed, config);
            for (var plantEntry : bed.getPlants().entrySet()) {
                GreenhouseData.Plant plant = plantEntry.getValue();
                PlantType type = config.plant(plant.getType());
                if (type == null || plant.grown(type)) continue;
                plant.setWater(Math.max(GreenhouseRules.MIN_WATER,
                        plant.getWater() - GreenhouseRules.waterLoss(config, effects.getOrDefault(plantEntry.getKey(), List.of()), ThreadLocalRandom.current())));
                if (plant.getWater() <= GreenhouseRules.MIN_WATER) {
                    plantEntry.setValue(deadPlant(now));
                    continue;
                }
                if (GreenhouseRules.advances(config, plant.getWater(), ThreadLocalRandom.current())) {
                    plant.setStage(plant.getStage() + 1);
                    if (plant.grown(type)) plant.setGrownAt(now);
                }
            }
            for (StageListener listener : STAGE_LISTENERS) listener.onStage(player, profile, entry.getKey(), bed, now);
        }
    }

    private static GreenhouseData.Plant deadPlant(long now) {
        GreenhouseData.Plant dead = new GreenhouseData.Plant(GreenhouseConfig.DEAD_PLANT, now);
        dead.setGrownAt(now);
        return dead;
    }

    private static boolean decay(GardenProfile profile, long now) {
        GreenhouseConfig config = GreenhouseConfig.get();
        boolean changed = false;
        for (GreenhouseData.Bed bed : profile.greenhouse().getBeds().values()) {
            for (var entry : bed.getPlants().entrySet()) {
                GreenhouseData.Plant plant = entry.getValue();
                if (plant.getType().equals(GreenhouseConfig.DEAD_PLANT)) continue;
                if (GreenhouseRules.decayed(config, plant, true, now)) {
                    entry.setValue(deadPlant(now));
                    changed = true;
                }
            }
        }
        return changed;
    }

    public static void simulate(SkyBlockPlayer player, int hours) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData data = profile.greenhouse();
        long now = System.currentTimeMillis();
        int stages = Math.max(1, (int) Math.round(hours * 3_600_000.0 / stageMillis(data)));
        for (int i = 0; i < stages; i++) advanceStage(player, profile, now);
        for (GreenhouseData.Bed bed : data.getBeds().values()) {
            for (GreenhouseData.Plant plant : bed.getPlants().values()) {
                plant.setLastWateredAt(plant.getLastWateredAt() + hours * 3_600_000L);
            }
        }
        Gardens.save(player, profile);
        data.getBeds().keySet().forEach(plot -> render(player, plot));
        player.sendMessage(Text.of("<a>Simulated {} hours of Greenhouse time!", hours));
    }

    public static long nextStageIn(GreenhouseData data) {
        return Math.max(0, data.getNextStageAt() - System.currentTimeMillis());
    }

    public static UpgradeResult upgrade(SkyBlockPlayer player, GreenhouseConfig.Upgrade upgrade) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData data = profile.greenhouse();
        GreenhouseConfig config = GreenhouseConfig.get();
        int tier = tier(data, upgrade);
        if (tier >= config.maxTier(upgrade)) return UpgradeResult.MAX_TIER;
        if (upgrade == GreenhouseConfig.Upgrade.PLOT_LIMIT && !data.getBeds().values().stream().allMatch(GreenhouseData.Bed::allUnlocked)) {
            return UpgradeResult.SLOTS_LOCKED;
        }
        GreenhouseConfig.Cost cost = config.costs(upgrade).get(tier);
        if (profile.getCopper() < cost.copper()) return UpgradeResult.NOT_ENOUGH_COPPER;
        if (cost.bundles() > 0 && player.getAmountInInventory(ItemType.COMPOST_BUNDLE) + player.getSackItems().getAmount(ItemType.COMPOST_BUNDLE) < cost.bundles()) {
            return UpgradeResult.NOT_ENOUGH_BUNDLES;
        }
        if (cost.vines() > 0 && player.getAmountInInventory(ItemType.ETHEREAL_VINE) + player.getSackItems().getAmount(ItemType.ETHEREAL_VINE) < cost.vines()) {
            return UpgradeResult.NOT_ENOUGH_VINES;
        }
        profile.spendCopper(cost.copper());
        take(player, ItemType.COMPOST_BUNDLE, cost.bundles());
        take(player, ItemType.ETHEREAL_VINE, cost.vines());
        setTier(data, upgrade, tier + 1);
        Gardens.save(player, profile);
        GardenService.syncExperience(player);
        return UpgradeResult.UPGRADED;
    }

    private static void take(SkyBlockPlayer player, ItemType type, int amount) {
        if (amount <= 0) return;
        int inventory = Math.min(amount, player.getAmountInInventory(type));
        if (inventory > 0) player.removeItemFromPlayer(type, inventory);
        if (amount > inventory) player.getSackItems().decrease(type, amount - inventory);
    }

    public static int tier(GreenhouseData data, GreenhouseConfig.Upgrade upgrade) {
        return switch (upgrade) {
            case GROWTH_SPEED -> data.getGrowthSpeedTier();
            case PLANT_YIELD -> data.getPlantYieldTier();
            case PLOT_LIMIT -> data.getPlotLimitTier();
        };
    }

    private static void setTier(GreenhouseData data, GreenhouseConfig.Upgrade upgrade, int tier) {
        switch (upgrade) {
            case GROWTH_SPEED -> data.setGrowthSpeedTier(tier);
            case PLANT_YIELD -> data.setPlantYieldTier(tier);
            case PLOT_LIMIT -> data.setPlotLimitTier(tier);
        }
    }
}
