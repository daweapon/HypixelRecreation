package net.swofty.type.skyblockgeneric.experiments;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.consumable.TimedConsumables;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public enum ExperienceBottle {
    EXPERIENCE_BOTTLE(ItemType.EXPERIENCE_BOTTLE, 8),
    GRAND(ItemType.GRAND_EXP_BOTTLE, 1_500),
    TITANIC(ItemType.TITANIC_EXP_BOTTLE, 250_000),
    COLOSSAL(ItemType.COLOSSAL_EXP_BOTTLE, 500_000);

    private final ItemType itemType;
    private final long baseOrbs;

    ExperienceBottle(ItemType itemType, long baseOrbs) {
        this.itemType = itemType;
        this.baseOrbs = baseOrbs;
    }

    public ItemType itemType() {
        return itemType;
    }

    public long baseOrbs() {
        return baseOrbs;
    }

    public long orbsFor(int enchantingLevel) {
        return EnchantingXp.orbsFromBottle(baseOrbs, enchantingLevel);
    }

    public long orbsFor(SkyBlockPlayer player) {
        return orbsFor(player.getSkills().getCurrentLevel(SkillCategories.ENCHANTING));
    }

    public void apply(SkyBlockPlayer player) {
        VanillaExperience.add(player, orbsFor(player));
    }

    public static void registerInteractables() {
        for (ExperienceBottle bottle : values()) {
            InteractableRegistry.register(bottle.itemType.name(), InteractableItemConfig.builder()
                    .rightClickHandler((player, item) -> {
                        long orbs = bottle.orbsFor(player);
                        bottle.apply(player);
                        TimedConsumables.consumeHeld(player, item);
                        player.sendMessage("<a>You smashed open the bottle and received <b>{}<a> experience!", String.format("%,d", orbs));
                    }).build());
        }
    }
}
