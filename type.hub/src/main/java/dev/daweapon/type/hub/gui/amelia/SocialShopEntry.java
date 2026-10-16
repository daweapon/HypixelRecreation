package dev.daweapon.type.hub.gui.amelia;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum SocialShopEntry {
    PARKOUR_START_END(ItemType.PARKOUR_CONTROLLER, 2, 1_000, 1),
    PARKOUR_CHECKPOINT(ItemType.PARKOUR_POINT, 1, 1_000, 1),
    PARKOUR_TIMES(ItemType.PARKOUR_TIMES, 1, 2_000, 1),
    SOCIAL_DISPLAY(ItemType.SOCIAL_DISPLAY, 1, 10_000, 2),
    EGG_HUNT(ItemType.EGG_HUNT, 2, 1_000, 3),
    ISLAND_NPC(ItemType.ISLAND_NPC, 1, 50_000, 5),
    TIC_TAC_TOE(ItemType.TIC_TAC_TOE, 1, 2_500, 6),
    FOUR_IN_A_ROW(ItemType.CONNECT_FOUR, 1, 2_500, 7),
    ROCK_PAPER_SHEARS(ItemType.ROCK_PAPER_SHEARS, 1, 10_000, 8),
    SHOWCASE_BLOCK(ItemType.SHOWCASE_BLOCK, 1, 50_000, 10),
    ;

    private final ItemType item;
    private final int amount;
    private final double coinCost;
    private final int requiredSocialLevel;

    public boolean isUnlocked(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.SOCIAL) >= requiredSocialLevel;
    }

    public static Optional<SocialShopEntry> of(ItemType item) {
        return Arrays.stream(values()).filter(entry -> entry.item == item).findFirst();
    }
}
