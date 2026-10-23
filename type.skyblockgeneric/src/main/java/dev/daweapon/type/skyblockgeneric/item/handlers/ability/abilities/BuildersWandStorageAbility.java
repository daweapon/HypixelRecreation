package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.island.building.BuildersWand;

public class BuildersWandStorageAbility extends RegisteredAbility {

    public BuildersWandStorageAbility() {
        super("BUILT_IN_STORAGE", "Built-in Storage",
                (player, item) -> "Opens the wand storage. Blocks will be placed from your inventory or the wand storage.",
                AbilityActivation.LEFT_CLICK, 0, new RegisteredAbility.NoAbilityCost(),
                (player, item, _, _) -> BuildersWand.openStorage(player));
    }
}
