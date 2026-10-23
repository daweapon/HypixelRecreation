package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.island.building.BuildersWand;

public class BuildersWandAbility extends RegisteredAbility {

    public BuildersWandAbility() {
        super("GRAND_ARCHITECT", "Grand Architect",
                (player, item) -> "Right-click the face of a block to extend all connected block faces.",
                AbilityActivation.RIGHT_CLICK_BLOCK, 0, new RegisteredAbility.NoAbilityCost(),
                BuildersWand::extend);
    }
}
