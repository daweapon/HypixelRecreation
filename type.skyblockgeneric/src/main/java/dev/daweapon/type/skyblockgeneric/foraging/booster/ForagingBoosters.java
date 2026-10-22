package dev.daweapon.type.skyblockgeneric.foraging.booster;

import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeForagingBoosters;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ForagingBoosters {
    public static final String APPLY_TRIGGER = "booster.apply";
    public static final String THREE_TRIGGER = "booster.three";
    public static final int TRIGGER_COUNT = 3;

    private ForagingBoosters() {
    }

    public static String handlerId(BoosterKind kind, int level) {
        return "BOOSTER_" + kind.name() + (level >= BoosterKind.UNCOMMON ? "_UNCOMMON" : "_COMMON");
    }

    public static void registerAnvilCombines() {
        for (BoosterKind kind : BoosterKind.values()) {
            for (int level : new int[]{BoosterKind.COMMON, BoosterKind.UNCOMMON}) {
                AnvilCombineRegistry.register(handlerId(kind, level), new AnvilCombineHandler(
                        (upgrade, sacrifice) -> apply(upgrade, kind, level),
                        (player, upgrade, sacrifice) -> canApply(upgrade, kind, level),
                        (upgrade, sacrifice, player) -> 0,
                        (player, upgrade, sacrifice) -> {
                            recordApplication(player, upgrade, kind, level);
                            return true;
                        }));
            }
        }
    }

    public static @Nullable BoosterSlot slotOf(SkyBlockItem item) {
        if (item.isNA() || item.isAir()) return null;
        return BoosterSlot.of(item.getAttributeHandler().getTypeAsString());
    }

    public static BoosterLoadout loadout(SkyBlockItem item) {
        ItemAttributeForagingBoosters attribute = (ItemAttributeForagingBoosters) item.getAttribute("foraging_boosters");
        return attribute == null ? BoosterLoadout.EMPTY : BoosterLoadout.parse(attribute.getValue());
    }

    private static void store(SkyBlockItem item, BoosterLoadout loadout) {
        ((ItemAttributeForagingBoosters) item.getAttribute("foraging_boosters")).setValue(loadout.serialize());
    }

    public static boolean canApply(SkyBlockItem item, BoosterKind kind, int level) {
        return slotOf(item) != null && loadout(item).accepts(kind, level);
    }

    public static void apply(SkyBlockItem item, BoosterKind kind, int level) {
        BoosterSlot slot = slotOf(item);
        if (slot == null) return;
        BoosterLoadout loadout = loadout(item);
        ItemStatistic statistic = kind.statistic();
        if (statistic != null) {
            ItemStatistics extra = item.getAttributeHandler().getExtraDynamicStatistics()
                    .addBase(statistic, -kind.amount(loadout.level(kind), slot))
                    .addBase(statistic, kind.amount(level, slot));
            item.getAttributeHandler().setExtraDynamicStatistics(extra);
        }
        store(item, loadout.with(kind, level));
    }

    public static void recordApplication(SkyBlockPlayer player, SkyBlockItem before, BoosterKind kind, int level) {
        ChapterTriggers.fire(player, APPLY_TRIGGER);
        if (loadout(before).with(kind, level).count() >= TRIGGER_COUNT) ChapterTriggers.fire(player, THREE_TRIGGER);
        player.sendMessage("<a>Applied the <9>{} Booster <a>to your item!", kind.displayName());
    }

    public static List<String> describe(SkyBlockItem item) {
        BoosterSlot slot = slotOf(item);
        List<String> lines = new ArrayList<>();
        if (slot == null) return lines;
        loadout(item).asMap().forEach((kind, level) -> lines.add("<9>" + kind.displayName() + " Booster <7>("
                + (level >= BoosterKind.UNCOMMON ? "<a>Uncommon" : "<f>Common") + "<7>)"));
        return lines;
    }

    public static double fightingPercent(SkyBlockPlayer player) {
        double total = 0;
        for (SkyBlockItem item : worn(player)) {
            BoosterSlot slot = slotOf(item);
            if (slot == null) continue;
            total += BoosterKind.FIGHTING.amount(loadout(item).level(BoosterKind.FIGHTING), slot);
        }
        return total;
    }

    public static double fightingMultiplier(SkyBlockPlayer player) {
        if (!HotfService.onForagingIsland() || player.getSkyblockDataHandler() == null) return 1;
        return 1 + fightingPercent(player) / 100D;
    }

    private static List<SkyBlockItem> worn(SkyBlockPlayer player) {
        List<SkyBlockItem> items = new ArrayList<>();
        PlayerItemOrigin.OriginCache cache = PlayerItemOrigin.getFromCache(player.getUuid());
        for (PlayerItemOrigin origin : List.of(PlayerItemOrigin.HELMET, PlayerItemOrigin.CHESTPLATE, PlayerItemOrigin.LEGGINGS, PlayerItemOrigin.BOOTS)) {
            SkyBlockItem item = cache.get(origin);
            if (item != null) items.add(item);
        }
        DatapointInventory inventory = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class);
        for (UnderstandableSkyBlockItem serialized : List.of(inventory.getValue().getNecklace(), inventory.getValue().getCloak(),
                inventory.getValue().getBelt(), inventory.getValue().getGloves())) {
            items.add(new SkyBlockItem(serialized));
        }
        items.add(new SkyBlockItem(player.getItemInMainHand()));
        return items;
    }
}
