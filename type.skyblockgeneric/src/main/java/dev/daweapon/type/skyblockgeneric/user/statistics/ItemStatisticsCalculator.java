package dev.daweapon.type.skyblockgeneric.user.statistics;

import java.util.*;
import net.minestom.server.entity.LivingEntity;
import dev.daweapon.commons.skyblock.item.PotatoType;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributeHotPotatoBookData;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.crimson.CrimsonGear;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.PlayerContextEnchant;
import dev.daweapon.type.skyblockgeneric.gems.Gemstone;
import dev.daweapon.type.skyblockgeneric.gifts.SnowSuit;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.mythological.gear.MythologicalGear;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class ItemStatisticsCalculator {
    private final SkyBlockPlayer player;

    ItemStatisticsCalculator(SkyBlockPlayer player) {
        this.player = player;
    }

    ItemStatistics calculateExtraItemStatisticsToAdd(SkyBlockItem item, SkyBlockPlayer causer, LivingEntity enemy) {
        ItemStatistics statistics = ItemStatistics.builder().build();
        statistics = getReforgeStatistics(item, statistics);
        statistics = getGemstoneStatistics(item, statistics);
        statistics = getEnchantStatistics(item, statistics, causer, enemy);
        statistics = getHotPotatoBookStatistics(item, statistics);
        statistics = ItemStatistics.add(statistics, MythologicalGear.extraItemStatistics(item, player));
        statistics = ItemStatistics.add(statistics, SnowSuit.extraItemStatistics(item, player));
        statistics = ItemStatistics.add(statistics, dev.daweapon.type.skyblockgeneric.technoblade.ArtOfWar.statistics(item));
        statistics = ItemStatistics.add(statistics, CrimsonGear.extraItemStatistics(item, player));
        statistics = ItemStatistics.add(statistics, ReforgeLoadout.statistics(item, player));
        statistics = ItemStatistics.add(statistics, dev.daweapon.type.skyblockgeneric.item.tool.ToolPassives.extraItemStatistics(item, player));
        statistics = ItemStatistics.add(statistics, dev.daweapon.type.skyblockgeneric.item.armor.ArmorPassives.extraItemStatistics(item, player));
        statistics = ItemStatistics.add(statistics, dev.daweapon.type.skyblockgeneric.item.armor.PieceBonuses.extraItemStatistics(item, player));
        statistics = ItemStatistics.add(statistics, dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityEffects.pieceStatistics(item, player));
        statistics = ItemStatistics.add(statistics, dev.daweapon.type.skyblockgeneric.slayer.zombie.ZombieSlayerGear.pieceStatistics(item));

        return statistics;
    }

    ItemStatistics getReforgeStatistics(SkyBlockItem item, ItemStatistics statistics) {
        if (item.getAttributeHandler().getReforge() != null) {
            statistics = item.getAttributeHandler().getReforge().getAfterCalculation(statistics,
                    item.getAttributeHandler().getRarity().ordinal() + 1);
        }
        return statistics;
    }

    ItemStatistics getHotPotatoBookStatistics(SkyBlockItem item, ItemStatistics statistics) {
        ItemAttributeHotPotatoBookData.HotPotatoBookData hotPotatoBookData = item.getAttributeHandler().getHotPotatoBookData();
        if (hotPotatoBookData.hasAppliedItem()) {
            ItemStatistics.Builder toAdd = ItemStatistics.builder();
            PotatoType potatoType = hotPotatoBookData.getPotatoType();

            int appliedBooks = hotPotatoBookData.getTotalAmount();
            double potatoMultiplier = dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks.hotPotatoMultiplier(player);
            potatoType.stats.forEach((statistic, amount) ->
                    toAdd.withBase(statistic, amount * appliedBooks * potatoMultiplier));
            statistics = ItemStatistics.add(statistics, toAdd.build());
        }
        return statistics;
    }

    ItemStatistics getEnchantStatistics(SkyBlockItem item, ItemStatistics statistics,
                                                SkyBlockPlayer causer, LivingEntity enemy) {
        for (SkyBlockEnchantment enchantment : item.getAttributeHandler().getEnchantments().toList()) {
            ItemStatistics enchantmentStatistics = enchantment.type().getEnch().getStatistics(enchantment.level()).clone();
            statistics = ItemStatistics.add(statistics, enchantmentStatistics);
            if (enchantment.type().getEnch() instanceof PlayerContextEnchant contextual) {
                statistics = ItemStatistics.add(statistics, contextual.getStatistics(player, enchantment.level()));
            }

            if (causer != null && enemy != null) {
                if (enchantment.type().getEnch() instanceof EventBasedEnchant eventBasedStatistic) {
                    statistics = ItemStatistics.add(statistics, eventBasedStatistic.getStatisticsOnDamage(
                            causer,
                            enemy,
                            enchantment.level()
                    ));
                }
            }
        }
        return statistics;
    }
    ItemStatistics getGemstoneStatistics(SkyBlockItem item, ItemStatistics statistics) {
        for (ItemStatistic statistic : ItemStatistic.values()) {
            int extra = Gemstone.getExtraStatisticFromGemstone(statistic, item);
            if (extra != 0) {
                statistics = statistics.addBase(statistic, (double) extra);
            }
        }
        return statistics;
    }
}
