package net.swofty.type.skyblockgeneric.carnival;

import net.swofty.type.skyblockgeneric.accessory.AccessoryOwnership;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.fishing.festival.FishingFestival;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/** Stats from Carnival Mask abilities and Doug's event perks. */
public final class CarnivalStatistics {
    private static final double PARTY_FLOCK_RANGE = 20;
    private static final List<RegionType> STICK_AROUND_REGIONS = List.of(
            RegionType.BIRCH_PARK, RegionType.SPRUCE_WOODS, RegionType.DARK_THICKET,
            RegionType.SAVANNA_WOODLAND, RegionType.JUNGLE_ISLAND);
    private static final Map<ItemType, Integer> INTIMIDATION_UPGRADES = Map.of(
            ItemType.INTIMIDATION_RING, 1, ItemType.INTIMIDATION_ARTIFACT, 2, ItemType.INTIMIDATION_RELIC, 3);

    private CarnivalStatistics() {}

    public static ItemStatistics all(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        ItemStatistics total = ItemStatistics.add(mask(player), partyFlock(player));
        if (HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_JERRYS_WORKSHOP) {
            total = ItemStatistics.add(total, single(ItemStatistic.SEA_CREATURE_CHANCE,
                    Carnivals.perkValue(player, CarnivalPerk.ICY_HOOK)));
        }
        if (FishingFestival.isActive()) {
            total = ItemStatistics.add(total, single(ItemStatistic.FISHING_WISDOM,
                    Carnivals.perkValue(player, CarnivalPerk.FISHING_PRODIGY)));
        }
        return total;
    }

    public static ItemStatistics against(SkyBlockPlayer player, @Nullable LivingEntity enemy) {
        if (!(enemy instanceof MythologicalCreatureMob) || player.getSkyblockDataHandler() == null) {
            return ItemStatistics.empty();
        }
        return single(ItemStatistic.MAGIC_FIND, Carnivals.perkValue(player, CarnivalPerk.DIANAS_FAVOR));
    }

    private static ItemStatistics mask(SkyBlockPlayer player) {
        ItemType helmet = helmetOf(player);
        if (helmet == null) return ItemStatistics.empty();
        if (helmet == ItemType.SALMON_MASK) {
            return inWater(player)
                    ? ItemStatistics.builder().withBase(ItemStatistic.SPEED, 10D).withBase(ItemStatistic.FISHING_SPEED, 10D).build()
                    : ItemStatistics.empty();
        }
        if (helmet == ItemType.ARMADILLO_MASK) return single(ItemStatistic.BLOCK_FORTUNE, 50);
        if (helmet == ItemType.FROG_MASK) {
            return inStickAroundRegion(player)
                    ? ItemStatistics.builder().withBase(ItemStatistic.FORAGING_FORTUNE, 20D).withBase(ItemStatistic.FORAGING_WISDOM, 5D).build()
                    : ItemStatistics.empty();
        }
        if (helmet == ItemType.BEE_MASK) return beePetBaseStatistics(player);
        return ItemStatistics.empty();
    }

    private static ItemStatistics partyFlock(SkyBlockPlayer player) {
        if (player.getInstance() == null) return ItemStatistics.empty();
        double strength = 0;
        for (SkyBlockPlayer other : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (other == player || other.getInstance() != player.getInstance()) continue;
            if (helmetOf(other) != ItemType.PARROT_MASK) continue;
            if (other.getPosition().distance(player.getPosition()) > PARTY_FLOCK_RANGE) continue;
            strength += intimidationUpgrades(other);
        }
        return single(ItemStatistic.STRENGTH, strength);
    }

    private static int intimidationUpgrades(SkyBlockPlayer player) {
        int best = 0;
        for (SkyBlockItem accessory : AccessoryOwnership.activeItems(player)) {
            best = Math.max(best, INTIMIDATION_UPGRADES.getOrDefault(accessory.getAttributeHandler().getPotentialType(), 0));
        }
        return best;
    }

    private static ItemStatistics beePetBaseStatistics(SkyBlockPlayer player) {
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        if (pet == null || pet.getAttributeHandler().getPotentialType() != ItemType.BEE_PET) return ItemStatistics.empty();
        return pet.getComponent(PetComponent.class).statisticsAt(pet.getAttributeHandler().getRarity(),
                PetLevels.level(pet));
    }

    public static RegionType stickAroundRegionToday() {
        long day = SkyBlockCalendar.getElapsed() / SkyBlockCalendar.DAY;
        return STICK_AROUND_REGIONS.get((int) Math.floorMod(day * 31L + 7L, (long) STICK_AROUND_REGIONS.size()));
    }

    private static boolean inStickAroundRegion(SkyBlockPlayer player) {
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(player.getPosition());
        return region != null && region.getType() == stickAroundRegionToday();
    }

    private static boolean inWater(SkyBlockPlayer player) {
        return player.getInstance() != null && player.getInstance().getBlock(player.getPosition()).compare(Block.WATER);
    }

    public static boolean isWearing(SkyBlockPlayer player, ItemType mask) {
        return helmetOf(player) == mask;
    }

    private static @Nullable ItemType helmetOf(SkyBlockPlayer player) {
        if (player.getHelmet().isAir()) return null;
        return new SkyBlockItem(player.getHelmet()).getAttributeHandler().getPotentialType();
    }

    private static ItemStatistics single(ItemStatistic statistic, double value) {
        if (value == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(statistic, value).build();
    }
}
