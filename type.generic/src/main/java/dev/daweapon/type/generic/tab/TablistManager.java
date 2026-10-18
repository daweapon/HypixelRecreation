package dev.daweapon.type.generic.tab;

import net.kyori.adventure.text.Component;
import org.tinylog.Logger;
import net.minestom.server.color.TeamColor;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.color.TeamColor;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.text.HypixelTextRenderer;
import dev.daweapon.type.generic.text.RenderContext;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TablistManager {
    private static final Map<HypixelPlayer, PlayerTabCache> tablistEntries = new ConcurrentHashMap<>();
    private static final Set<String> reportedFailures = ConcurrentHashMap.newKeySet();

    private static final class PlayerTabCache {
        private final List<UUID> tabEntries = new ArrayList<>();
        private final List<RenderedEntry> rendered = new ArrayList<>();
        private final Set<String> createdTeams = new HashSet<>();
    }

    private record RenderedEntry(Component name, TablistSkin skin) {
    }

    public abstract List<TablistModule> getModules();

    protected List<TablistModule.TablistEntry> buildEntries(HypixelPlayer player) {
        List<TablistModule.TablistEntry> entries = new ArrayList<>();
        for (TablistModule module : getModules()) {
            try {
                entries.addAll(module.getEntries(player));
            } catch (Exception exception) {
                reportOnce(module.getClass().getName(), exception);
            }
        }
        return entries;
    }

    protected static void reportOnce(String source, Exception exception) {
        if (reportedFailures.add(source)) {
            Logger.error(exception, "Tablist module {} failed", source);
        }
    }

    public void deleteTablistEntries(HypixelPlayer player) {
        tablistEntries.remove(player);
    }

    public void nullifyCache(HypixelPlayer player) {
        PlayerTabCache cache = tablistEntries.get(player);
        if (cache != null && !cache.tabEntries.isEmpty()) {
            player.sendPacket(new PlayerInfoRemovePacket(cache.tabEntries));
        }
        if (cache != null) {
            cache.tabEntries.clear();
            cache.rendered.clear();
        }
    }

    public void runScheduler(Scheduler scheduler) {
        scheduler.scheduleTask(() -> {
            HypixelGenericLoader.forEachLoadedPlayer(player -> {
                PlayerTabCache cache = tablistEntries.computeIfAbsent(player, ignored -> new PlayerTabCache());
                List<TablistModule.TablistEntry> entries = buildEntries(player);
                RenderContext renderContext = RenderContext.of(player);

                for (int slotIndex = 0; slotIndex < entries.size(); slotIndex++) {
                    TablistModule.TablistEntry entry = entries.get(slotIndex);
                    RenderedEntry desired = new RenderedEntry(
                            HypixelTextRenderer.render(entry.content().asComponent(), renderContext), entry.registry());

                    if (slotIndex < cache.rendered.size()) {
                        RenderedEntry current = cache.rendered.get(slotIndex);
                        if (current.equals(desired)) continue;
                        UUID uuid = cache.tabEntries.get(slotIndex);
                        cache.rendered.set(slotIndex, desired);
                        if (current.skin() == desired.skin()) {
                            player.sendPacket(new PlayerInfoUpdatePacket(
                                    PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                                    new PlayerInfoUpdatePacket.Entry(uuid, "", List.of(), true, 0,
                                            GameMode.CREATIVE, desired.name(), null, 1, true)));
                            continue;
                        }
                        player.sendPacket(new PlayerInfoRemovePacket(List.of(uuid)));
                        sendEntry(player, cache, slotIndex, uuid, desired);
                        continue;
                    }

                    UUID uuid = UUID.nameUUIDFromBytes((player.getUuid() + "#tab#" + slotIndex)
                            .getBytes(StandardCharsets.UTF_8));
                    cache.tabEntries.add(uuid);
                    cache.rendered.add(desired);
                    sendEntry(player, cache, slotIndex, uuid, desired);
                }

                if (cache.rendered.size() > entries.size()) {
                    List<UUID> stale = new ArrayList<>(cache.tabEntries.subList(entries.size(), cache.tabEntries.size()));
                    player.sendPacket(new PlayerInfoRemovePacket(stale));
                    cache.tabEntries.subList(entries.size(), cache.tabEntries.size()).clear();
                    cache.rendered.subList(entries.size(), cache.rendered.size()).clear();
                }
            });
        }, TaskSchedule.seconds(5), TaskSchedule.seconds(3), ExecutionType.TICK_END);
    }

    private void sendEntry(HypixelPlayer player, PlayerTabCache cache, int slotIndex, UUID uuid, RenderedEntry entry) {
        String teamName = getTeamName(slotIndex);
        String fakeProfileName = getFakeProfileName(slotIndex);

        List<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();
        properties.add(new PlayerInfoUpdatePacket.Property(
                "textures",
                entry.skin().getTexture(),
                entry.skin().getSignature()));

        if (cache.createdTeams.add(teamName)) {
            player.sendPacket(new TeamsPacket(teamName, new TeamsPacket.CreateTeamAction(
                    new TeamsPacket.Settings(
                            Component.text(teamName),
                            Component.text(teamName),
                            Component.empty(),
                            TeamsPacket.NameTagVisibility.ALWAYS,
                            TeamsPacket.CollisionRule.ALWAYS,
                            TeamColor.RED,
                            (byte) 0x01
                    ),
                    new ArrayList<>(Collections.singletonList(fakeProfileName))
            )));
        }

        player.sendPacket(new PlayerInfoUpdatePacket(EnumSet.of(
                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                PlayerInfoUpdatePacket.Action.UPDATE_LISTED
        ), Collections.singletonList(new PlayerInfoUpdatePacket.Entry(
                uuid,
                fakeProfileName,
                properties,
                true,
                0,
                GameMode.CREATIVE,
                entry.name(),
                null,
                1, true))));
    }

    private static String getTeamName(int slotIndex) {
        return String.format(Locale.ROOT, "TAB%03d", slotIndex);
    }

    private static String getFakeProfileName(int slotIndex) {
        return "tab" + Integer.toString(slotIndex, 36);
    }

}
