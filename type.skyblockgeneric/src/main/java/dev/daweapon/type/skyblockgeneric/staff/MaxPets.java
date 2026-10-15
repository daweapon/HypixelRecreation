package dev.daweapon.type.skyblockgeneric.staff;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetData;
import dev.daweapon.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import dev.daweapon.type.skyblockgeneric.user.pets.PetScore;

import java.util.Comparator;
import java.util.List;

/**
 * {@code /maxprofile}'s pet step: every pet the item registry defines, at its highest rarity and
 * level, in the Pets menu. An owned copy is upgraded in place, so running it again adds nothing.
 */
public final class MaxPets {
    private static final Comparator<SkyBlockItem> BEST_COPY = Comparator
            .comparingInt((SkyBlockItem pet) -> pet.getAttributeHandler().getRarity().ordinal())
            .thenComparingDouble(pet -> pet.getAttributeHandler().getPetData().getExperience());

    private MaxPets() {
    }

    public static List<ItemType> petTypes() {
        return ConfigurableSkyBlockItem.getIDs().stream()
                .sorted()
                .filter(id -> {
                    ConfigurableSkyBlockItem config = ConfigurableSkyBlockItem.getFromID(id);
                    return config != null && config.hasComponent(PetComponent.class);
                })
                .map(ItemType::get)
                .filter(type -> type != null)
                .toList();
    }

    public static SkyBlockItem maxed(ItemType type) {
        SkyBlockItem pet = new SkyBlockItem(type);
        maximise(pet);
        return pet;
    }

    public static void maximise(SkyBlockItem pet) {
        PetComponent component = pet.getComponent(PetComponent.class);
        pet.getAttributeHandler().setRarity(component.getMaxRarity());
        if (!PetLevels.isMaxLevel(pet)) PetLevels.setLevel(pet, component.getMaxLevel());
    }

    public static void maxOut(SkyBlockPlayer player) {
        DatapointPetData datapoint = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.PET_DATA, DatapointPetData.class);
        DatapointPetData.UserPetData data = datapoint.getValue();
        maxOut(data);
        data.refreshCachedAbilities();
        datapoint.setValue(data);
        if (data.getEnabledPet() != null) data.updatePetEntityImpl(player);
        PetScore.syncSkyBlockXP(player);
    }

    public static void maxOut(DatapointPetData.UserPetData data) {
        for (ItemType type : petTypes()) {
            SkyBlockItem owned = data.getPetsMap().keySet().stream()
                    .filter(pet -> pet.getAttributeHandler().getPotentialType() == type)
                    .max(BEST_COPY)
                    .orElse(null);
            if (owned != null) {
                maximise(owned);
            } else {
                data.addPet(maxed(type));
            }
        }
    }
}
