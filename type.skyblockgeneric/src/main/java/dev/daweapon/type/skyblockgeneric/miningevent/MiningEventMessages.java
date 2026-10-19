package dev.daweapon.type.skyblockgeneric.miningevent;

import java.util.List;

public final class MiningEventMessages {
    private static final String DASHLINE = "<a><l>" + "▬".repeat(36);

    private MiningEventMessages() {
    }

    public static void banner(MiningEventType type, String title, String subtitle, List<String> lines) {
        MiningEvents.broadcast(DASHLINE);
        MiningEvents.broadcast("                 " + type.color() + "<l>" + title);
        if (subtitle != null) MiningEvents.broadcast("                       <7>" + subtitle);
        MiningEvents.broadcast(" ");
        lines.forEach(MiningEvents::broadcast);
        MiningEvents.broadcast(DASHLINE);
    }

    public static void warning(MiningEventType type, MiningEventZone zone, int seconds, boolean teleportLine) {
        MiningEvents.broadcast(type.color() + "⚑ <e>The " + type.coloredName() + "<e> event starts in <a>" + seconds + "<e> seconds!");
        if (type.kind() == MiningEventKind.PASSIVE) {
            MiningEvents.broadcast("<e>This is a passive event! " + type.color() + "It's happening everywhere in " + zone.phrase() + "!");
        } else if (teleportLine) {
            MiningEvents.broadcast("<hover:'<e>Teleport to Old Man Garry'><click:run:'/tptogarry'><e><l>Click here to teleport to Garry and prepare!</click></hover>");
        }
    }
}
