package dev.daweapon.type.skyblockgeneric.event.custom;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/** A Salvation beam passed through entities that are not SkyBlock mobs, for modes with their own bosses to score it. */
public record SalvationBeamEvent(SkyBlockPlayer shooter, List<Entity> targets, double damage) implements Event {
}
