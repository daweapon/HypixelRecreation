package dev.daweapon.type.skyblockgeneric.achievement;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.generic.achievement.PlayerAchievementHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import dev.daweapon.type.skyblockgeneric.user.pets.PetScore;

import java.util.HashSet;
import java.util.Set;

public final class PetAchievements {
    private static final int ZOO_SIZE = 20;
    private static final int CARETAKER_LEVEL = 80;
    private static final int FRIEND_LEVEL = 100;
    private static final int KING_SCORE = 100;

    private PetAchievements() {
    }

    public static void refresh(SkyBlockPlayer player) {
        PlayerAchievementHandler achievements = player.getAchievementHandler();
        Set<ItemType> types = new HashSet<>();
        boolean legendary = false;
        int highestLevel = 0;
        for (SkyBlockItem pet : player.getPetData().getPetsMap().keySet()) {
            types.add(pet.getAttributeHandler().getPotentialType());
            if (pet.getAttributeHandler().getRarity().ordinal() >= Rarity.LEGENDARY.ordinal()) legendary = true;
            highestLevel = Math.max(highestLevel, PetLevels.level(pet));
        }
        if (types.size() >= ZOO_SIZE) achievements.completeAchievement("skyblock.the_real_zoo_shady");
        if (legendary) achievements.completeAchievement("skyblock.fully_evolved");
        if (highestLevel >= CARETAKER_LEVEL) achievements.completeAchievement("skyblock.caretaker");
        if (highestLevel >= FRIEND_LEVEL) achievements.completeAchievement("skyblock.friend_for_life");
        if (PetScore.of(player) >= KING_SCORE) achievements.completeAchievement("skyblock.king_of_the_pets");
    }
}
