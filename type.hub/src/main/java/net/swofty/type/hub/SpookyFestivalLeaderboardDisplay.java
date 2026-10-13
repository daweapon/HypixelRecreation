package net.swofty.type.hub;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIFearMongerer;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.tinylog.Logger;

// The floating candy-contest leaderboard hologram next to the Fear Mongerer (Roadmap #19) -
// user-confirmed via screenshot: real Hypixel shows top 10 + your own rank below the NPC's
// hologram, with "Resets each event." and "Click to view rewards!" (clicking the NPC itself
// opens GUIFearMongerer - the prompt text isn't a separately-clickable object).
//
// Placed beside NPCFearMongerer (-29.5, 69, 10.5), not floating directly above it -
// user-corrected after the first placement stacked it on top of the NPC.
//
// Clicking the hologram (not the NPC - clicking NPCFearMongerer opens the candy shop instead,
// GUIFearMongererShop) opens the tier reward menu, via a shared InteractionEntity layered over
// the text - PlayerHolograms' marker armor stands have no hitbox, so a plain hologram can't
// receive clicks on its own (see GlassDisplay for the same interaction-entity-over-text pattern).
//
// Offline entries resolve their name from the account's stored IGN once and are cached, since
// the hologram refreshes far more often than names change.
public class SpookyFestivalLeaderboardDisplay {

    private static final Pos DISPLAY_POS = new Pos(-29.5, 70, 12.5);
    private static final int TOP_N = 10;

    private static final Map<HypixelPlayer, PlayerHolograms.ExternalPlayerHologram> holos = new ConcurrentHashMap<>();
    private static InteractionEntity clickTarget;
    private static final Map<UUID, String> names = new ConcurrentHashMap<>();

    public static void addAndUpdate() {
        Set<UUID> loadedUuids = HypixelGenericLoader.getLoadedPlayers().stream()
                .map(HypixelPlayer::getUuid)
                .collect(Collectors.toSet());
        holos.keySet().removeIf(p -> !loadedUuids.contains(p.getUuid()));

        boolean active = SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.SPOOKY_FESTIVAL);
        if (!active) {
            holos.values().forEach(PlayerHolograms::removeExternalPlayerHologram);
            holos.clear();
            if (clickTarget != null) {
                clickTarget.remove();
                clickTarget = null;
            }
            return;
        }

        if (clickTarget == null) {
            clickTarget = new InteractionEntity(1.2f, 2.4f, (player, _) -> {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                skyBlockPlayer.openView(new GUIFearMongerer(), GUIFearMongerer.State.forPlayer(skyBlockPlayer));
            });
            clickTarget.setInstance(HypixelConst.getInstanceContainer(), DISPLAY_POS.add(0, 1, 0));
        }

        String leaderboardKey = "spooky_festival_" + SkyBlockCalendar.getYear();
        List<LeaderboardService.LeaderboardEntry> top = LeaderboardService.getTopN(leaderboardKey, TOP_N);

        HypixelGenericLoader.forEachLoadedPlayer(player -> {
            List<String> message = new ArrayList<>(List.of(
                    "<6><l>" + StringUtility.ntify(SkyBlockCalendar.getYear()) + " SPOOKY FESTIVAL",
                    "<f>Candy Contest",
                    " "
            ));

            for (LeaderboardService.LeaderboardEntry entry : top) {
                message.add(rankLine(entry));
            }

            LeaderboardService.LeaderboardEntry own = LeaderboardService.getPlayerRankEntry(leaderboardKey, player.getUuid());
            if (own != null && own.rank() > TOP_N) {
                message.add(" ");
                message.add(rankLine(own));
            }

            message.add(" ");
            message.add("<7>Resets each event.");
            message.add("<e>Click to view rewards!");

            if (!holos.containsKey(player)) {
                PlayerHolograms.ExternalPlayerHologram holo = PlayerHolograms.ExternalPlayerHologram.builder()
                        .player(player)
                        .text(message.toArray(new String[0]))
                        .pos(DISPLAY_POS)
                        .spacing(0.3d)
                        .instance(player.getInstance())
                        .build();

                PlayerHolograms.addExternalPlayerHologram(holo);
                holos.put(player, holo);
                return;
            }

            PlayerHolograms.updateExternalPlayerHologramText(holos.get(player), message.toArray(new String[0]));
        });
    }

    private static String rankLine(LeaderboardService.LeaderboardEntry entry) {
        String color = switch (entry.rank()) {
            case 1 -> "<6>";
            case 2 -> "<7>";
            case 3 -> "<c>";
            default -> "<f>";
        };
        String name = names.computeIfAbsent(entry.playerUuid(), SpookyFestivalLeaderboardDisplay::resolveName);
        if (name == null) name = entry.playerUuid().toString();
        return entry.rank() + ". " + color + name + " <7>- <f>" + StringUtility.commaify(entry.scoreAsLong());
    }

    private static String resolveName(UUID uuid) {
        try {
            String name = HypixelPlayer.getRawName(uuid);
            if (name != null && !name.isBlank()) return name;
        } catch (RuntimeException e) {
            Logger.warn("Could not resolve a leaderboard name for {}: {}", uuid, e.getMessage());
        }
        return null;
    }
}
