package dev.daweapon.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.AnimalConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.jerrysworkshop.gui.GUIShopSherry;

// Sherry, the snow golem outside Sherry's Showroom in Jerry's Workshop, who runs a winter shop.
public class NPCSherry extends HypixelNPC {

	public NPCSherry() {
		super(new AnimalConfiguration() {
			@Override
			public EntityType entityType() {
				return EntityType.SNOW_GOLEM;
			}

			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"<c>Sherry",
						"<e><l>CLICK"
				};
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(6.500, 76.000, 95.500, 100, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		event.player().openView(new GUIShopSherry());
	}
}
