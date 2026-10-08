package net.swofty.type.skyblockgeneric.user.pets;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.components.PetItemComponent;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * Pet Items: a pet holds at most one, which boosts its stats. Giving a pet a new item returns the
 * one it was holding.
 */
public final class PetItems {
    public static final Set<ItemStatistic> COMBAT_STATISTICS = EnumSet.of(
            ItemStatistic.HEALTH, ItemStatistic.DEFENSE, ItemStatistic.STRENGTH, ItemStatistic.INTELLIGENCE,
            ItemStatistic.CRITICAL_CHANCE, ItemStatistic.CRITICAL_DAMAGE, ItemStatistic.BONUS_ATTACK_SPEED,
            ItemStatistic.ABILITY_DAMAGE, ItemStatistic.TRUE_DEFENSE, ItemStatistic.FEROCITY,
            ItemStatistic.HEALTH_REGENERATION, ItemStatistic.VITALITY, ItemStatistic.MENDING);

    private PetItems() {
    }

    public static void give(SkyBlockPlayer player, SkyBlockItem petItem) {
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        if (pet == null) {
            player.sendMessage("<c>You need to have a pet summoned to give it a Pet Item!");
            return;
        }
        PetItemComponent component = petItem.getComponent(PetItemComponent.class);
        ItemType petType = pet.getAttributeHandler().getPotentialType();
        if (!component.appliesTo(petType)) {
            player.sendMessage("<c>This Pet Item can't be given to this pet!");
            return;
        }

        ItemType given = petItem.getAttributeHandler().getPotentialType();
        ItemType previous = pet.getAttributeHandler().getPetHeldItem();
        if (previous == given) {
            player.sendMessage("<c>Your pet is already holding this item!");
            return;
        }

        pet.getAttributeHandler().setPetHeldItem(given);
        player.setItemInHand(petItem.getAmount() > 1 ? withAmount(petItem, petItem.getAmount() - 1) : null);
        if (previous != null) player.addAndUpdateItem(new SkyBlockItem(previous));

        String petName = "<color:" + pet.getAttributeHandler().getRarity().getColor().asHexString() + ">"
                + pet.getComponent(PetComponent.class).getPetName();
        String itemName = "<color:" + petItem.getAttributeHandler().getRarity().getColor().asHexString() + ">"
                + petItem.getDisplayName();
        player.sendMessage("<a>Your " + petName + " <a>is now holding " + itemName + "<a>.");
    }

    public static @Nullable ItemType heldType(@Nullable SkyBlockItem pet) {
        return pet == null ? null : pet.getAttributeHandler().getPetHeldItem();
    }

    public static @Nullable PetItemComponent held(@Nullable SkyBlockItem pet) {
        if (pet == null) return null;
        ItemType held = pet.getAttributeHandler().getPetHeldItem();
        if (held == null) return null;
        SkyBlockItem item = new SkyBlockItem(held);
        return item.hasComponent(PetItemComponent.class) ? item.getComponent(PetItemComponent.class) : null;
    }

    /**
     * The pet's own stats at its level, including what its held Pet Item adds, but not its
     * abilities. This is what Chimera and the Manticore Claw copy.
     */
    public static ItemStatistics statistics(SkyBlockItem pet) {
        PetComponent component = pet.getComponent(PetComponent.class);
        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);
        ItemStatistics base = ItemStatistics.add(component.getBaseStatistics(),
                ItemStatistics.multiply(component.getPerLevelStatistics(rarity), level));

        PetItemComponent item = held(pet);
        if (item == null) return base;
        ItemStatistics.Builder boosted = ItemStatistics.builder();
        for (ItemStatistic statistic : ItemStatistic.values()) {
            double value = base.getOverall(statistic);
            if (value == 0) continue;
            double percent = item.getAllPetStatisticsBoost() + item.getPetStatisticBoosts().getOrDefault(statistic, 0D);
            if (percent != 0) boosted.withBase(statistic, value * percent / 100D);
        }
        return ItemStatistics.add(ItemStatistics.add(base, boosted.build()), item.getStatistics());
    }

    public static ItemStatistics combatStatistics(ItemStatistics statistics, double fraction) {
        ItemStatistics.Builder copied = ItemStatistics.builder();
        for (ItemStatistic statistic : COMBAT_STATISTICS) {
            double value = statistics.getOverall(statistic);
            if (value != 0) copied.withBase(statistic, value * fraction);
        }
        return copied.build();
    }

    public static boolean isKnockbackImmune(SkyBlockPlayer player) {
        PetItemComponent item = held(player.getPetData().getEnabledPet());
        return item != null && item.isKnockbackImmune();
    }

    public static double expMultiplier(SkyBlockItem pet, SkillCategories skill) {
        PetItemComponent item = held(pet);
        if (item == null || item.getPetExpBoost() == 0) return 1;
        if (item.getPetExpSkill() != null && item.getPetExpSkill() != skill) return 1;
        return 1 + item.getPetExpBoost() / 100D;
    }

    private static SkyBlockItem withAmount(SkyBlockItem item, int amount) {
        SkyBlockItem copy = item.clone();
        copy.setAmount(amount);
        return copy;
    }
}
