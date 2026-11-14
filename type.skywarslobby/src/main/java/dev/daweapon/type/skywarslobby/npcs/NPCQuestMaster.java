package dev.daweapon.type.skywarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import dev.daweapon.type.generic.achievement.AchievementCategory;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.VillagerConfiguration;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.lobby.gui.GUIGameQuests;

import dev.daweapon.type.generic.event.custom.NPCInteractEvent;

public class NPCQuestMaster extends HypixelNPC {

	public NPCQuestMaster() {
		super(new VillagerConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"<b>Quest Master",
						"<e><l>RIGHT CLICK",
				};
			}


			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(11.5, 63, 6.5, 110, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return false;
			}

			@Override
			public VillagerProfession profession() {
				return VillagerProfession.LIBRARIAN;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		new GUIGameQuests(AchievementCategory.SKYWARS).open(event.player());
	}
}
