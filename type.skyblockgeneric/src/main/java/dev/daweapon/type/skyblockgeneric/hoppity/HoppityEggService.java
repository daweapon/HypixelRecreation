package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc.MiscPetHooks;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import dev.daweapon.type.skyblockgeneric.rabbits.HoppityEggType;
import dev.daweapon.type.skyblockgeneric.rabbits.RabbitShardBonuses;
import dev.daweapon.type.skyblockgeneric.region.IslandType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class HoppityEggService {
    public static final int UNIQUE_LOCATIONS_FOR_RESIDENT = 15;
    public static final long STRAY_GUARANTEE_MS = 30_000L;
    private static final int KEPT_CLAIMED_SPAWNS = 40;
    private static final double MAX_DUPLICATE_STACKS = 10;

    private HoppityEggService() {
    }

    public static boolean claim(SkyBlockPlayer player, ActiveEgg egg) {
        ChocolateFactoryData current = ChocolateFactoryHelper.getData(player);
        if (current.hasClaimedSpawn(egg.spawnId())) return false;
        boolean[] first = {false};
        ChocolateFactoryHelper.mutateData(player, data -> {
            if (!data.getClaimedSpawns().add(egg.spawnId())) return;
            first[0] = true;
            pruneClaimed(data, egg);
            data.setTotalEggsFound(data.getTotalEggsFound() + 1);
        });
        if (!first[0]) return false;
        player.sendMessage("<d><l>HOPPITY'S HUNT</l> <d>You found a " + typeColor(egg.type()) + "{} <f>{}<d>!", egg.type().getDisplayName(),
                egg.location().message());
        open(player, egg.location(), egg.island(), ThreadLocalRandom.current());
        ChocolateFactoryHelper.mutateData(player, data ->
                data.setStrayDueAt(System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(STRAY_GUARANTEE_MS / 2, STRAY_GUARANTEE_MS)));
        return true;
    }

    public static void open(SkyBlockPlayer player, EggLocation location, IslandType rollIsland, RandomGenerator random) {
        ChocolateFactoryHelper.updateProduction(player);
        RabbitRoller.Result[] rolled = {null};
        double[] outcome = {0};
        ChocolateFactoryHelper.mutateData(player, data -> {
            if (location != null && data.getUniqueEggLocations().add(location.key())) {
                awardResidentRabbit(player, data, location.island());
            }
            RabbitRoller.Result result = RabbitRoller.roll(context(player, data, rollIsland), random);
            if (result == null) return;
            rolled[0] = result;
            data.setMythicPity(result.mythicPity());
            data.setDivinePity(result.divinePity());
            outcome[0] = applyRabbit(player, data, result.rabbit());
        });
        if (rolled[0] != null) announce(player, rolled[0].rabbit(), outcome[0]);
    }

    public static void award(SkyBlockPlayer player, ChocolateRabbit rabbit) {
        ChocolateFactoryHelper.updateProduction(player);
        double[] outcome = {0};
        ChocolateFactoryHelper.mutateData(player, data -> outcome[0] = applyRabbit(player, data, rabbit));
        announce(player, rabbit, outcome[0]);
    }

    public static RabbitRoller.Context context(SkyBlockPlayer player, ChocolateFactoryData data, IslandType island) {
        int year = SkyBlockCalendar.getYear();
        return new RabbitRoller.Context(player.getUuid(), data.getFactoryLevel(), data.getRabbitShrineLevel(),
                data.hasFoundRabbit(ChocolateRabbit.NAPOLEON), RabbitShardBonuses.legendaryLuck(player),
                RabbitShardBonuses.mythicLuck(player), RabbitShardBonuses.divineLuck(player),
                unobtainedBonusPercent(player), island, data.getFoundRabbits(), data.getMythicPity(), data.getDivinePity(),
                year, EggLocationCatalogue.get().islands());
    }

    public static double unobtainedBonusPercent(SkyBlockPlayer player) {
        return dev.daweapon.type.skyblockgeneric.accessory.AccessoryEconomyHooks.unobtainedRabbitPercent(player);
    }

    private static double applyRabbit(SkyBlockPlayer player, ChocolateFactoryData data, ChocolateRabbit rabbit) {
        boolean duplicate = data.hasFoundRabbit(rabbit);
        if (!duplicate && !data.isBarnFull()) {
            data.addFoundRabbit(rabbit);
            return -1;
        }
        if (duplicate) data.addDuplicate(rabbit);
        long chocolate = duplicateChocolate(data, rabbit, MiscPetHooks.chocolateDuplicatePercent(player),
                sweetBenevolence());
        data.addChocolate(chocolate);
        return duplicate ? chocolate : -chocolate - 2;
    }

    private static double sweetBenevolence() {
        return HuntEggSchedule.isHuntActive(SkyBlockCalendar.getElapsed())
                ? ElectionPerks.multiplier(SkyBlockMayor.Perk.SWEET_BENEVOLENCE) : 1.0;
    }

    public static long duplicateReward(SkyBlockPlayer player, ChocolateFactoryData data, ChocolateRabbit rabbit) {
        data.addDuplicate(rabbit);
        long chocolate = duplicateChocolate(data, rabbit, MiscPetHooks.chocolateDuplicatePercent(player), sweetBenevolence());
        data.addChocolate(chocolate);
        return chocolate;
    }

    public static long duplicateChocolate(ChocolateFactoryData data, ChocolateRabbit rabbit, double petPercent, double perkMultiplier) {
        double stacks = Math.min(data.getDuplicates(rabbit), MAX_DUPLICATE_STACKS);
        double bonus = (data.hasFoundRabbit(ChocolateRabbit.KING) ? 0.30 : 0) + petPercent / 100.0;
        double chocolate = data.getChocolatePerSecond() * baseSeconds(rabbit.getRarity()) * (1 + stacks * 0.1) * (1 + bonus) * perkMultiplier;
        return Math.max(1L, (long) chocolate);
    }

    public static int baseSeconds(RabbitRarity rarity) {
        return switch (rarity) {
            case COMMON -> 150;
            case UNCOMMON -> 300;
            case RARE -> 750;
            case EPIC -> 1500;
            case LEGENDARY -> 4000;
            case MYTHIC -> 8000;
            case DIVINE -> 15000;
        };
    }

    private static void awardResidentRabbit(SkyBlockPlayer player, ChocolateFactoryData data, IslandType island) {
        if (data.uniqueLocationsOn(island.name()) < UNIQUE_LOCATIONS_FOR_RESIDENT) return;
        for (ChocolateRabbit rabbit : ChocolateRabbit.values()) {
            if (rabbit.isUniqueLocationRabbit() && rabbit.getLocation() == island && !data.hasFoundRabbit(rabbit)) {
                data.addFoundRabbit(rabbit);
                player.sendMessage("<d><l>NEW RABBIT! <7>You found all {} egg locations on {}<7>: {}",
                        UNIQUE_LOCATIONS_FOR_RESIDENT, island.getName(), rabbit.getFormattedName());
            }
        }
    }

    private static void announce(SkyBlockPlayer player, ChocolateRabbit rabbit, double outcome) {
        if (outcome == -1) {
            player.sendMessage("<d><l>NEW RABBIT! <7>{} " + bonusText(rabbit), rabbit.getFormattedName());
        } else if (outcome <= -2) {
            player.sendMessage("<c><l>BARN FULL! <7>{} <7>was crushed for <6>+{} Chocolate<7>!", rabbit.getFormattedName(),
                    ChocolateFactoryHelper.formatChocolate((long) (-outcome - 2)));
        } else {
            player.sendMessage("<d><l>DUPLICATE RABBIT! <7>{} <6>+{} Chocolate", rabbit.getFormattedName(),
                    ChocolateFactoryHelper.formatChocolate((long) outcome));
        }
    }

    private static String bonusText(ChocolateRabbit rabbit) {
        List<String> parts = new ArrayList<>();
        if (rabbit.getChocolateBonus() > 0) parts.add("<6>+" + rabbit.getChocolateBonus() + " Chocolate per second");
        if (rabbit.getMultiplierBonus() > 0) parts.add("<6>+" + String.format("%.3fx", rabbit.getMultiplierBonus()) + " Chocolate");
        return String.join(" <7>and ", parts);
    }

    private static void pruneClaimed(ChocolateFactoryData data, ActiveEgg egg) {
        if (data.getClaimedSpawns().size() <= KEPT_CLAIMED_SPAWNS) return;
        String prefix = egg.year() + ":";
        data.getClaimedSpawns().removeIf(id -> !id.startsWith(prefix)
                || egg.spawnNumber() - Integer.parseInt(id.substring(prefix.length())) > KEPT_CLAIMED_SPAWNS);
    }

    public static String typeColor(HoppityEggType type) {
        return switch (type) {
            case BREAKFAST, BRUNCH -> "<6>";
            case LUNCH, DEJEUNER -> "<9>";
            case DINNER, SUPPER -> "<a>";
            case HITMAN -> "<c>";
        };
    }
}
