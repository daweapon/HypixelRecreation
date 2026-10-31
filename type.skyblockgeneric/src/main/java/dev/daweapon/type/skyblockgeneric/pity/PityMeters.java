package dev.daweapon.type.skyblockgeneric.pity;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.mob.VanquisherSpawns;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointTrophyFish.FishProgress;
import dev.daweapon.type.skyblockgeneric.fishing.catches.TrophyTier;
import dev.daweapon.type.skyblockgeneric.fishing.registry.FishingRegistry;
import dev.daweapon.type.skyblockgeneric.fishing.resolver.FishFamilyDrops;
import dev.daweapon.type.skyblockgeneric.fishing.trophy.TrophyFishRoller;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.tool.OverclockerPity;
import dev.daweapon.type.skyblockgeneric.glacite.mineshaft.MineshaftPity;
import dev.daweapon.type.skyblockgeneric.glacite.mineshaft.MineshaftService;
import dev.daweapon.type.skyblockgeneric.hoppity.RabbitRoller;
import dev.daweapon.type.skyblockgeneric.moonglade.MobTheFish;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import dev.daweapon.type.skyblockgeneric.theend.SpecialZealotOdds;
import dev.daweapon.type.skyblockgeneric.theend.ZealotPity;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class PityMeters {
    private static final long TREASURE_PITY = FishFamilyDrops.PITY_TREASURES;
    private static final long VANQUISHER_PITY = 750;
    private static final Map<OverclockerPity.Source, String> OVERCLOCKER_LABELS = Map.of(
            OverclockerPity.Source.VISITORS, "Visitors",
            OverclockerPity.Source.CONTESTS, "Jacob's Contests",
            OverclockerPity.Source.CROPS, "Crops",
            OverclockerPity.Source.GREENHOUSE, "Greenhouse",
            OverclockerPity.Source.PESTS, "Pests");

    private PityMeters() {
    }

    static PityMeter of(PityCategoryId category, String key) {
        return switch (category) {
            case CRIMSON_ISLE -> key.equals("VANQUISHER") ? vanquisher() : trophyFish(key);
            case THE_END -> single(ZealotPity::meter, SpecialZealotOdds.PITY_KILLS);
            case GARDEN -> key.equals("OVERCLOCKER_3000") ? overclocker() : PityMeter.UNAVAILABLE;
            case GLACITE_TUNNELS -> single(player -> MineshaftPity.displayed(MineshaftService.pityCounter(player)), MineshaftPity.START);
            case HOPPITYS_HUNT -> key.startsWith("MYTHIC") ? rabbits(RabbitRarity.MYTHIC) : rabbits(RabbitRarity.DIVINE);
            case MOONGLADE_MARSH -> switch (key) {
                case "SWAMP_THE_FISH" -> single(player -> FishFamilyDrops.treasureCatches(player, ItemType.SWAMP_THE_FISH), TREASURE_PITY);
                case "MOB_THE_FISH" -> single(MobTheFish::kills, MobTheFish.PITY_KILLS);
                default -> PityMeter.UNAVAILABLE;
            };
            case TORRHUS_CANYON -> single(player -> FishFamilyDrops.treasureCatches(player, ItemType.FISH_THE_FISH), TREASURE_PITY);
            case LOTUS_ATOLL, EVENTS -> PityMeter.UNAVAILABLE;
        };
    }

    private static PityMeter single(Function<SkyBlockPlayer, Number> current, long max) {
        return player -> List.of(PityProgress.line("Progress", current.apply(player).longValue(), max));
    }

    private static PityMeter vanquisher() {
        return single(VanquisherSpawns::killsSinceLast, VANQUISHER_PITY);
    }

    private static PityMeter trophyFish(String key) {
        boolean known = FishingRegistry.getTrophyFish().stream().anyMatch(fish -> fish.id().equals(key));
        if (!known) return PityMeter.UNAVAILABLE;
        return player -> {
            FishProgress progress = player.getTrophyFishData().getProgress(key);
            return List.of(
                    tier("<6><l>GOLD", progress, TrophyTier.GOLD, TrophyFishRoller.GOLD_PITY),
                    tier("<b><l>DIAMOND", progress, TrophyTier.DIAMOND, TrophyFishRoller.DIAMOND_PITY));
        };
    }

    private static String tier(String label, FishProgress progress, TrophyTier tier, int max) {
        String heading = "Progress to " + label + "<r><7>";
        if (progress.hasTier(tier.name())) return "<7>" + heading + ": <a>Already caught";
        return PityProgress.line(heading, progress.getTotalCatches(), max);
    }

    private static PityMeter rabbits(RabbitRarity rarity) {
        boolean mythic = rarity == RabbitRarity.MYTHIC;
        return player -> {
            ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
            List<String> lines = new ArrayList<>();
            lines.add(PityProgress.line("Progress", mythic ? data.getMythicPity() : data.getDivinePity(),
                    mythic ? RabbitRoller.MYTHIC_PITY : RabbitRoller.DIVINE_PITY));
            int needed = RabbitRoller.unlockLevel(rarity);
            if (data.getFactoryLevel() < needed) lines.add("<8>(needs Chocolate Factory " + needed + ")");
            return lines;
        };
    }

    private static PityMeter overclocker() {
        return player -> {
            OverclockerPity pity = Gardens.profile(player).overclockerPity();
            List<String> lines = new ArrayList<>();
            for (OverclockerPity.Source source : OverclockerPity.Source.values()) {
                String label = "Progress from " + OVERCLOCKER_LABELS.get(source);
                lines.add(source.active() ? PityProgress.percentLine(label, pity.progress(source))
                        : "<7>" + label + ": " + PityProgress.UNAVAILABLE);
            }
            return lines;
        };
    }
}
