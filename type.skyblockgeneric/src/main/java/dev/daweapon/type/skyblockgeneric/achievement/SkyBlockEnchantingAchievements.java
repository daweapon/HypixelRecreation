package dev.daweapon.type.skyblockgeneric.achievement;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.generic.achievement.PlayerAchievementHandler;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.stream.Collectors;

public final class SkyBlockEnchantingAchievements {
    private static final int MASTER_ENCHANTER_LEVELS = 64;
    private static final int HIGHER_ENCHANTS_LEVEL = 6;

    private SkyBlockEnchantingAchievements() {
    }

    public static void onAnvilCombine(SkyBlockPlayer player, SkyBlockItem before, SkyBlockItem sacrifice,
                                      SkyBlockItem after, int levelsSpent) {
        PlayerAchievementHandler achievements = player.getAchievementHandler();

        if (!before.getAttributeHandler().isRecombobulated() && after.getAttributeHandler().isRecombobulated()) {
            achievements.completeAchievement("skyblock.upgrades_people_upgrades");
            if (after.getAttributeHandler().getRarity() == Rarity.MYTHIC) achievements.completeAchievement("skyblock.mystical");
            if (before.getAttributeHandler().getPotentialType() == ItemType.ASPECT_OF_THE_JERRY) {
                achievements.completeAchievement("skyblock.seriously");
            }
        }

        ItemType sacrificeType = sacrifice.getAttributeHandler().getPotentialType();
        if (sacrificeType == ItemType.HOT_POTATO_BOOK || sacrificeType == ItemType.FUMING_POTATO_BOOK) {
            achievements.completeAchievement("skyblock.every_little_bit_helps");
        }

        Map<EnchantmentType, Integer> previous = levels(before);
        levels(after).forEach((type, level) -> {
            if (level > previous.getOrDefault(type, 0)) onEnchantmentApplied(player, type, level);
        });
        if (after.getAttributeHandler().getPotentialType() == ItemType.ENCHANTED_BOOK
                && levels(after).values().stream().anyMatch(level -> level >= HIGHER_ENCHANTS_LEVEL)) {
            achievements.completeAchievement("skyblock.higher_enchants");
        }
        if (levelsSpent >= MASTER_ENCHANTER_LEVELS) achievements.completeAchievement("skyblock.master_enchanter");
    }

    public static void onTableEnchant(SkyBlockPlayer player, EnchantmentType type, int level, int levelsSpent) {
        onEnchantmentApplied(player, type, level);
        if (levelsSpent >= MASTER_ENCHANTER_LEVELS) {
            player.getAchievementHandler().completeAchievement("skyblock.master_enchanter");
        }
    }

    private static void onEnchantmentApplied(SkyBlockPlayer player, EnchantmentType type, int level) {
        PlayerAchievementHandler achievements = player.getAchievementHandler();
        if (type == EnchantmentType.FORTUNE) achievements.completeAchievement("skyblock.fortunate");
        if (type.getEnch().isUltimate()) achievements.completeAchievement("skyblock.tough_choice");
    }

    private static Map<EnchantmentType, Integer> levels(SkyBlockItem item) {
        return item.getAttributeHandler().getEnchantments()
                .collect(Collectors.toMap(SkyBlockEnchantment::type, SkyBlockEnchantment::level, Math::max));
    }
}
