package dev.daweapon.type.crimsonisle.quest.rescue;

import lombok.Getter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.quest.FactionQuestTier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The two Rescue Mission venues, keyed by the faction that sends you: Mages sneak into The Bastion, Barbarians
 * into the Cathedral. Recruiter, Undercover Agent and the Cathedral guard posts are wiki positions; the Bastion
 * guards beyond the wiki's two, every hostage spot, the bells and the venue bounds are estimates checked
 * against the world.
 */
@Getter
public enum RescueSite {
    MAGE(CrimsonFaction.MAGE, "<2>The Bastion", "<c>Piglin Guard", EntityType.PIGLIN_BRUTE,
            new Pos(-120.5, 92, -751.5, 180, 0), new Pos(-624.5, 120, -961.5, 135, 0),
            Map.of(FactionQuestTier.C, new Pos(-638.5, 124, -982.5),
                    FactionQuestTier.B, new Pos(-675.5, 126, -1001.5),
                    FactionQuestTier.A, new Pos(-651.5, 123, -1060.5)),
            List.of(new Pos(-676.5, 126, -985.5), new Pos(-652.5, 124, -1053.5), new Pos(-644.5, 123, -974.5),
                    new Pos(-629.5, 124, -991.5), new Pos(-668.5, 126, -1010.5), new Pos(-659.5, 124, -1044.5),
                    new Pos(-643.5, 124, -1048.5)),
            List.of(new Pos(-640, 123, -979), new Pos(-659, 124, -1037)),
            new Pos(-720, 95, -1075), new Pos(-600, 175, -940)),
    BARBARIAN(CrimsonFaction.BARBARIAN, "<5>Cathedral", "<5>Mage Guard", null,
            new Pos(-581.5, 100, -690.5, 0, 0), new Pos(-15, 93, -845, 180, 0),
            Map.of(FactionQuestTier.D, new Pos(-39.5, 114, -889.5),
                    FactionQuestTier.C, new Pos(-8.5, 114, -924.5),
                    FactionQuestTier.B, new Pos(-69.5, 114, -944.5),
                    FactionQuestTier.A, new Pos(-94.5, 130, -916.5)),
            List.of(new Pos(-94.5, 130, -912), new Pos(-91.5, 130, -911.5), new Pos(-44.5, 114, -877.5),
                    new Pos(-37.5, 114, -879.5), new Pos(-30.5, 114, -878.5), new Pos(-8.5, 114, -896.5),
                    new Pos(2.5, 114, -929.5), new Pos(-12.5, 114, -931.5), new Pos(-70.5, 114, -949.5),
                    new Pos(-67.5, 114, -894.5)),
            List.of(new Pos(-25, 114, -890), new Pos(-60, 114, -935)),
            new Pos(-110, 85, -960), new Pos(30, 175, -838)),
    ;

    private final CrimsonFaction faction;
    private final String venueName;
    private final String guardName;
    private final EntityType guardEntity;
    private final Pos recruiter;
    private final Pos agent;
    private final Map<FactionQuestTier, Pos> hostageSpots;
    private final List<Pos> guardPosts;
    private final List<Pos> bells;
    private final Pos min;
    private final Pos max;

    RescueSite(CrimsonFaction faction, String venueName, String guardName, EntityType guardEntity, Pos recruiter,
               Pos agent, Map<FactionQuestTier, Pos> hostageSpots, List<Pos> guardPosts, List<Pos> bells, Pos min, Pos max) {
        this.faction = faction;
        this.venueName = venueName;
        this.guardName = guardName;
        this.guardEntity = guardEntity;
        this.recruiter = recruiter;
        this.agent = agent;
        this.hostageSpots = hostageSpots;
        this.guardPosts = guardPosts;
        this.bells = bells;
        this.min = min;
        this.max = max;
    }

    public boolean contains(Point point) {
        return point.x() >= min.x() && point.x() <= max.x() && point.y() >= min.y() && point.y() <= max.y()
                && point.z() >= min.z() && point.z() <= max.z();
    }

    public Pos hostageSpot(@Nullable FactionQuestTier tier) {
        Pos spot = tier == null ? null : hostageSpots.get(tier);
        return spot != null ? spot : hostageSpots.values().iterator().next();
    }

    /** The way a guard faces when it isn't sweeping: toward the venue's way in, where the agent waits. */
    public float guardYaw(Pos post) {
        double dx = agent.x() - post.x();
        double dz = agent.z() - post.z();
        return (float) Math.toDegrees(-Math.atan2(dx, dz));
    }

    public static RescueSite of(CrimsonFaction faction) {
        return faction == CrimsonFaction.MAGE ? MAGE : BARBARIAN;
    }
}
