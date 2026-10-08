package net.swofty.type.crimsonisle.quest.rescue;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;

/** One guard post in a rescue venue. It only exists for players currently sneaking through that venue. */
@Getter
public class RescueGuardNPC extends HypixelNPC {
    private final RescueSite site;
    private final int index;

    public RescueGuardNPC(RescueSite site, int index) {
        super(appearance(site).configure(new String[]{site.getGuardName()}, site.getGuardName(),
                _ -> facing(site, index), player -> {
                    RescueState state = RescueService.state(player);
                    return state != null && state.infiltrating(site);
                }, false, false));
        this.site = site;
        this.index = index;
    }

    public Pos post() {
        return site.getGuardPosts().get(index);
    }

    private static Pos facing(RescueSite site, int index) {
        Pos post = site.getGuardPosts().get(index);
        return post.withView(site.guardYaw(post), 0);
    }

    private static RescueAppearance appearance(RescueSite site) {
        EntityType entity = site.getGuardEntity();
        return entity == null ? RescueAppearance.MAGE_GUARD : RescueAppearance.of(entity);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        sendNPCMessage(event.player(), RescueService.RESTRICTED_LINE);
    }
}
