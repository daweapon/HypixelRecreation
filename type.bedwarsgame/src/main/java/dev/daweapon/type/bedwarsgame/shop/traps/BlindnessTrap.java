package dev.daweapon.type.bedwarsgame.shop.traps;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import dev.daweapon.commons.bedwars.map.BedWarsMapsConfig;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.shop.Currency;
import dev.daweapon.type.bedwarsgame.shop.Trap;
import dev.daweapon.type.bedwarsgame.shop.TrapId;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;

// Team trap that blinds the intruder for a short duration when triggered.
public class BlindnessTrap extends Trap {

	public BlindnessTrap() {
		super(
			TrapId.BLINDNESS,
				"Blindness Trap",
				ItemStack.of(Material.TRIPWIRE_HOOK),
				"Blinds intruders for 10 seconds.",
				Currency.DIAMOND
		);
	}

	@Override
	public void onTrigger(BedWarsGame game, BedWarsMapsConfig.TeamKey teamName, BedWarsPlayer triggerer) {
		triggerer.addEffect(new Potion(PotionEffect.BLINDNESS, (byte) 1, 200));
	}
}
