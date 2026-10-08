package net.swofty.type.crimsonisle.quest.rescue;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.skyblockgeneric.crimson.questline.BarbarianStep;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** One guard post in a rescue venue. It only exists for players currently sneaking through that venue. */
@Getter
public class RescueGuardNPC extends HypixelNPC {
    private final RescueSite site;
    private final int index;

    public RescueGuardNPC(RescueSite site, int index) {
        super(appearance(site).configure(new String[]{site.getGuardName()}, site.getGuardName(),
                _ -> facing(site, index), player -> {
                    RescueState state = RescueService.state(player);
                    return (state != null && state.infiltrating(site)) || guardsQuestline(site, index, player);
                }, false, false));
        this.site = site;
        this.index = index;
    }

    /**
     * The questline raids use the same posts: the Barbarian questline's Dean's-office theft is watched
     * by all ten Cathedral guards, the Mage questline's Bastion raid by the first two Bastion guards.
     */
    private static boolean guardsQuestline(RescueSite site, int index, SkyBlockPlayer player) {
        return switch (site) {
            case BARBARIAN -> CrimsonQuestlines.barbarian(player) == BarbarianStep.STEAL_RESEARCH;
            case MAGE -> index < 2 && CrimsonQuestlines.mage(player) == MageStep.RECOVER_RESEARCH;
        };
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
