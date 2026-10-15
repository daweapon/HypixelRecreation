package dev.daweapon.type.skyblockgeneric.event.actions.custom.bestiary;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryData;
import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryExperience;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.event.custom.BestiaryUpdateEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// Announces bestiary tier-ups and grants their stat/coin/XP rewards
//TODO make messages clickable to open the right mob gui

public class ActionBestiaryLevelUp implements HypixelEventClass {

	private final BestiaryData bestiaryData = new BestiaryData();

	@PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
	public void run(BestiaryUpdateEvent event) {
		SkyBlockPlayer player = event.getPlayer();
		BestiaryMob mob = event.getBestiaryMob();
		int oldKills = event.getOldTotalValue();
		int newKills = event.getNewTotalValue();
		String mobName = mob.getDisplayName();

		int oldTier = bestiaryData.getCurrentBestiaryTier(mob, oldKills);
		int newTier = bestiaryData.getCurrentBestiaryTier(mob, newKills);

		if (oldTier != newTier) BestiaryExperience.syncSkyBlockXP(player);

		if (newKills == 1) {
			player.sendMessage("  <3><l>BESTIARY FAMILY UNLOCKED </l><b>{}", mobName);
			return;
		}

		if (newTier == oldTier + 1) {
			Text transitionLine = oldTier == 0
					? Text.of("<center><b>{} {:roman}</center>", mobName, newTier)
					: Text.of("<center><b>{} <8>{:roman} ➡ <b>{:roman}</center>", mobName, oldTier, newTier);

			int magicFind = bestiaryData.getMagicFind(newTier);
			int strength = bestiaryData.getStrength(newTier);
			int coinBonus = bestiaryData.getExtraCoinPercentage(newTier);
			int xpBonus = bestiaryData.getExtraXpPercentage(newTier);

			List<Text> lines = new ArrayList<>();
			lines.add(Text.empty());
			lines.add(Text.of("<center><6><l>BESTIARY</center>"));
			lines.add(transitionLine);
			lines.add(Text.empty());
			lines.add(Text.of("<center><6><l>REWARDS</center>"));
			lines.add(Text.of("<center><8>+<a>{} {} <stat:magic_find></center>", magicFind, mobName));
			lines.add(Text.of("<center><8>+<a>{} {} <stat:strength></center>", strength, mobName));
			lines.add(Text.of("<center><8>+<6>{}% <a>{} <7>coins</center>", coinBonus, mobName));
			lines.add(Text.of("<center><8>+<a>{}% <7>chance for extra XP orbs</center>", xpBonus));
			lines.add(Text.of("<center><8>+<b>1 SkyBlock XP</center>"));
			lines.add(Text.empty());

			player.sendMessage("<3><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			for (Text line : lines) {
				player.sendMessage(line);
			}
			player.sendMessage("<3><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		}
	}
}
