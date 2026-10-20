package dev.daweapon.type.skyblockgeneric.torrhus;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class TorrhusZoneTips {
    private static final Map<RegionType, List<String>> TIPS = new EnumMap<>(RegionType.class);

    static {
        tips(RegionType.TORRHUS_CANYON, "Talk to <2>Helia <f>to begin your <6>Torrhus Chapters<f>.",
                "Cut down <6>Helix Trees<f>.", "Loot <6>Honeyhives<f>.");
        tips(RegionType.TORRHUS_HEIGHTS, "<e>Hunt <f>critters that live high up.");
        tips(RegionType.MIRIAS_HUT, "Take part in <d>Miria’s Contest <f>each SkyBlock Day.",
                "Progress through <a>Miria’s Milestones<f>.",
                "Obtain <a>Safari Tickets <f>and other rewards from <b>Miria<f>.",
                "Purchase useful items from <a>Miria’s Shop<f>.");
        tips(RegionType.SPRING_PATH, "Climb up to the peak of <b>Torrhus Springs<f>.",
                "Meet <b>Miria <f>and partake in <d>Miria's Contests<f>.",
                "Keep an eye out for pesky <7>Mountain Goats<f>!");
        tips(RegionType.TORRHUS_SPRINGS, "Go fishing for <6>Torrhus Sea Creatures<f>.",
                "Dive down into <b>Torrhus Springs<f>.",
                "Hunt <f>mobs that live in and around the <b>Springs<f>!");
        tips(RegionType.SPRING_SHALLOWS, "Gather more <a>Sea Lumies<f>.", "Use your <9>Fishing Net <f>to <e>Hunt<f>.",
                "Swim down deeper into the <9>Spring Depths<f>.");
        tips(RegionType.SPRING_DEPTHS, "Gather more <a>Sea Lumies<f>.", "Use your <9>Fishing Net <f>to <e>Hunt<f>.",
                "Swim down deeper into the <9>Spring Depths<f>.");
        tips(RegionType.ANTS_CAVE, "Break <e>Anthills <f>and vacuum up <e>Ants<f>!",
                "Purchase <6>Minion Upgrade Stones <f>from <2>Ant<f>.",
                "Keep an eye out for the elusive <d>Queen Ant<f>!");
        tips(RegionType.HOTSPOT_HAVEN, "Take some time to gather your breath.",
                "Fish in the <d>Fishing Hotspot <f>for <9>Giant Water Bug <f>shards.");
        tips(RegionType.DESERT_TEMPLE, "The <b>Altar <f>of an ancient being.");
        tips(RegionType.PANGOLIN_HIDEAWAY);
        tips(RegionType.CRITTER_SAFARI_ENTRANCE, "<7>Talk to the <2>Safari Manager <7>to enter the <2>Critter Safari<7>.",
                "<7>Talk to <a>Andrea <7>to view your <2>Safari Milestones<7>.",
                "<7>Talk to <6>Archie <7>to view the <2>Safari Essence Shop<7>.");
    }

    private TorrhusZoneTips() {
    }

    private static void tips(RegionType zone, String... lines) {
        TIPS.put(zone, List.of(lines));
    }

    public static @Nullable Text[] of(RegionType zone) {
        List<String> lines = TIPS.get(zone);
        return lines == null ? null : lines.stream().map(Text::of).toArray(Text[]::new);
    }
}
