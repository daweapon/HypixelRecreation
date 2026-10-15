package dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry;

import dev.daweapon.type.skyblockgeneric.gifts.SnowmanPet;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.EggstraLootHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.HigherGroundHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.HiveHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.KillComboHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.StampedeHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;

/**
 * Handlers for the pets that existed before the Pets update.
 */
final class CorePetAbilities implements PetAbilityRegistry.Provider {
    @Override
    public void register(PetAbilityRegistry.Registrar registrar) {
        registrar.register("HIVE", new HiveHandler());
        registrar.register("KILL_COMBO", new KillComboHandler());
        registrar.register("EGGSTRA_LOOT", new EggstraLootHandler());
        registrar.register("HIGHER_GROUND", new HigherGroundHandler());
        registrar.register("STAMPEDE", new StampedeHandler());
        registrar.register("SNOWMAN_BLIZZARD", new PetAbilityHandler() {
            @Override
            public void onEvent(PetEvent event, PetAbilityContext context) {
                SnowmanPet.blizzard(event, context.values(), context.runtime());
            }
        });
        registrar.register("SNOWMAN_SNOW_CANNON", new PetAbilityHandler() {
            @Override
            public void onEvent(PetEvent event, PetAbilityContext context) {
                SnowmanPet.snowCannon(event, context.values(), context.runtime());
            }
        });
    }
}
