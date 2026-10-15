package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Hub-side entry point for Season of Jerry (Roadmap #20) - real Hypixel has an NPC/portal in the
// Hub during the event that transfers players to the Jerry's Workshop instance; this was entirely
// missing (no Hub NPC existed to reach the already-built type.jerrysworkshop module/server at
// all), reported by the user as "no npc for the season of jerry".
//
// Position (-29.5, 69, 14.5) continues the same Village pond NPC row used by NPCBaker/NPCOringo/
// NPCFearMongerer, 2 blocks past the Spooky Festival leaderboard hologram - an UNCONFIRMED guess,
// not sourced from a screenshot like those three were. Skin reuses NPCAdventurer's placeholder
// texture, same situation as the other event NPCs.
//
// Gated via visible(), not a position toggle, matching the fix applied to the other 3 event NPCs.
public class NPCJerry extends HypixelNPC {

    public NPCJerry() {
        super(new HumanConfiguration() {
            @Override
            public boolean visible(HypixelPlayer player) {
                return isSeasonActive();
            }

            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Jerry", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "FaHd91h2NJ5HnDgFbKaEDQDSd/mLzTDavHEo54LEBIwH3aJ4dzPigXp/UO19LjtHUpHHoC/6vOEzJSFhdR9SfOIzNCbWA7RoVFgkoNyIAYpdpf4M4ErtmzrhZiuWrKV4vTebAMaptKWtYY5LkuI0wHDG8SrhPL2rEbxVVDPjw3k0PRS7R1DaTAnmf8OqMHeWyJmWZ7KMAbOsQUORFvlUlPpc+DDurhXCw+iruIGCcwTo+t+RLyaGgfCwZoJYQTqkwmHIh2/rrzSG5Nn5k7Tou2+JY11ebfL7tu+DUlNFVGZ3HHbY7/p2rgPg9uNYxAb4JYeqZ8VmOSPDqqBjkI66h7LPzA7JdR7MwdddlAXiHN0aqwfsoOZ5P8l/XQrJbxg/2JmkF6MCPiD7781fQWKUmNe2BhdXKE1/rzlOMlpj4USfWg9JuLGWLFM5C33i8keJunmpcxUTK8ufU0xuiZDMNiZxdIOyq4eN/zcNbwj2XELIiQ7q1WPUC+CSY1AHWMapNpol460wQZoJP1vyFvv+gHrv64ugQaHXKOGmtKlHRlwoKtTuU9A61kd4DjfcIO0PMC9ct8LgWYO+uRZggVm830YGb+h0BoCF5pbVCiUk3Sth3y6w+joMzJtONLTj7fN87p4kkohIY/Ybtbc9wHHyKw561iFC6HXXemFlBdKaVpg=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTkxMzU4MzE3MzksInByb2ZpbGVJZCI6ImIwZDRiMjhiYzFkNzQ4ODlhZjBlODY2MWNlZTk2YWFiIiwicHJvZmlsZU5hbWUiOiJ4RmFpaUxlUiIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQzNGYzOGMxYmIxMDZlMTE5MDhhZDNjYzkwMTYyYzE4Yjg2M2Q2NzgyNjVjODRhODRhMzU4OTAzZjhmN2ExYyJ9fX0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-29.5, 69, 14.5, -90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    private static boolean isSeasonActive() {
        List<CalendarEvent> events = SkyBlockCalendar.getCurrentEvents();
        return events.contains(CalendarEvent.SEASON_OF_JERRY);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (!isSeasonActive()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        player.sendMessage("<e>Sending you to Jerry's Workshop...");
        player.sendTo(ServerType.SKYBLOCK_JERRYS_WORKSHOP);
    }
}
