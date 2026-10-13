package net.swofty.type.skyblockgeneric.garden.feast;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.chip.ChipService;
import net.swofty.type.skyblockgeneric.garden.chip.GardenChip;
import net.swofty.type.skyblockgeneric.garden.pest.PestRewards;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class FeastService {
    public static final double HARVEST_RARE_CROP_PERCENT = 0.006;
    public static final double PEST_RARE_CROP_PERCENT = 15;
    public static final double MOUSE_RARE_CROP_PERCENT = 30;
    public static final double SEASONING_PERCENT = 0.04;
    public static final int[] NORMAL_TARGETS = {5, 25, 75, 150, 250};
    public static final int[] NORMAL_FLASKS = {1, 1, 2, 2, 2};
    public static final int[] GRAND_TARGETS = {5, 25, 75, 150, 250, 350, 450, 550, 750};
    public static final int[] GRAND_FLASKS = {1, 1, 2, 2, 2, 2, 2, 2, 2};
    public static final int[] GRAND_KERNELS = {25, 50, 75, 100, 125, 150, 175, 200, 225};
    public static final int TICKETS_PER_MILESTONE = 5;

    private FeastService() {
    }

    public static double overbloom(SkyBlockPlayer player) {
        return player.getStatistics().allStatistics().getOverall(ItemStatistic.OVERBLOOM)
                + ChipService.bonus(player, GardenChip.RAREFINDER);
    }

    public static double overbloomFactor(SkyBlockPlayer player) {
        return 1 + Math.max(0, overbloom(player)) / 100.0;
    }

    public static double feastFortune(SkyBlockPlayer player) {
        return FeastCalendar.active() ? Carnivals.perkValue(player, CarnivalPerk.FORTUNATE_FEASTING) : 0;
    }

    public static double feastPestChance(SkyBlockPlayer player) {
        return FeastCalendar.active() ? Carnivals.perkValue(player, CarnivalPerk.FEAST_CRASHERS) : 0;
    }

    public static FeastData data(GardenProfile profile) {
        FeastData data = profile.feast();
        data.rollOver(FeastCalendar.year());
        return data;
    }

    public static int[] targets() {
        return FeastCalendar.grand() ? GRAND_TARGETS : NORMAL_TARGETS;
    }

    public static void onHarvest(SkyBlockPlayer player, GardenCrop crop) {
        if (!FeastCalendar.active()) return;
        double factor = overbloomFactor(player);
        if (FeastCalendar.inSeasonNow().contains(crop)) {
            giveRareCrop(player, crop, HARVEST_RARE_CROP_PERCENT * factor);
        }
        double talent = 1 + Carnivals.perkValue(player, CarnivalPerk.NATURAL_TALENT) / 100.0;
        if (ThreadLocalRandom.current().nextDouble() * 100 < SEASONING_PERCENT * factor * talent) addSeasoning(player, 1);
    }

    public static void onPestKill(SkyBlockPlayer player, GardenCrop crop, boolean fieldMouse) {
        if (!FeastCalendar.active()) return;
        double factor = overbloomFactor(player);
        List<GardenCrop> inSeason = FeastCalendar.inSeasonNow();
        if (fieldMouse) {
            GardenCrop pick = inSeason.get(ThreadLocalRandom.current().nextInt(inSeason.size()));
            giveRareCrop(player, pick, MOUSE_RARE_CROP_PERCENT * factor);
        } else if (crop != null && inSeason.contains(crop)) {
            giveRareCrop(player, crop, PEST_RARE_CROP_PERCENT * factor);
        }
    }

    private static void giveRareCrop(SkyBlockPlayer player, GardenCrop crop, double percent) {
        String id = FeastCalendar.rareCrop(crop);
        ItemType item = id == null ? null : ItemType.get(id);
        if (item == null || !LootRoll.roll(player.getUuid(), item, percent, ThreadLocalRandom.current())) return;
        SkyBlockItem reward = new SkyBlockItem(item, 1);
        PestRewards.give(player, reward);
        player.sendMessage(Text.of("<6><l>RARE CROP!</l> {}", reward.getDisplayNameText()));
    }

    public static void addSeasoning(SkyBlockPlayer player, int amount) {
        GardenProfile profile = Gardens.profile(player);
        FeastData data = data(profile);
        data.setSeasoning(data.getSeasoning() + amount);
        player.sendMessage(Text.of("<2>You found <a>Seasoning<2>! It was donated to the Communal Stew. <8>({} total)", data.getSeasoning()));
        if (claimable(profile)) player.sendMessage(Text.of("<6>A Harvest Feast milestone is ready to claim at Feast Chef Ted!"));
        Gardens.save(player, profile);
    }

    public static boolean claimable(GardenProfile profile) {
        FeastData data = data(profile);
        int[] targets = targets();
        return data.getClaimed() < targets.length && data.getSeasoning() >= targets[data.getClaimed()];
    }

    public static int flasksFor(SkyBlockPlayer player, int tier) {
        int[] flasks = FeastCalendar.grand() ? GRAND_FLASKS : NORMAL_FLASKS;
        int count = flasks[tier];
        if (tier == flasks.length - 1) count += (int) Carnivals.perkValue(player, CarnivalPerk.LEFTOVERS);
        return count;
    }

    public static boolean claim(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        if (!claimable(profile)) return false;
        FeastData data = data(profile);
        int tier = data.getClaimed();
        boolean grand = FeastCalendar.grand();
        int last = targets().length - 1;
        int flaskCount = flasksFor(player, tier);
        data.setClaimed(tier + 1);
        ItemType flask = ItemType.get("FEAST_FLASK");
        if (flask != null) player.addAndUpdateItem(new SkyBlockItem(flask, flaskCount));
        ItemType tickets = ItemType.CARNIVAL_TICKET;
        player.addAndUpdateItem(new SkyBlockItem(tickets, TICKETS_PER_MILESTONE));
        if (grand) data.setKernels(data.getKernels() + GRAND_KERNELS[tier]);
        if (tier == last && !grand) {
            ItemType jar = ItemType.get("COMMEMORATIVE_MASON_JAR");
            if (jar != null) player.addAndUpdateItem(new SkyBlockItem(jar, 1));
        }
        Gardens.save(player, profile);
        GardenService.syncExperience(player);
        String kernels = grand ? "<6>, <e>" + GRAND_KERNELS[tier] + " Kernels" : "";
        player.sendMessage(Text.of("<6>You claimed Harvest Feast milestone <e>{}<6>: <5>{}x Feast Flask<6>, <a>{} Carnival Tickets" + kernels,
                net.swofty.commons.StringUtility.getAsRomanNumeral(tier + 1), flaskCount, TICKETS_PER_MILESTONE));
        return true;
    }
}
