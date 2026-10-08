package net.swofty.type.skyblockgeneric.item.handlers.pet.registry;

import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.CountScaledStatHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc.LazerbeamHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetConditions;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;

import java.util.HashSet;
import java.util.Set;

/**
 * Handlers and named conditions for the Farming, Alchemy, Enchanting, Taming, Hunting, Gabagool,
 * Rift and Bingo pets.
 */
final class MiscPetAbilities implements PetAbilityRegistry.Provider {
    @Override
    public void register(PetAbilityRegistry.Registrar registrar) {
        PetConditions.register("JUMP_BOOST", player -> player.hasEffect(PotionEffect.JUMP_BOOST));

        registrar.register("GUARDIAN_LAZERBEAM", new LazerbeamHandler());
        registrar.register("ROSE_DRAGON_GARDEN_POWER", CountScaledStatHandler.flat(
                player -> player.getSkills().getCurrentLevel(SkillCategories.FARMING), ItemStatistic.FARMING_FORTUNE));
        registrar.register("ROSE_DRAGON_SYMBIOSIS",
                CountScaledStatHandler.flat(MiscPetAbilities::roseSymbiosisPets, ItemStatistic.FARMING_FORTUNE));
    }

    private static int roseSymbiosisPets(SkyBlockPlayer player) {
        Set<SkyBlockItem> pets = player.getPetData().getPetsMap().keySet();
        boolean ownsMaxedRose = pets.stream().anyMatch(pet ->
                pet.getAttributeHandler().getPotentialType() == ItemType.ROSE_DRAGON_PET && PetLevels.isMaxLevel(pet));
        if (!ownsMaxedRose) return 0;
        Set<ItemType> maxed = new HashSet<>();
        for (SkyBlockItem pet : pets) {
            ItemType type = pet.getAttributeHandler().getPotentialType();
            if (type == ItemType.ROSE_DRAGON_PET || !pet.hasComponent(PetComponent.class)) continue;
            if (pet.getComponent(PetComponent.class).getPetType() == PetType.FARMING && PetLevels.isMaxLevel(pet)) maxed.add(type);
        }
        return maxed.size();
    }
}
