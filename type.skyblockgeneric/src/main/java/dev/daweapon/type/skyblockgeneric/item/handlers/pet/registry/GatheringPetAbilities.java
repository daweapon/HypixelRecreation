package dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry;

import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsZones;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.fishing.festival.FishingFestival;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.item.components.AxeComponent;
import dev.daweapon.type.skyblockgeneric.item.components.DrillComponent;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.ArmorBoostHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.BaseStatBoostHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.CountScaledStatHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.NearFluidStatHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.NearbyPlayersStatHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.TimedBuffHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.TreasureCatches;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.TreasureMaterializeHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.PetConditions;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.PetType;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;

import java.util.HashSet;
import java.util.Set;

/**
 * Handlers and named conditions for the Fishing, Mining and Foraging pets.
 */
public final class GatheringPetAbilities implements PetAbilityRegistry.Provider {
    private static final int MAX_ATTRIBUTE_LEVEL = 10;
    private static final int MANGROVE_DIGIT_CAP = 10_000_000;
    private static final long COMFORT_ZONE_MILLIS = 30_000;
    private static final long HOP_MILLIS = 20_000;

    @Override
    public void register(PetAbilityRegistry.Registrar registrar) {
        PetConditions.register("NIGHT", _ -> SkyBlockCalendar.getHour() < 6 || SkyBlockCalendar.getHour() >= 20);
        PetConditions.register("HOLDING_AXE", player -> holding(player, AxeComponent.class));
        PetConditions.register("HOLDING_DRILL", player -> holding(player, DrillComponent.class));
        PetConditions.register("ABSORPTION_ACTIVE", player -> player.getAdditionalHearts() > 0);
        PetConditions.register("HOLDING_INK_WAND", player -> holdingType(player) == ItemType.INK_WAND);

        PetConditions.register("CRYSTAL_HOLLOWS", player -> CrystalHollowsZones.isCrystalHollows(regionOf(player)));
        registrar.register("AMMONITE_EXPERT_CAVE_FISHER",
                CountScaledStatHandler.flat(GatheringPetAbilities::hotmTier, ItemStatistic.DOUBLE_HOOK_CHANCE));
        registrar.register("AMMONITE_HEART_OF_THE_SEA",
                CountScaledStatHandler.flat(GatheringPetAbilities::hotmTier, ItemStatistic.SEA_CREATURE_CHANCE));
        registrar.register("AMMONITE_GIFT", CountScaledStatHandler.flat(GatheringPetAbilities::miningAndFishingLevels,
                ItemStatistic.FISHING_SPEED, ItemStatistic.SPEED, ItemStatistic.DEFENSE));
        registrar.register("DOLPHIN_POD_TACTICS",
                new NearbyPlayersStatHandler(30, 5, null, ItemStatistic.FISHING_SPEED));
        registrar.register("FLYING_FISH_WATER_BENDER",
                new NearFluidStatHandler(Block.WATER, ItemStatistic.STRENGTH, ItemStatistic.DEFENSE));
        registrar.register("FLYING_FISH_LAVA_BENDER",
                new NearFluidStatHandler(Block.LAVA, ItemStatistic.STRENGTH, ItemStatistic.DEFENSE));
        registrar.register("FLYING_FISH_MAGMATIC_DIVER", new ArmorBoostHandler(Set.of(ItemType.MAGMA_LORD_HELMET,
                ItemType.MAGMA_LORD_CHESTPLATE, ItemType.MAGMA_LORD_LEGGINGS, ItemType.MAGMA_LORD_BOOTS)));
        registrar.register("HERMIT_CRAB_COMFORT_ZONE",
                new TimedBuffHandler(TreasureCatches::isTreasure, COMFORT_ZONE_MILLIS, ItemStatistic.FISHING_SPEED));
        registrar.register("HERMIT_CRAB_CRAB_RAVE",
                new NearbyPlayersStatHandler(30, 5, ItemType.HERMIT_CRAB_PET, ItemStatistic.TREASURE_CHANCE));
        registrar.register("MEGALODON_ENHANCED_SCALES",
                BaseStatBoostHandler.doubling(_ -> FishingFestival.isActive()));
        registrar.register("PENGUIN_CHILLY_RECEPTION",
                new NearbyPlayersStatHandler(30, 10, null, ItemStatistic.COLD_RESISTANCE));
        registrar.register("SEAL_PEAK_PERFORMANCE", new TreasureMaterializeHandler(ItemType.TREASURE_BAIT));
        registrar.register("SPINOSAURUS_ADAPTIVE_PREDATOR", BaseStatBoostHandler.byValue(GatheringPetAbilities::inWeather));
        registrar.register("FROG_HOP",
                new TimedBuffHandler(event -> event instanceof PetEvent.Jump, HOP_MILLIS, ItemStatistic.FORAGING_FORTUNE));
        registrar.register("FROG_HAPPY_TREE_FRIENDS", new NearbyPlayersStatHandler(NearbyPlayersStatHandler.WHOLE_ISLAND,
                10, ItemType.FROG_PET, ItemStatistic.FORAGING_FORTUNE, ItemStatistic.FISHING_SPEED));
        registrar.register("JADE_DRAGON_JADE_SCALE", CountScaledStatHandler.flat(GatheringPetAbilities::mangroveDigits,
                ItemStatistic.FORAGING_FORTUNE, ItemStatistic.SWEEP));
        registrar.register("JADE_DRAGON_APEX_PREDATOR",
                CountScaledStatHandler.percent(GatheringPetAbilities::maxedAttributes, ItemStatistic.SWEEP));
        registrar.register("JADE_DRAGON_SYMBIOSIS",
                CountScaledStatHandler.flat(GatheringPetAbilities::jadeSymbiosisPets, ItemStatistic.FORAGING_FORTUNE));
    }

    private static RegionType regionOf(SkyBlockPlayer player) {
        return player.getRegion() == null ? null : player.getRegion().getType();
    }

    private static boolean holding(SkyBlockPlayer player, Class<? extends SkyBlockItemComponent> component) {
        return new SkyBlockItem(player.getItemInMainHand()).hasComponent(component);
    }

    private static ItemType holdingType(SkyBlockPlayer player) {
        return new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType();
    }

    private static boolean inWeather(SkyBlockPlayer player) {
        return player.getInstance() != null && player.getInstance().getWeather().rainLevel() > 0;
    }

    private static int hotmTier(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).getValue().getTier();
    }

    private static int miningAndFishingLevels(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.MINING)
                + player.getSkills().getCurrentLevel(SkillCategories.FISHING);
    }

    private static int mangroveDigits(SkyBlockPlayer player) {
        Integer collected = player.getCollection().get(ItemType.MANGROVE_LOG);
        if (collected == null || collected <= 0) return 0;
        return String.valueOf(Math.min(collected, MANGROVE_DIGIT_CAP)).length();
    }

    private static int maxedAttributes(SkyBlockPlayer player) {
        return (int) AttributeRegistry.values().stream()
                .filter(definition -> player.getHuntingData().level(definition.id()) >= MAX_ATTRIBUTE_LEVEL)
                .count();
    }

    private static int jadeSymbiosisPets(SkyBlockPlayer player) {
        Set<SkyBlockItem> pets = player.getPetData().getPetsMap().keySet();
        boolean ownsMaxedJade = pets.stream().anyMatch(pet ->
                pet.getAttributeHandler().getPotentialType() == ItemType.JADE_DRAGON_PET && PetLevels.isMaxLevel(pet));
        if (!ownsMaxedJade) return 0;
        Set<ItemType> maxed = new HashSet<>();
        for (SkyBlockItem pet : pets) {
            ItemType type = pet.getAttributeHandler().getPotentialType();
            if (type == ItemType.JADE_DRAGON_PET || !pet.hasComponent(PetComponent.class)) continue;
            if (pet.getComponent(PetComponent.class).getPetType() == PetType.FORAGING && PetLevels.isMaxLevel(pet)) maxed.add(type);
        }
        return maxed.size();
    }
}
