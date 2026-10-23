package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.island.building.BlockZapper;

public class BlockZapperAbility extends RegisteredAbility {

    public BlockZapperAbility() {
        super("ZAPPER", "Grand... Zapper?",
                (player, item) -> "Right-click the face of a block to zap all connected blocks of the same type.",
                AbilityActivation.RIGHT_CLICK_BLOCK, 0, new RegisteredAbility.NoAbilityCost(),
                BlockZapper::zap);
    }
}
