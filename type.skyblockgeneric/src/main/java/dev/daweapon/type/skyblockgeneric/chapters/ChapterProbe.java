package dev.daweapon.type.skyblockgeneric.chapters;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointTreeGifts;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeRegistry;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;

@FunctionalInterface
public interface ChapterProbe {
    int MAX_ATTRIBUTE_LEVEL = 10;

    long value(SkyBlockPlayer player);

    static String shardTrigger(String shardName) {
        return "shard." + shardName.toLowerCase().replace(' ', '_');
    }

    static ChapterProbe parse(String spec) {
        int split = spec.indexOf(':');
        String kind = split < 0 ? spec : spec.substring(0, split);
        String argument = split < 0 ? "" : spec.substring(split + 1);
        return switch (kind) {
            case "treegifts" -> player -> player.getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.TREE_GIFTS, DatapointTreeGifts.class).count(argument);
            case "shards" -> shards(List.of(argument.split(",")));
            case "items" -> items(Arrays.stream(argument.split(",")).map(ChapterProbe::item).toList());
            case "worn" -> worn(Arrays.stream(argument.split(",")).map(ChapterProbe::item).toList());
            case "stat" -> stat(ItemStatistic.valueOf(argument));
            case "hotftier" -> HotfService::tier;
            case "attrmax" -> attributeMaxed(AttributeDefinition.AttributeSkill.valueOf(argument));
            default -> throw new IllegalArgumentException("Unknown chapter probe " + spec);
        };
    }

    private static ItemType item(String name) {
        return ItemType.valueOf(name);
    }

    private static ChapterProbe shards(List<String> names) {
        List<AttributeDefinition> definitions = names.stream().map(name -> AttributeRegistry.findByShard(name)
                .orElseThrow(() -> new IllegalArgumentException("Unknown shard " + name))).toList();
        return player -> {
            long owned = 0;
            var data = player.getHuntingData();
            for (AttributeDefinition definition : definitions) {
                boolean has = data.shardCount(definition.id()) > 0
                        || ChapterService.data(player).trigger(shardTrigger(definition.shard())) > 0;
                if (has) owned++;
            }
            return owned;
        };
    }

    private static ChapterProbe items(List<ItemType> types) {
        return player -> types.stream().filter(type -> player.countItem(type) > 0).count();
    }

    private static ChapterProbe worn(List<ItemType> types) {
        return player -> {
            long worn = 0;
            for (SkyBlockItem piece : player.getArmor()) {
                if (piece == null) continue;
                ItemType type = piece.getAttributeHandler().getPotentialType();
                if (type != null && types.contains(type)) worn++;
            }
            return worn;
        };
    }

    private static ChapterProbe stat(ItemStatistic statistic) {
        return player -> (long) Math.floor(player.getStatistics().allStatistics().getOverall(statistic));
    }

    private static ChapterProbe attributeMaxed(AttributeDefinition.AttributeSkill skill) {
        return player -> AttributeRegistry.values().stream()
                .filter(definition -> definition.skill() == skill)
                .anyMatch(definition -> player.getHuntingData().level(definition.id()) >= MAX_ATTRIBUTE_LEVEL) ? 1 : 0;
    }
}
