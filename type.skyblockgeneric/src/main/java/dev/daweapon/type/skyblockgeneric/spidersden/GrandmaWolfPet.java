package dev.daweapon.type.skyblockgeneric.spidersden;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetData;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Grandma Wolf's pet (wiki: Grandma_Wolf): given on the first visit as Common, then raised to Uncommon, Rare,
 * Epic and Legendary at Bestiary Milestones 5, 10, 15 and 20 (wiki: Grandma_Wolf_Pet, confirmed in a player video).
 */
public final class GrandmaWolfPet {
    public static final String ID = "GRANDMA_WOLF_PET";
    public static final Map<Rarity, Integer> MILESTONES = Map.of(
            Rarity.UNCOMMON, 5, Rarity.RARE, 10, Rarity.EPIC, 15, Rarity.LEGENDARY, 20);

    private GrandmaWolfPet() {
    }

    public static ItemType type() {
        return ItemType.get(ID);
    }

    public static @Nullable Rarity next(Rarity current) {
        return switch (current) {
            case COMMON -> Rarity.UNCOMMON;
            case UNCOMMON -> Rarity.RARE;
            case RARE -> Rarity.EPIC;
            case EPIC -> Rarity.LEGENDARY;
            default -> null;
        };
    }

    public static int milestoneFor(Rarity rarity) {
        return MILESTONES.getOrDefault(rarity, 0);
    }

    public static @Nullable SkyBlockItem owned(SkyBlockPlayer player) {
        return player.getPetData().getPet(type());
    }

    public static boolean give(SkyBlockPlayer player) {
        DatapointPetData.UserPetData data = player.getPetData();
        if (data.isFull()) return false;
        SkyBlockItem pet = new SkyBlockItem(type());
        pet.getAttributeHandler().setRarity(Rarity.COMMON);
        data.addPet(pet);
        return true;
    }

    public static void upgrade(SkyBlockPlayer player, SkyBlockItem pet, Rarity to) {
        DatapointPetData.UserPetData data = player.getPetData();
        boolean enabled = pet.equals(data.getEnabledPet());
        data.removePet(pet);
        pet.getAttributeHandler().setRarity(to);
        data.addPet(pet);
        if (enabled) {
            data.setEnabled(pet, true);
            data.updatePetEntityImpl(player);
        }
        data.refreshCachedAbilities();
    }
}
