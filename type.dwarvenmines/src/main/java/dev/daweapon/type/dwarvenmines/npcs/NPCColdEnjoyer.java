package dev.daweapon.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;

public class NPCColdEnjoyer extends AbstractTalkingNPC {

	public NPCColdEnjoyer() {
		super("Cold Enjoyer", "<b>",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM4YTJmYjA2ZDQ3Nzc2MDY2OTQ0ODdmNWI2ZjI5MDc2ZmY2YzA5Y2Y0NTE5MWI5YjE2Njk2Y2Q5OTYxYTg4NzYifX19",
				new Pos(-15.25, 136.78125, 217.25, 0, 0),
				"I've figured out exactly how this Cold stuff works!",
				"For every 1 Cold Resistance you have, Cold builds up 1% slower.",
				"So, if you have 100 Cold Resistance, Cold will take 2x as long to build up!",
				"You can get Cold Resistance from some Heart of the Mountain perks, and from certain items.",
				"Your Mining Speed is reduced by 0.5% for every Cold you have.",
				"The formula is: ( Current Cold - Cold Resistance ) / 2 = Mining Speed Penalty",
				"So, if you have 70 Cold and 5 Cold Resistance, your Mining Speed will be reduced by 7.5%.",
				"If your Cold Resistance is greater than the amount of Cold you have, you won't receive a Mining Speed reduction!",
				"Since 99 Cold is the most you can have before dying,",
				"a Cold Resistance stat of 99 or more will be enough to ensure you never receive a Mining Speed Penalty.",
				"Every 25 Cold, your speed cap will be reduced by 25%.",
				"By the time you're at 75 Cold, you'll be moving at a snail's pace...",
				"If you want to keep moving, keep warm!");
	}
}
