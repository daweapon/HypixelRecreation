package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;

public final class EerieUpgrades {
    private EerieUpgrades() {
    }

    public static Rarity claimedRarity(SkyBlockPlayer player) {
        if (TyashoiProgress.claimed(player, "EERIE_TOY")) return Rarity.LEGENDARY;
        if (TyashoiProgress.claimed(player, "EERIE_TREAT")) return Rarity.RARE;
        return Rarity.COMMON;
    }

    public static SkyBlockItem newPet(SkyBlockPlayer player) {
        SkyBlockItem pet = new SkyBlockItem(ItemType.EERIE_PET);
        pet.getAttributeHandler().setRarity(claimedRarity(player));
        return pet;
    }

    public static void upgradeOwned(SkyBlockPlayer player) {
        Rarity target = claimedRarity(player);
        var petData = player.getPetData();
        for (SkyBlockItem pet : new ArrayList<>(petData.getPetsMap().keySet())) {
            if (pet.getAttributeHandler().getPotentialType() != ItemType.EERIE_PET) continue;
            if (pet.getAttributeHandler().getRarity().ordinal() >= target.ordinal()) continue;
            boolean active = Boolean.TRUE.equals(petData.getPetsMap().get(pet));
            SkyBlockItem upgraded = pet.clone();
            upgraded.getAttributeHandler().setRarity(target);
            petData.removePet(pet);
            petData.addPet(upgraded);
            if (active) {
                petData.setEnabled(upgraded, true);
                petData.updatePetEntityImpl(player);
            }
        }
    }
}
