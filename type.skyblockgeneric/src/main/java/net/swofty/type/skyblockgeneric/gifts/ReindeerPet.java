package net.swofty.type.skyblockgeneric.gifts;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetAbilities;

public final class ReindeerPet {
    private ReindeerPet() {
    }

    public static boolean isReindeer(SkyBlockItem pet) {
        return pet != null && pet.getAttributeHandler().getPotentialType() == ItemType.REINDEER_PET;
    }

    public static double winterSprintMultiplier(SkyBlockItem pet) {
        if (!isReindeer(pet)) return 1;
        return pet.getComponent(PetComponent.class).getAbilities().stream()
                .filter(ability -> ability.getName().equals("Winter Sprint") && ability.isActive()
                        && ability.appliesAt(pet.getAttributeHandler().getRarity()))
                .findFirst().map(ability -> ability.valuesFor(pet)).filter(values -> values.length > 0)
                .map(values -> values[0]).orElse(1D);
    }

    public static double snowPowerPercent(SkyBlockPlayer player) {
        return PetAbilities.value(player, ItemType.REINDEER_PET, "Snow Power", 0);
    }

    public static double icyWindPercent(SkyBlockPlayer player) {
        return PetAbilities.value(player, ItemType.REINDEER_PET, "Icy Wind", 0);
    }
}
