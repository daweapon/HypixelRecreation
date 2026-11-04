package dev.daweapon.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCTerry extends HypixelNPC {

	public NPCTerry() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"Terry",
						"<e><l>CLICK"
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "NrK1/1oig6wiHeu00G5K/FKHQ+sFOVTbhULXdInFHV0wKveGno7E8/p6X1gDyS1Kg6ucXyiQuv4ObybCyX1Jfr0IuhOfzevWfaS0r5MbHkRbnZZpgcBDwUsZBcQifuoeK3ARF5olPpoqNMOgKYrsjPsvYUky5sJh5Umou/e1GhQg+1fxufnd4J3JzlJ0McAipMXZOF3dzrxvXU4gQQFYbHmDWc+9yoceeVjrn9N+aXTEvN/sn64jwRQTKlaQzs7AMVzu29D/y5BatMAonD0dPcJxGJeb+U3OTtjdbXhYKX8tD3wMvDYrNtGgdTce9ImCliKgKBivjiIKSarF7Hz4fC74c/+3K2YjZLMnX37Nj5LBEDHPhtF369QHxTB1rJUEgFda6CjvUOwq5haJBRyHFDa/hXU103nuCodDi7rkF8R6eGTaAYmfYfBMNlK0q2gVApH1WP21ChsnunNFLQnAmdibYi0TsVCgeBKLdLpK3MNoWeN7J13fEfUxxUdqCMNemtjLKbfKkzUq6h16zDS4PVD12Qj0FeGCDflFAHsPUckHrblG11ksSZuC0BZw6HBROuJCMzxHo4MCfUNTtyxVuKIXZGqOnm18mhrHsjuTjWVbpFCAUqx5l8M55hPewNvoyjRcnc9ojH9pCATgmlEPb3rdn+ILFhttT+SjpQ+8rfU=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzQ3ODY4NTg1MTEsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzljM2I3MjI0MmYzOGIxNWMxMzEyZmFjODRlN2NjNzMzMjkyMzZkMjAyMThlMGFiNzhkMDhlMmFjNDc4MDZhNDgifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-92.500, 78.000, 25.500, -66, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	private static final String SPOKEN = "npc.terry.spoken";

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;
		if (ChapterTriggers.done(player, SPOKEN)) {
			player.openView(new CatalogueShopView("terry"));
			return;
		}
		setDialogue(player, "hello").thenRun(() -> ChapterTriggers.fire(player, SPOKEN));
	}

	@Override
	public DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[]{
				DialogueSet.builder().key("hello").lines(
						"Ahoy, <b>" + player.getUsername() + "<f>! Welcome to the Jerry Pond!",
						"You may have noticed that this pond is full of unique creatures and treasures!",
						"I'm looking for <b>Ice Hunks<f> and <b>Blue Ice Hunks<f>!",
						"You can find these by <b>ice fishing<f> in the pond - break the ice and cast your rod!",
						"Bring enough hunks back to me and I will reforge your fishing rod with modifiers exclusive to me -  Terry!"
				).build()
		};
	}
}
