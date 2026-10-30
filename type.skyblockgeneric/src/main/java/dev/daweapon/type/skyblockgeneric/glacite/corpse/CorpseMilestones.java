package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointFrozenCorpses;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public final class CorpseMilestones {
    private static List<CorpseMilestone> milestones;

    private CorpseMilestones() {
    }

    @SuppressWarnings("unchecked")
    public static synchronized List<CorpseMilestone> all() {
        if (milestones == null) {
            List<CorpseMilestone> loaded = new ArrayList<>();
            for (Map<String, Object> raw : (List<Map<String, Object>>) CorpseFiles.read("milestones.yml").get("milestones")) {
                Object item = raw.get("reward_item");
                loaded.add(new CorpseMilestone(number(raw, "tier"), number(raw, "lapis"), number(raw, "umber"), number(raw, "tungsten"),
                        number(raw, "vanguard"), number(raw, "powder"), number(raw, "rng_xp"), number(raw, "skyblock_xp"),
                        item == null ? null : ItemType.valueOf(item.toString()), number(raw, "bonus_corpse"), number(raw, "bonus_roll")));
            }
            milestones = List.copyOf(loaded);
        }
        return milestones;
    }

    private static int number(Map<String, Object> raw, String key) {
        Object value = raw.get(key);
        return value == null ? 0 : ((Number) value).intValue();
    }

    public static int highestReached(ToIntFunction<CorpseType> looted) {
        int tier = 0;
        for (CorpseMilestone milestone : all()) {
            if (!milestone.reached(looted)) break;
            tier = milestone.tier();
        }
        return tier;
    }

    public static double bonusCorpsePercent(int claimed) {
        return all().stream().filter(milestone -> milestone.tier() <= claimed).mapToDouble(CorpseMilestone::bonusCorpse).sum();
    }

    public static double bonusRollPercent(int claimed) {
        return all().stream().filter(milestone -> milestone.tier() <= claimed).mapToDouble(CorpseMilestone::bonusRoll).sum();
    }

    public static int skyBlockXp(int claimed) {
        return all().stream().filter(milestone -> milestone.tier() <= claimed).mapToInt(CorpseMilestone::skyBlockXp).sum();
    }

    public static int claimed(SkyBlockPlayer player) {
        return CorpseProgress.data(player).getMilestonesClaimed();
    }

    public static List<Text> claimReached(SkyBlockPlayer player) {
        DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
        if (!data.isMetWolf()) return List.of();
        List<Text> lines = new ArrayList<>();
        ToIntFunction<CorpseType> looted = type -> data.lootedCount(type.name());
        for (CorpseMilestone milestone : all()) {
            if (milestone.tier() <= data.getMilestonesClaimed()) continue;
            if (!milestone.reached(looted)) break;
            lines.addAll(grant(player, data, milestone));
        }
        CorpseProgress.save(player, data);
        syncXp(player);
        return lines;
    }

    private static List<Text> grant(SkyBlockPlayer player, DatapointFrozenCorpses.FrozenCorpseData data, CorpseMilestone milestone) {
        data.setMilestonesClaimed(milestone.tier());
        data.setRngXp(data.getRngXp() + milestone.rngXp());
        HotmService.addFlatPowder(player, HotmPowder.GLACITE, milestone.powder());
        if (milestone.rewardItem() != null) player.addAndUpdateItem(new SkyBlockItem(milestone.rewardItem()));
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<b><l>FROZEN CORPSE MILESTONE TIER {}", StringUtility.getAsRomanNumeral(milestone.tier())));
        if (milestone.rewardItem() != null) lines.add(Text.of("  <8>+{}", new SkyBlockItem(milestone.rewardItem()).getDisplayNameText()));
        if (milestone.bonusCorpse() > 0) lines.add(Text.of("  <8>+<a>{}% <f>chance for <a>+1 <b>Frozen Corpse", (int) milestone.bonusCorpse()));
        if (milestone.bonusRoll() > 0) lines.add(Text.of("  <8>+<a>{}% <f>chance for <a>+1 <f>item per corpse", (int) milestone.bonusRoll()));
        lines.add(Text.of("  <8>+<b>{} Glacite Powder", String.format("%,d", milestone.powder())));
        lines.add(Text.of("  <8>+<9>{} <b>Frozen Corpse RNG Meter <9>XP", String.format("%,d", milestone.rngXp())));
        lines.add(Text.of("  <8>+<b>{} SkyBlock XP", milestone.skyBlockXp()));
        return lines;
    }

    public static void syncXp(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.FROZEN_CORPSE_MILESTONES, skyBlockXp(claimed(player)));
    }

    public static void maxOut(SkyBlockPlayer player) {
        DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
        data.setMetWolf(true);
        for (CorpseMilestone milestone : all()) {
            if (milestone.tier() <= data.getMilestonesClaimed()) continue;
            for (CorpseType type : CorpseType.values()) {
                data.getLooted().merge(type.name(), milestone.required(type), Math::max);
            }
            data.setMilestonesClaimed(milestone.tier());
        }
        CorpseProgress.save(player, data);
        syncXp(player);
    }

    public static boolean completeNext(SkyBlockPlayer player) {
        DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
        if (data.getMilestonesClaimed() >= all().size()) return false;
        CorpseMilestone next = all().get(data.getMilestonesClaimed());
        data.setMetWolf(true);
        for (CorpseType type : CorpseType.values()) data.getLooted().merge(type.name(), next.required(type), Math::max);
        CorpseProgress.save(player, data);
        grantAndNotify(player, next);
        return true;
    }

    private static void grantAndNotify(SkyBlockPlayer player, CorpseMilestone milestone) {
        DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
        List<Text> lines = grant(player, data, milestone);
        CorpseProgress.save(player, data);
        syncXp(player);
        lines.forEach(line -> player.sendMessage(line));
    }
}
