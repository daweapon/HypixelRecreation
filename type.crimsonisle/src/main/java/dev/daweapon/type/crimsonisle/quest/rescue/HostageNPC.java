package dev.daweapon.type.crimsonisle.quest.rescue;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** The captive a Rescue Mission is after. Where it is held depends on the quest's tier; clicking it ends the mission. */
@Getter
public class HostageNPC extends HypixelNPC {
    private final RescueSite site;

    public HostageNPC(RescueSite site) {
        super((site == RescueSite.MAGE ? RescueAppearance.MAGE_HOSTAGE : RescueAppearance.PIGLIN).configure(
                new String[]{"<e>Hostage", "<e><l>CLICK"}, "<e>Hostage",
                player -> spot(site, player), player -> {
                    RescueState state = RescueService.state(player);
                    return state != null && state.infiltrating(site);
                }, true, true));
        this.site = site;
    }

    private static Pos spot(RescueSite site, SkyBlockPlayer player) {
        RescueState state = RescueService.state(player);
        return state == null ? site.hostageSpot(null) : site.hostageSpot(state.tier());
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        RescueService.rescue((SkyBlockPlayer) event.player(), this);
    }
}
