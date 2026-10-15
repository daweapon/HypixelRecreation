package dev.daweapon.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.jerrysworkshop.gui.GUIShopFrosty;
import dev.daweapon.type.skyblockgeneric.gifts.SnowCannon;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Frosty in Jerry's Workshop: gives a free Snow Cannon on the first visit, refills carried cannons at
// 1 coin per Snowball, and otherwise sells another Snow Cannon.
public class NPCFrosty extends HypixelNPC {

	public NPCFrosty() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"Frosty"
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "pSocoChCCNb8dv3syEVm3kolZaVkf6qR70ngHQdDRw6/rA2tVIZbvBWS6n99Nx8MxfMpRi9mcSTGuKGuOq7lHradijwTrHIn8pcMD2k3JwzXyVQ+ipxqAxJChJTffASJFFL7v/o9SzY89ullKfVl5xIgJHmw00Z77qDDn9ZnWLaazwlfpMR443kOVge/3DlRp62ZFbLBvvyc5locPIUm6Fbl9kbKMBOeEJLFzw+1jvBlkiBbsSemzL7i4ro3Jvd0LUxtPoZ+GONTfdAYE3Go/6zSrBwtVloGYbbtdvyH8rijD9nbLUVWbZV3YeCO+0oNlrxpCVksAEAAHUpDhoakz/vJe+2QEtMsckv6ahjemDnZ3Mo2oewjvL0ENNqClBnp6g8pgu+8P4Cv3qJozs8+Ftk8Rwum9z51bSNxApvz5mnR9dyHbysRYeVrWqULHcWrBt1eeL7AyxNR1JwExF8/3hXpqXdABwgLf3vGCtjafVjcfeAlf3sjQ2voDnQQeULQhoOqVlSqz8s8tlNUJYmIGm9MhS5kBwGUvE6s/82if7dYLL9zGA0WqmmPs2PuOby4a3BBWG4dDgmd/jqkNLFhLDm28o1+SCIYscLwEdJ341BoJIXD20xJBtmRLTsC2HhIXXvmCNxSH9rQyPhmThbf2Hd3GB3zMY0c3my4sN9HJKI=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzQ4NzQ2OTcyMzMsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2UxNmY5NmM3NTZkZWZlZTYzOTA4OWI3MDI5MDE0MzEwNzI0ZGYwZWYzMGM3NTEwZjJkNzJhYzllMGRhY2Y3ZDMifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-1.500, 76.000, 92.500, 124, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		boolean firstVisit = !player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FROSTY);
		player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FROSTY, true);

		if (SnowCannon.hasAnyCannon(player)) {
			int refilled = SnowCannon.refill(player);
			player.sendMessage(refilled > 0
					? "<c>Frosty<f>: Refilled <a>" + String.format("%,d", refilled) + " <f>Snowballs for <6>"
					+ String.format("%,d", refilled * SnowCannon.COINS_PER_SNOWBALL) + " coins<f>!"
					: "<c>Frosty<f>: Your Snowballs are already topped up - or you're out of coins!");
			return;
		}
		if (firstVisit) {
			player.addAndUpdateItem(ItemType.SNOW_CANNON);
			player.sendMessage("<c>Frosty<f>: Here, take this <9>Frosty the Snow Cannon<f>! It's loaded with 1,000 Snowballs.");
			return;
		}
		player.openView(new GUIShopFrosty());
	}
}
