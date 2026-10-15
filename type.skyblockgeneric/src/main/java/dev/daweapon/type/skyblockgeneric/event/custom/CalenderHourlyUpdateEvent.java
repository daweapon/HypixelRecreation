package dev.daweapon.type.skyblockgeneric.event.custom;

import net.minestom.server.event.Event;

// Fired once per in-game hour by the SkyBlock calendar
public record CalenderHourlyUpdateEvent(int hour) implements Event {

}
