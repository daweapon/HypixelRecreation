package net.swofty.type.skyblockgeneric.item.handlers.pet.registry;

import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.CountScaledStatHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc.BarthererHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc.RepugnantAromaHandler;
import net.swofty.type.skyblockgeneric.garden.GardenProgress;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.pest.PestBestiary;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc.BingoChimeraHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc.BingoClimberHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc.BulwarkHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc.CoinsPerMobLevelHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc.DeathsTouchHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc.ExtinguishHandler;
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
        registrar.register("BINGO_CLIMBER", new BingoClimberHandler());
        registrar.register("BINGO_CHIMERA", new BingoChimeraHandler());
        registrar.register("COINS_PER_MOB_LEVEL", new CoinsPerMobLevelHandler());
        registrar.register("WISP_BULWARK", new BulwarkHandler());
        registrar.register("WISP_EXTINGUISH", new ExtinguishHandler());
        registrar.register("WITHER_SKELETON_DEATHS_TOUCH", new DeathsTouchHandler());
        registrar.register("ROSE_DRAGON_GARDEN_POWER", CountScaledStatHandler.flat(
                player -> player.getSkills().getCurrentLevel(SkillCategories.FARMING), ItemStatistic.FARMING_FORTUNE));
        registrar.register("HEDGEHOG_HUNTERS_INSIGHT",
                CountScaledStatHandler.flat(PestBestiary::totalTiers, ItemStatistic.FARMING_FORTUNE));
        registrar.register("MOSQUITO_BUZZIN_BARTERER", new BarthererHandler());
        registrar.register("SLUG_REPUGNANT_AROMA", new RepugnantAromaHandler());
        registrar.register("ROSE_DRAGON_ROSY_SCALES", CountScaledStatHandler.flat(
                player -> GardenProgress.totalMilestoneTiers(Gardens.profile(player)),
                ItemStatistic.FARMING_FORTUNE, ItemStatistic.SPEED));
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
