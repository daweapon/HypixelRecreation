package dev.daweapon.type.catacombs.run;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;
import dev.daweapon.dungeons.catacombs.run.DungeonRoomState;
import dev.daweapon.type.catacombs.engine.DungeonEngine;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointRank;
import dev.daweapon.type.generic.tab.AreaServerModule;
import dev.daweapon.type.generic.tab.CustomTablistSkin;
import dev.daweapon.type.generic.tab.TablistManager;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkin;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.tabmodules.AccountInformationModule;
import dev.daweapon.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonClassProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The Catacombs server's tab list: the dungeon layout while a run is going, the usual columns otherwise. */
public final class CatacombsTabList extends TablistManager {
    @Override
    public List<TablistModule> getModules() {
        return new ArrayList<>(List.of(
                new SkyBlockPlayersOnlineModule(1),
                new SkyBlockPlayersOnlineModule(2),
                new AreaServerModule("tablist.server_info.area.catacombs"),
                new AccountInformationModule()));
    }

    @Override
    protected List<TablistModule.TablistEntry> buildEntries(HypixelPlayer player) {
        CatacombsRun run = player instanceof SkyBlockPlayer member ? CatacombsRuns.of(member.getUuid()) : null;
        if (run == null || run.lifecycle().stage() == CatacombsLifecycle.Stage.WAITING || run.lifecycle().state() == null) {
            return waitingColumns(player);
        }
        List<TablistModule.TablistEntry> entries = new ArrayList<>();
        List<String> slots = CatacombsTabLines.slots(snapshot(run));
        List<SkyBlockPlayer> members = run.onlineMembers();
        for (int index = 0; index < slots.size(); index++) {
            String line = slots.get(index);
            entries.add(line == null ? TablistModule.getGrayEntry()
                    : new TablistModule.TablistEntry(Text.of(line), skin(index, members)));
        }
        return entries;
    }

    private List<TablistModule.TablistEntry> waitingColumns(HypixelPlayer player) {
        List<TablistModule.TablistEntry> entries = new ArrayList<>();
        for (TablistModule module : getModules()) {
            List<TablistModule.TablistEntry> column = new ArrayList<>();
            try {
                column.addAll(module.getEntries(player));
            } catch (Exception exception) {
                reportOnce(module.getClass().getName(), exception);
            }
            entries.addAll(padded(column));
        }
        return entries;
    }

    static List<TablistModule.TablistEntry> padded(List<TablistModule.TablistEntry> column) {
        List<TablistModule.TablistEntry> rows = new ArrayList<>(column.subList(0, Math.min(column.size(), CatacombsTabLines.ROWS)));
        while (rows.size() < CatacombsTabLines.ROWS) rows.add(TablistModule.getGrayEntry());
        return rows;
    }

    private static TablistSkin skin(int index, List<SkyBlockPlayer> members) {
        if (index % CatacombsTabLines.ROWS == 0) return TablistSkinRegistry.CYAN;
        int member = CatacombsTabLines.memberAt(index);
        if (member >= 0 && member < members.size() && members.get(member).getSkin() != null)
            return new CustomTablistSkin(members.get(member).getSkin());
        return TablistSkinRegistry.GRAY;
    }

    static CatacombsTabLines.Snapshot snapshot(CatacombsRun run) {
        CatacombsRunState state = run.lifecycle().state();
        List<SkyBlockPlayer> members = run.onlineMembers();
        List<CatacombsTabLines.Member> party = members.stream().map(member -> member(run, member)).toList();
        List<SkyBlockPlayer> downed = members.stream().filter(member -> run.ghosts().isGhost(member.getUuid())).toList();
        int revive = downed.isEmpty() ? 0 : run.ghosts().secondsToRevive(downed.getFirst().getUuid());
        Map<Integer, DungeonRoomState> rooms = state.rooms();
        List<CatacombsTabLines.Puzzle> puzzles = run.arena().layout().roomsOfKind(RoomKind.PUZZLE).stream()
                .map(room -> puzzle(room, rooms))
                .toList();
        DungeonEngine engine = run.engine();
        int opened = engine == null ? 0 : engine.discoveredRooms();
        return new CatacombsTabLines.Snapshot(party, downed.stream().map(SkyBlockPlayer::getUsername).toList(), revive,
                state.deaths(), state.secretsFound(), state.config().totalSecrets(), state.crypts(), opened,
                state.completedRooms(), run.lifecycle().elapsedSeconds(), puzzles, state.blessings().levels());
    }

    private static CatacombsTabLines.Member member(CatacombsRun run, SkyBlockPlayer member) {
        var rank = member.getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue();
        DungeonClassType dungeonClass = run.classOf(member.getUuid());
        return new CatacombsTabLines.Member(member.getSkyBlockExperience().getLevel().asInt(), rank.getTextColor().asHexString(),
                member.getUsername(), dungeonClass == null ? "EMPTY" : dungeonClass.displayName(),
                dungeonClass == null ? 0 : DungeonClassProgress.level(member, dungeonClass),
                run.ghosts().isGhost(member.getUuid()));
    }

    private static CatacombsTabLines.Puzzle puzzle(PlacedRoom room, Map<Integer, DungeonRoomState> rooms) {
        String name = CatacombsPuzzle.ofTemplate(room.template().id()).map(CatacombsPuzzle::displayName).orElse(room.template().name());
        return new CatacombsTabLines.Puzzle(name, rooms.getOrDefault(room.index(), DungeonRoomState.UNDISCOVERED));
    }
}
