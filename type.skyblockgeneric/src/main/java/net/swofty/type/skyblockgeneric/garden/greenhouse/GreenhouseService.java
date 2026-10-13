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
import net.swofty.type.skyblockgeneric.item.components.HoeComponent;
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

    private static final String JERRYSEED = "FERTILIZED_JERRYSEED";
    private static final long MISSED_STAGE_GRACE_MILLIS = 120_000;
    private static final int MAX_CATCH_UP = 96;
    private static final List<StageListener> STAGE_LISTENERS = new ArrayList<>();
    private static final List<HarvestListener> HARVEST_LISTENERS = new ArrayList<>();

    public enum UpgradeResult {
        UPGRADED, MAX_TIER, NOT_ENOUGH_COPPER, NOT_ENOUGH_BUNDLES, NOT_ENOUGH_VINES, SLOTS_LOCKED
    }

    static {
        STAGE_LISTENERS.add(MutationSpread::onStage);
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

    private static @Nullable MutationBehaviour.Ctx context(SkyBlockPlayer player, GardenProfile profile, int plot, GreenhouseData.Bed bed, int anchor, long now) {
        GreenhouseData.Plant plant = bed.getPlants().get(anchor);
        PlantType type = plant == null ? null : GreenhouseConfig.get().plant(plant.getType());
        return type == null ? null : new MutationBehaviour.Ctx(player, profile, plot, bed, anchor, plant, type, now);
    }

    public static boolean handleBreak(SkyBlockPlayer player, Point point) {
        GardenProfile profile = Gardens.profile(player);
        int plot = GardenLayout.plotAt(point.x(), point.z());
        if (plot == GardenLayout.NONE || !isGreenhouse(profile, plot)) return false;
        GreenhouseLayout.Spot clicked = GreenhouseLayout.spotAt(point, plot);
        if (clicked == null) return true;
        GreenhouseData.Bed bed = profile.greenhouse().bed(plot);
        int anchor = bed.anchorAt(clicked.slot());
        if (anchor < 0) return true;
        GreenhouseLayout.Spot spot = new GreenhouseLayout.Spot(plot, anchor);
        if (GreenhouseMinigames.intercept(player, spot, true)) return true;
        MutationBehaviour.Ctx ctx = context(player, profile, plot, bed, anchor, System.currentTimeMillis());
        if (ctx == null) return true;
        if (MutationBehaviours.of(ctx.type().id()).harvestable(ctx)) {
            harvest(player, spot);
        } else {
            player.openView(new net.swofty.type.skyblockgeneric.gui.inventories.garden.GUIGreenhouseDestroy(spot));
        }
        return true;
    }

    public static boolean interact(SkyBlockPlayer player, GreenhouseLayout.Spot clicked, SkyBlockItem held) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData.Bed bed = profile.greenhouse().bed(clicked.plot());
        if (bed == null) return false;
        int anchor = bed.anchorAt(clicked.slot());
        if (anchor < 0) return !held.isNA() && setSurface(player, profile, bed, clicked, held);
        GreenhouseLayout.Spot spot = new GreenhouseLayout.Spot(clicked.plot(), anchor);
        if (GreenhouseMinigames.intercept(player, spot, false)) return true;
        MutationBehaviour.Ctx ctx = context(player, profile, clicked.plot(), bed, anchor, System.currentTimeMillis());
        if (ctx == null || !MutationBehaviours.of(ctx.type().id()).onUse(ctx, held)) return false;
        Gardens.save(player, profile);
        render(player, clicked.plot());
        return true;
    }

    private static boolean setSurface(SkyBlockPlayer player, GardenProfile profile, GreenhouseData.Bed bed, GreenhouseLayout.Spot spot, SkyBlockItem held) {
        ItemType heldType = held.getAttributeHandler().getPotentialType();
        boolean hoe = held.hasComponent(HoeComponent.class);
        Surface surface = hoe ? Surface.FARMLAND : Surface.ofItem(heldType == null ? null : heldType.name());
        if (surface == null || !bed.getUnlocked()[spot.slot()]) return false;
        if (bed.surface(spot.slot()) == surface) {
            player.sendMessage(Text.of("<c>This slot already has {} <c>as its growth surface!", surface.displayName()));
            return true;
        }
        if (!hoe) player.removeItemFromPlayer(heldType, 1);
        bed.setSurface(spot.slot(), surface);
        Gardens.save(player, profile);
        render(player, spot.plot());
        player.sendMessage(Text.of("<a>Changed the growth surface to <e>{}<a>!", surface.displayName()));
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
        int size = type.footprint();
        int anchor = Footprint.anchorFor(spot.slot(), size);
        List<Integer> cells = Footprint.cells(anchor, size);
        if (cells.isEmpty()) {
            player.sendMessage(Text.of("<c>There is not enough room for a {}x{} crop here!", size, size));
            return true;
        }
        for (int cell : cells) {
            if (!bed.getUnlocked()[cell]) {
                player.sendMessage(Text.of("<c>Every slot under a {}x{} crop must be unlocked!", size, size));
                return true;
            }
            if (bed.occupied(cell)) {
                player.sendMessage(Text.of("<c>This block is already occupied!"));
                return true;
            }
            if (!type.id().equals(GreenhouseConfig.DEAD_PLANT) && !Surface.accepts(type.surface(), bed.surface(cell))) {
                player.sendMessage(Text.of("<c>{} <c>needs <e>{}<c> to grow! Right-click an empty slot with that block to change its surface.", type.name(), type.surface()));
                return true;
            }
        }
        boolean seed = heldType != null && heldType.name().equals(JERRYSEED);
        if (type.mutation() && !seed && !profile.greenhouse().getAnalyzed().contains(type.id())) {
            player.sendMessage(Text.of("<c>You haven't analyzed this Mutation! Analyze it at the Crop Analyzer first."));
            return true;
        }
        player.removeItemFromPlayer(heldType, 1);
        GreenhouseData.Plant plant = new GreenhouseData.Plant(type.id(), System.currentTimeMillis());
        if (type.stages() == 0) plant.setGrownAt(plant.getPlantedAt());
        bed.getPlants().put(anchor, plant);
        Gardens.save(player, profile);
        render(player, spot.plot());
        return true;
    }

    public static @Nullable PlantType plantFor(@Nullable ItemType item) {
        GreenhouseConfig config = GreenhouseConfig.get();
        GardenCrop crop = GardenCrop.ofItem(item);
        if (crop != null) return config.plantOf(crop);
        if (item != null && item.name().equals(JERRYSEED)) return config.plant("JERRYFLOWER");
        MutationCatalogue.Mutation mutation = MutationCatalogue.get().ofItem(item);
        return mutation == null ? null : config.plant(mutation.id());
    }

    public static void destroyUngrown(SkyBlockPlayer player, GreenhouseLayout.Spot spot) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData.Bed bed = profile.greenhouse().bed(spot.plot());
        if (bed == null || bed.getPlants().remove(spot.slot()) == null) return;
        if (player.getInstance() != null) GreenhouseCreatures.dismiss(player.getInstance(), spot.plot(), spot.slot(), "rat");
        Gardens.save(player, profile);
        render(player, spot.plot());
    }

    public static boolean harvest(SkyBlockPlayer player, GreenhouseLayout.Spot spot) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData.Bed bed = profile.greenhouse().bed(spot.plot());
        MutationBehaviour.Ctx ctx = bed == null ? null : context(player, profile, spot.plot(), bed, spot.slot(), System.currentTimeMillis());
        if (ctx == null) return false;
        MutationBehaviour behaviour = MutationBehaviours.of(ctx.type().id());
        if (!behaviour.harvestable(ctx)) return false;
        MutationBehaviour.Harvest outcome = behaviour.onHarvest(ctx, new SkyBlockItem(player.getItemInMainHand()));
        if (outcome != MutationBehaviour.Harvest.PROCEED) {
            Gardens.save(player, profile);
            render(player, spot.plot());
            return true;
        }
        return finishHarvest(player, spot);
    }

    public static boolean finishHarvest(SkyBlockPlayer player, GreenhouseLayout.Spot spot) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData data = profile.greenhouse();
        GreenhouseData.Bed bed = data.bed(spot.plot());
        GreenhouseConfig config = GreenhouseConfig.get();
        MutationBehaviour.Ctx ctx = bed == null ? null : context(player, profile, spot.plot(), bed, spot.slot(), System.currentTimeMillis());
        if (ctx == null) return false;
        PlantType type = ctx.type();
        MutationBehaviour behaviour = MutationBehaviours.of(type.id());
        List<GreenhouseEffect> effects = GreenhouseRules.appliedEffects(bed, config).getOrDefault(spot.slot(), List.of());
        bed.getPlants().remove(spot.slot());
        double unique = GreenhouseRules.uniquePercent(data, config);
        if (type.baseCrop()) {
            double fortune = GardenFarm.fortune(player, profile, type.crop());
            double multiplier = GreenhouseRules.yieldMultiplier(effects, config.plantYieldPercent(data.getPlantYieldTier()), unique);
            int amount = GreenhouseRules.harvestAmount(type, fortune, multiplier);
            amount = (int) Math.round(amount * (1 + net.swofty.type.skyblockgeneric.garden.chip.ChipService.bonus(profile.chips(), net.swofty.type.skyblockgeneric.garden.chip.GardenChip.EVERGREEN) / 100.0));
            ItemType item = type.crop() == GardenCrop.MUSHROOM ? ItemType.RED_MUSHROOM : itemOf(type.crop());
            if (item != null && amount > 0) {
                PestRewards.give(player, new SkyBlockItem(item, amount));
                player.sendMessage(Text.of("<7>You harvested <a>{}x {}<7>!", StringUtility.commaify(amount), new SkyBlockItem(item).getDisplayNameText()));
            }
            profile.addSowdust(GreenhouseRules.sowdust(type.crop(), type.baseYield(), fortune));
            double xp = config.baseXp() * GreenhouseRules.xpMultiplier(effects);
            player.getSkills().increase(player, SkillCategories.FARMING, xp);
            GardenService.recordCrops(player, type.crop(), Math.max(1, amount));
        }
        if (type.mutation()) harvestMutation(player, profile, ctx, behaviour, effects, unique);
        rollBounty(player, effects);
        net.swofty.type.skyblockgeneric.garden.tool.OverclockerPity.record(player, profile, net.swofty.type.skyblockgeneric.garden.tool.OverclockerPity.Source.GREENHOUSE);
        behaviour.afterHarvest(ctx);
        for (HarvestListener listener : HARVEST_LISTENERS) listener.onHarvest(player, profile, spot.plot(), spot.slot(), type, effects);
        Gardens.save(player, profile);
        render(player, spot.plot());
        return true;
    }

    private static void harvestMutation(SkyBlockPlayer player, GardenProfile profile, MutationBehaviour.Ctx ctx, MutationBehaviour behaviour, List<GreenhouseEffect> effects, double unique) {
        PlantType type = ctx.type();
        GreenhouseConfig config = GreenhouseConfig.get();
        GreenhouseData data = profile.greenhouse();
        if (type.id().equals(GreenhouseConfig.DEAD_PLANT)) {
            ItemType dead = ItemType.get("DEAD_PLANT");
            if (dead != null) PestRewards.give(player, new SkyBlockItem(dead, 1));
            return;
        }
        MutationCatalogue.Mutation mutation = MutationCatalogue.get().mutation(type.id());
        if (mutation == null) return;
        ItemType item = mutation.item();
        int count = behaviour.itemCount(ctx);
        if (item != null && count > 0) {
            PestRewards.give(player, new SkyBlockItem(item, count));
            player.sendMessage(Text.of("<7>You harvested {}x {}<7>!", count, new SkyBlockItem(item).getDisplayNameText()));
        }
        for (SkyBlockItem extra : behaviour.extraDrops(ctx)) {
            PestRewards.give(player, extra);
            player.sendMessage(Text.of("<7>You harvested <a>{}x {}<7>!", extra.getAmount(), extra.getDisplayNameText()));
        }
        double fortune = player.getStatistics().allStatistics().getOverall(net.swofty.commons.skyblock.statistics.ItemStatistic.FARMING_FORTUNE);
        double multiplier = GreenhouseRules.yieldMultiplier(effects, config.plantYieldPercent(data.getPlantYieldTier()), unique) * behaviour.dropMultiplier(ctx);
        for (MutationCatalogue.Drop drop : mutation.drops()) {
            ItemType dropItem = ItemType.get(drop.item());
            if (dropItem == null) continue;
            int amount = (int) Math.round(drop.amount() * (1 + Math.max(0, fortune) / 100.0) * Math.max(0, multiplier));
            if (amount > 0) PestRewards.give(player, new SkyBlockItem(dropItem, amount));
            GardenCrop dropCrop = GardenCrop.ofItem(dropItem);
            if (dropCrop != null) profile.addSowdust(GreenhouseRules.sowdust(dropCrop, drop.amount(), fortune));
        }
        player.getSkills().increase(player, SkillCategories.FARMING, mutation.farmingXp() * GreenhouseRules.xpMultiplier(effects));
        ItemType vine = ItemType.ETHEREAL_VINE;
        if (LootRoll.roll(player.getUuid(), vine, config.vineChance(mutation.rarity()), ThreadLocalRandom.current())) {
            PestRewards.give(player, new SkyBlockItem(vine, 1));
            player.sendMessage(Text.of("<6><l>RARE DROP!</l> {}", new SkyBlockItem(vine).getDisplayNameText()));
        }
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
        GreenhouseData.Plant plant = bed == null ? null : bed.plantAt(spot.slot());
        if (plant == null || !MutationBehaviours.waterable(plant.getType())) return;
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
        if (decay(player, profile, now)) changed = true;
        tickBehaviours(player, profile, now);
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
            for (int anchor : new ArrayList<>(bed.getPlants().keySet())) {
                MutationBehaviour.Ctx ctx = context(player, profile, entry.getKey(), bed, anchor, now);
                if (ctx == null) continue;
                MutationBehaviour behaviour = MutationBehaviours.of(ctx.type().id());
                if (!behaviour.ticks(ctx)) continue;
                GreenhouseData.Plant plant = ctx.plant();
                if (behaviour.needsWater()) {
                    plant.setWater(Math.max(GreenhouseRules.MIN_WATER,
                            plant.getWater() - GreenhouseRules.waterLoss(config, effects.getOrDefault(anchor, List.of()), ThreadLocalRandom.current())));
                    if (plant.getWater() <= GreenhouseRules.MIN_WATER) {
                        bed.getPlants().put(anchor, deadPlant(now));
                        continue;
                    }
                    if (!GreenhouseRules.advances(config, plant.getWater(), ThreadLocalRandom.current())) continue;
                }
                stagePlant(ctx, behaviour);
            }
            for (StageListener listener : STAGE_LISTENERS) listener.onStage(player, profile, entry.getKey(), bed, now);
        }
    }

    private static void stagePlant(MutationBehaviour.Ctx ctx, MutationBehaviour behaviour) {
        if (!behaviour.beforeStage(ctx)) return;
        GreenhouseData.Plant plant = ctx.plant();
        plant.setStage(plant.getStage() + 1);
        if (plant.grown(ctx.type())) plant.setGrownAt(ctx.now());
        behaviour.onStage(ctx);
    }

    public static int growPlot(SkyBlockPlayer player, int plot, int stages) {
        GardenProfile profile = Gardens.profile(player);
        GreenhouseData.Bed bed = profile.greenhouse().bed(plot);
        if (bed == null) return 0;
        long now = System.currentTimeMillis();
        int grown = 0;
        for (int anchor : new ArrayList<>(bed.getPlants().keySet())) {
            for (int i = 0; i < stages; i++) {
                MutationBehaviour.Ctx ctx = context(player, profile, plot, bed, anchor, now);
                if (ctx == null) break;
                MutationBehaviour behaviour = MutationBehaviours.of(ctx.type().id());
                if (!behaviour.ticks(ctx)) break;
                int before = ctx.plant().getStage();
                stagePlant(ctx, behaviour);
                if (bed.getPlants().get(anchor) != ctx.plant()) break;
                if (ctx.plant().getStage() > before) grown++;
            }
        }
        Gardens.save(player, profile);
        render(player, plot);
        return grown;
    }

    private static GreenhouseData.Plant deadPlant(long now) {
        GreenhouseData.Plant dead = new GreenhouseData.Plant(GreenhouseConfig.DEAD_PLANT, now);
        dead.setGrownAt(now);
        return dead;
    }

    private static boolean decay(SkyBlockPlayer player, GardenProfile profile, long now) {
        GreenhouseConfig config = GreenhouseConfig.get();
        boolean changed = false;
        for (var bedEntry : profile.greenhouse().getBeds().entrySet()) {
            GreenhouseData.Bed bed = bedEntry.getValue();
            for (int anchor : new ArrayList<>(bed.getPlants().keySet())) {
                MutationBehaviour.Ctx ctx = context(player, profile, bedEntry.getKey(), bed, anchor, now);
                if (ctx == null || ctx.type().id().equals(GreenhouseConfig.DEAD_PLANT)) continue;
                MutationCatalogue.Mutation mutation = MutationCatalogue.get().mutation(ctx.type().id());
                if (mutation != null && mutation.neverDecays()) continue;
                MutationBehaviour behaviour = MutationBehaviours.of(ctx.type().id());
                if (!GreenhouseRules.decayed(config, ctx.plant(), MutationBehaviours.waterable(ctx.type().id()), now)) continue;
                behaviour.onDecay(ctx);
                bed.getPlants().put(anchor, deadPlant(now));
                changed = true;
            }
        }
        return changed;
    }

    private static void tickBehaviours(SkyBlockPlayer player, GardenProfile profile, long now) {
        Instance instance = player.getInstance();
        if (instance == null) return;
        List<SkyBlockPlayer> players = new ArrayList<>();
        for (net.minestom.server.entity.Player online : instance.getPlayers()) if (online instanceof SkyBlockPlayer other) players.add(other);
        for (var bedEntry : profile.greenhouse().getBeds().entrySet()) {
            GreenhouseData.Bed bed = bedEntry.getValue();
            for (int anchor : new ArrayList<>(bed.getPlants().keySet())) {
                MutationBehaviour.Ctx ctx = context(player, profile, bedEntry.getKey(), bed, anchor, now);
                if (ctx == null) continue;
                MutationBehaviour behaviour = MutationBehaviours.of(ctx.type().id());
                behaviour.onTick(ctx);
                if (behaviour.listensNear()) for (SkyBlockPlayer other : players) behaviour.onNear(ctx, other);
            }
        }
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
        if (cost.bundles() > 0 && player.getAmountAnywhere(ItemType.COMPOST_BUNDLE) < cost.bundles()) {
            return UpgradeResult.NOT_ENOUGH_BUNDLES;
        }
        if (cost.vines() > 0 && player.getAmountAnywhere(ItemType.ETHEREAL_VINE) < cost.vines()) {
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
        player.removeItemFromPlayer(type, amount);
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
