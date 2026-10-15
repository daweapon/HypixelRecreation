package dev.daweapon.type.crimsonisle.town;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.List;

/** A talk-only NPC that walks between a fixed set of stops (see {@link Roamers}). */
public class RoamingNPC extends TalkNPC {
    private final Roamers.Route route;

    public RoamingNPC(String name, String texture, List<Pos> stops, Duration interval, String[] introduction, String[]... idle) {
        super(name, texture, stops.getFirst(), introduction, idle);
        this.route = Roamers.register(this, stops, interval);
    }

    public RoamingNPC(String name, EntityType entityType, List<Pos> stops, Duration interval, String[] introduction, String[]... idle) {
        super(name, entityType, stops.getFirst(), introduction, idle);
        this.route = Roamers.register(this, stops, interval);
    }

    @Override
    protected Pos position(SkyBlockPlayer player) {
        return route.current();
    }

    @Override
    protected boolean moves() {
        return true;
    }
}
