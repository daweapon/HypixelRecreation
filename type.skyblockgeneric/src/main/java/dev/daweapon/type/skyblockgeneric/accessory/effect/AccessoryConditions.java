package dev.daweapon.type.skyblockgeneric.accessory.effect;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public record AccessoryConditions(Set<ServerType> servers, Set<RegionType> regions, @Nullable TimeOfDay time) {
    public static final AccessoryConditions ALWAYS = new AccessoryConditions(Set.of(), Set.of(), null);

    public enum TimeOfDay {
        DAY, NIGHT;

        boolean isNow() {
            int hour = SkyBlockCalendar.getHour();
            boolean day = hour >= 6 && hour < 20;
            return this == DAY ? day : !day;
        }
    }

    public boolean isUnconditional() {
        return servers.isEmpty() && regions.isEmpty() && time == null;
    }

    public boolean test(SkyBlockPlayer player) {
        if (!servers.isEmpty()) {
            if (HypixelConst.getTypeLoader() == null || !servers.contains(HypixelConst.getTypeLoader().getType())) return false;
        }
        if (!regions.isEmpty()) {
            SkyBlockRegion region = player.getRegion();
            if (region == null || !regions.contains(region.getType())) return false;
        }
        return time == null || time.isNow();
    }
}
