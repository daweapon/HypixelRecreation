package net.swofty.type.skyblockgeneric.user.pets;

import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetCandyComponent;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Feeding Pet Candy to the summoned pet. A pet at its level cap can't eat, fixed-level and Gabagool
 * pets don't level from candy, and capped candy stops after {@link PetCandyComponent#LIFETIME_CAP}.
 */
public final class PetCandy {
    private PetCandy() {
    }

    public static @Nullable String problem(SkyBlockItem pet, PetCandyComponent candy) {
        PetComponent component = pet.getComponent(PetComponent.class);
        if (component.isFixedLevel() || component.getPetType() == PetType.GABAGOOL) return "This pet can't eat Pet Candy!";
        if (PetLevels.isMaxLevel(pet)) return "This pet is already at its max level!";
        if (candy.isCountsTowardsCap() && pet.getAttributeHandler().getPetCandyUsed() >= PetCandyComponent.LIFETIME_CAP) {
            return "This pet has already eaten " + PetCandyComponent.LIFETIME_CAP + " Pet Candy!";
        }
        return null;
    }

    public static boolean apply(SkyBlockItem pet, PetCandyComponent candy) {
        if (problem(pet, candy) != null) return false;
        if (candy.isCountsTowardsCap()) {
            pet.getAttributeHandler().setPetCandyUsed(pet.getAttributeHandler().getPetCandyUsed() + 1);
        }
        if (candy.grantsLevels()) {
            PetLevels.setLevel(pet, targetLevel(pet, candy));
        } else {
            PetLevels.addExperience(pet, candy.getExperience());
        }
        return true;
    }

    public static int targetLevel(SkyBlockItem pet, PetCandyComponent candy) {
        int max = PetLevels.maxLevel(pet);
        return candy.isToMaxLevel() ? max : Math.min(max, PetLevels.level(pet) + candy.getLevels());
    }

    static String gainDescription(SkyBlockItem pet, PetCandyComponent candy) {
        if (candy.isToMaxLevel()) return "reached <6>max level";
        if (candy.grantsLevels()) return "gained <6>" + (targetLevel(pet, candy) - PetLevels.level(pet)) + " level(s)";
        return "gained <6>" + String.format("%,d", (long) candy.getExperience()) + " <a>Pet Exp";
    }

    public static void feed(SkyBlockPlayer player, SkyBlockItem candyItem, PetCandyComponent candy) {
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        if (pet == null) {
            player.sendMessage("<c>You need to have a pet summoned to feed it Pet Candy!");
            return;
        }
        String problem = problem(pet, candy);
        if (problem != null) {
            player.sendMessage("<c>" + problem);
            return;
        }
        int before = PetLevels.level(pet);
        String gain = gainDescription(pet, candy);
        apply(pet, candy);
        SkyBlockItem remaining = candyItem.getAmount() > 1 ? candyItem.clone() : null;
        if (remaining != null) remaining.setAmount(candyItem.getAmount() - 1);
        player.setItemInHand(remaining);
        player.sendMessage("<a>Your <color:" + pet.getAttributeHandler().getRarity().getColor().asHexString() + ">"
                + pet.getComponent(PetComponent.class).getPetName() + " <a>ate " + candyItem.getDisplayName()
                + " <a>and " + gain + "<a>!");
        if (PetLevels.level(pet) > before) {
            player.getPetData().refreshCachedAbilities();
            PetScore.syncSkyBlockXP(player);
        }
    }
}
