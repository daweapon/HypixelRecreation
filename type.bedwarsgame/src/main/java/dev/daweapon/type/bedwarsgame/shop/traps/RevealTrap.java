package dev.daweapon.type.bedwarsgame.shop.traps;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.bedwars.map.BedWarsMapsConfig;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.shop.Currency;
import dev.daweapon.type.bedwarsgame.shop.Trap;
import dev.daweapon.type.bedwarsgame.shop.TrapId;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.utility.ScheduleUtility;

// Team trap that makes the triggering intruder glow, revealing their location.
public class RevealTrap extends Trap {

	public RevealTrap() {
		super(
			TrapId.REVEAL,
				"Reveal Trap",
				ItemStack.of(Material.REDSTONE_TORCH),
				"Reveals intruders by glowing for 10s.",
				Currency.DIAMOND
		);
	}

	@Override
	public void onTrigger(BedWarsGame game, BedWarsMapsConfig.TeamKey teamName, BedWarsPlayer triggerer) {
		triggerer.setGlowing(true);
		ScheduleUtility.delay(
				() -> triggerer.setGlowing(false),
				10 * 20
		);
	}
}
