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
 * Pet Items: a pet holds at most one, which boosts its stats, its Pet Exp or its rarity. Giving a pet
 * a new item returns the one it was holding for a fee based on that item's rarity.
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
        ItemType given = petItem.getAttributeHandler().getPotentialType();
        ItemType previous = pet.getAttributeHandler().getPetHeldItem();
        String problem = problem(pet, given, component, previous);
        if (problem != null) {
            player.sendMessage("<c>" + problem);
            return;
        }

        long fee = previous == null ? 0 : swapFee(new SkyBlockItem(previous).getAttributeHandler().getRarity());
        if (player.getCoins() < fee) {
            player.sendMessage("<c>You need <6>" + String.format("%,d", fee) + " Coins <c>to take back your pet's current item!");
            return;
        }
        if (fee > 0) player.removeCoins(fee);

        Rarity before = pet.getAttributeHandler().getRarity();
        hold(pet, given);
        player.setItemInHand(petItem.getAmount() > 1 ? withAmount(petItem, petItem.getAmount() - 1) : null);
        if (previous != null) player.addAndUpdateItem(new SkyBlockItem(previous));
        player.getPetData().refreshCachedAbilities();
        if (pet.getAttributeHandler().getRarity() != before) player.getPetData().updatePetEntityImpl(player);

        String petName = "<color:" + pet.getAttributeHandler().getRarity().getColor().asHexString() + ">"
                + pet.getComponent(PetComponent.class).getPetName();
        String itemName = "<color:" + petItem.getAttributeHandler().getRarity().getColor().asHexString() + ">"
                + petItem.getDisplayName();
        player.sendMessage("<a>Your " + petName + " <a>is now holding " + itemName + "<a>.");
    }

    public static @Nullable String problem(SkyBlockItem pet, ItemType given, PetItemComponent component,
                                           @Nullable ItemType previous) {
        ItemType petType = pet.getAttributeHandler().getPotentialType();
        if (!component.appliesTo(petType)) return "This Pet Item can't be given to this pet!";
        if (previous == given) return "Your pet is already holding this item!";
        if (component.getTierBoost() != null) {
            Rarity target = baseRarity(pet).upgrade();
            PetComponent petComponent = pet.getComponent(PetComponent.class);
            if (target.ordinal() > component.getTierBoost().maxFor(petType).ordinal()
                    || !petComponent.getRarities().contains(target)) {
                return "This pet can't be boosted to a higher rarity!";
            }
        }
        return null;
    }

    public static void hold(SkyBlockItem pet, @Nullable ItemType item) {
        Rarity base = baseRarity(pet);
        pet.getAttributeHandler().setPetHeldItem(item);
        PetItemComponent held = held(pet);
        pet.getAttributeHandler().setRarity(held != null && held.getTierBoost() != null ? base.upgrade() : base);
    }

    public static Rarity baseRarity(SkyBlockItem pet) {
        Rarity rarity = pet.getAttributeHandler().getRarity();
        return isTierBoosted(pet) ? rarity.downgrade() : rarity;
    }

    public static boolean isTierBoosted(@Nullable SkyBlockItem pet) {
        PetItemComponent held = held(pet);
        return held != null && held.getTierBoost() != null;
    }

    public static long swapFee(Rarity heldRarity) {
        return switch (heldRarity) {
            case COMMON -> 25_000;
            case UNCOMMON -> 50_000;
            case RARE -> 100_000;
            case EPIC -> 250_000;
            case LEGENDARY -> 500_000;
            case MYTHIC -> 1_000_000;
            default -> 2_500_000;
        };
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

    private static @Nullable PetItemComponent active(@Nullable SkyBlockItem pet) {
        PetItemComponent item = held(pet);
        return item == null || item.isInactive() ? null : item;
    }

    /**
     * The pet's own stats at its level, including what its held Pet Item adds, but not its
     * abilities. This is what Chimera and the Manticore Claw copy.
     */
    public static ItemStatistics statistics(SkyBlockItem pet) {
        PetComponent component = pet.getComponent(PetComponent.class);
        int level = PetLevels.level(pet);
        ItemStatistics base = component.statisticsAt(pet.getAttributeHandler().getRarity(), level);

        PetItemComponent item = active(pet);
        if (item == null) return base;
        ItemStatistics.Builder boosted = ItemStatistics.builder();
        for (ItemStatistic statistic : ItemStatistic.values()) {
            double value = base.getOverall(statistic);
            if (value == 0) continue;
            double percent = item.getAllPetStatisticsBoost() + item.getPetStatisticBoosts().getOrDefault(statistic, 0D);
            if (percent != 0) boosted.withBase(statistic, value * percent / 100D);
        }
        ItemStatistics total = ItemStatistics.add(ItemStatistics.add(base, boosted.build()), item.getStatistics());
        if (item.hasLevelStatistics()) {
            total = ItemStatistics.add(total, ItemStatistics.multiply(item.getLevelStatistics(), level / item.getLevelStep()));
        }
        return total;
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
        PetItemComponent item = active(player.getPetData().getEnabledPet());
        return item != null && item.isKnockbackImmune();
    }

    public static double expMultiplier(SkyBlockItem pet, SkillCategories skill) {
        PetItemComponent item = active(pet);
        if (item == null || item.getPetExpBoost() == 0) return 1;
        if (item.getPetExpSkill() != null && item.getPetExpSkill() != skill) return 1;
        return 1 + item.getPetExpBoost() / 100D;
    }

    public static double expShareRate(@Nullable SkyBlockItem pet) {
        PetItemComponent item = active(pet);
        return item == null ? 0 : item.getExpShareRate() / 100D;
    }

    public static double trophyTierMultiplier(SkyBlockPlayer player, String tier) {
        PetItemComponent item = active(player.getPetData().getEnabledPet());
        return item == null ? 1 : 1 + item.getTrophyTierBoost().getOrDefault(tier, 0D) / 100D;
    }

    private static SkyBlockItem withAmount(SkyBlockItem item, int amount) {
        SkyBlockItem copy = item.clone();
        copy.setAmount(amount);
        return copy;
    }
}
