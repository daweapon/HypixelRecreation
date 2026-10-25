package dev.daweapon.type.skyblockgeneric.foresttemple;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.hologram.HologramEntity;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.gui.inventories.starborn.GUIStarbornAltar;
import dev.daweapon.type.skyblockgeneric.torrhus.StarbornBenefactors;
import dev.daweapon.type.skyblockgeneric.torrhus.StarbornTemple;
import dev.daweapon.type.skyblockgeneric.torrhus.TempleDoorState;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class ForestTemple {
    private static final int INTERIOR_MIN_X = -646, INTERIOR_MAX_X = -627, INTERIOR_MIN_Z = 69, INTERIOR_MAX_Z = 85;
    private static final int INTERIOR_MIN_Y = 56, INTERIOR_MAX_Y = 76;
    private static final int DOOR_MIN_X = -637, DOOR_MAX_X = -636, DOOR_MIN_Y = 60, DOOR_MAX_Y = 66, DOOR_Z = 68;
    private static final double DOOR_RANGE_SQUARED = 70 * 70;
    private static final int ALTAR_MIN_X = -640, ALTAR_MAX_X = -633, ALTAR_Y = 60, ALTAR_MIN_Z = 80, ALTAR_MAX_Z = 81;
    private static final String BANNER = "<5><l>" + "▬".repeat(40);

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();
    private static boolean altarHologramSpawned;

    private ForestTemple() {
    }

    private static final class Session {
        private final LanternPuzzle lanterns = new LanternPuzzle();
        private final TempleDoorState door = new TempleDoorState(DOOR_Z + 4.5, DOOR_Z + 8, DOOR_Z + 2.5);
        private boolean entrance;
        private FloorPuzzle floor;
    }

    public static boolean onGalatea() {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_GALATEA;
    }

    public static boolean insideTemple(Point position) {
        return position.x() >= INTERIOR_MIN_X && position.x() <= INTERIOR_MAX_X && position.z() >= INTERIOR_MIN_Z
                && position.z() <= INTERIOR_MAX_Z && position.y() >= INTERIOR_MIN_Y && position.y() <= INTERIOR_MAX_Y;
    }

    public static boolean isAltar(Point position) {
        return position.blockY() == ALTAR_Y && position.blockX() >= ALTAR_MIN_X && position.blockX() <= ALTAR_MAX_X
                && position.blockZ() >= ALTAR_MIN_Z && position.blockZ() <= ALTAR_MAX_Z;
    }

    public static boolean handle(SkyBlockPlayer player, Point position, Block block) {
        if (!onGalatea() || player.getSkyblockDataHandler() == null) return false;
        if (block.name().contains("lantern")) {
            int lantern = LanternPuzzle.indexOf(position.blockX(), position.blockY(), position.blockZ());
            if (lantern < 0) return false;
            pressLantern(player, lantern);
            return true;
        }
        if (block.name().contains("orange_glazed_terracotta") && position.blockY() == FloorPuzzle.FLOOR_Y) {
            int cell = FloorPuzzle.cellAt(position.blockX(), position.blockZ());
            if (cell < 0) return false;
            rotate(player, cell);
            return true;
        }
        if (isAltar(position) && block.id() != Block.AIR.id()) {
            if (!StarbornBenefactors.activated(player, StarbornTemple.FOREST)) {
                player.sendMessage("<c>The altar lies dormant. <7>Awaken the temple first.");
            } else {
                GUIStarbornAltar.open(player, StarbornTemple.FOREST);
            }
            return true;
        }
        return false;
    }

    private static void pressLantern(SkyBlockPlayer player, int lantern) {
        Session session = SESSIONS.computeIfAbsent(player.getUuid(), _ -> new Session());
        switch (session.lanterns.press(lantern)) {
            case SOLVED -> {
                session.entrance = true;
                player.sendMessage("<7><o>The guardian of the temple opens the door...");
            }
            case WRONG -> player.sendMessage("<c>The lanterns flicker and go dark.");
            case PROGRESS -> player.sendMessage("<a>The lantern glows. <7>({}/{})", session.lanterns.progress(), LanternPuzzle.SEQUENCE.size());
        }
    }

    public static void register(Scheduler scheduler) {
        scheduler.scheduleTask(ForestTemple::tick, TaskSchedule.seconds(1), TaskSchedule.seconds(1), ExecutionType.TICK_END);
    }

    private static void tick() {
        if (!onGalatea()) return;
        spawnAltarHologram();
        List<UUID> online = new ArrayList<>();
        SkyBlockGenericLoader.forEachLoadedPlayer(player -> {
            online.add(player.getUuid());
            if (player.getSkyblockDataHandler() == null || player.getInstance() == null) return;
            Session session = SESSIONS.get(player.getUuid());
            if (session != null) updateDoor(player, session);
            boolean inside = session != null && session.entrance && insideTemple(player.getPosition());
            boolean activated = StarbornBenefactors.activated(player, StarbornTemple.FOREST);
            if (inside && activated) showFloor(player, FloorPuzzle.solvedLayout());
            if (inside && !activated) {
                if (session.floor == null) session.floor = FloorPuzzle.scrambled(ThreadLocalRandom.current());
                showFloor(player, session);
            }
            if (!inside && session != null) session.floor = null;
        });
        SESSIONS.keySet().removeIf(id -> !online.contains(id));
    }

    private static void spawnAltarHologram() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (altarHologramSpawned || instance == null) return;
        altarHologramSpawned = true;
        HologramEntity title = new HologramEntity("<a><l>TEMPLE ALTAR");
        title.setInstance(instance, new Pos(-636.5, 62.6, 80.5));
        HologramEntity hint = new HologramEntity("<e><l>Click to view!");
        hint.setInstance(instance, new Pos(-636.5, 62.2, 80.5));
    }

    private static boolean nearDoor(Point position) {
        double dx = position.x() - (DOOR_MIN_X + DOOR_MAX_X) / 2.0;
        double dy = position.y() - DOOR_MIN_Y;
        double dz = position.z() - DOOR_Z;
        return dx * dx + dy * dy + dz * dz <= DOOR_RANGE_SQUARED;
    }

    private static void updateDoor(SkyBlockPlayer player, Session session) {
        if (!nearDoor(player.getPosition())) {
            SESSIONS.remove(player.getUuid());
            return;
        }
        TempleDoorState.Change change = session.door.update(session.entrance, false, player.getPosition().z());
        if (change == TempleDoorState.Change.SLAMMED) {
            player.sendMessage("<7><o>The door slams down behind you...");
        }
        boolean open = session.door.state() == TempleDoorState.State.OPEN;
        if (open || change != TempleDoorState.Change.NONE) showDoor(player, open);
    }

    private static void showDoor(SkyBlockPlayer player, boolean open) {
        for (int x = DOOR_MIN_X; x <= DOOR_MAX_X; x++) {
            for (int y = DOOR_MIN_Y; y <= DOOR_MAX_Y; y++) {
                Pos position = new Pos(x, y, DOOR_Z);
                Block shown = open ? Block.AIR : player.getInstance().getBlock(position);
                player.sendPacket(new BlockChangePacket(position, shown));
            }
        }
    }

    private static void showFloor(SkyBlockPlayer player, Session session) {
        FloorPuzzle.Facing[] layout = new FloorPuzzle.Facing[FloorPuzzle.CELLS];
        for (int i = 0; i < layout.length; i++) layout[i] = session.floor.facing(i);
        showFloor(player, layout);
    }

    private static void showFloor(SkyBlockPlayer player, FloorPuzzle.Facing[] layout) {
        for (int cell = 0; cell < layout.length; cell++) sendCell(player, cell, layout[cell]);
    }

    private static void sendCell(SkyBlockPlayer player, int cell, FloorPuzzle.Facing facing) {
        Pos position = new Pos(FloorPuzzle.x(cell), FloorPuzzle.FLOOR_Y, FloorPuzzle.z(cell));
        player.sendPacket(new BlockChangePacket(position, Block.ORANGE_GLAZED_TERRACOTTA.withProperty("facing", facing.property())));
    }

    private static void rotate(SkyBlockPlayer player, int cell) {
        Session session = SESSIONS.get(player.getUuid());
        if (session == null || session.floor == null || StarbornBenefactors.activated(player, StarbornTemple.FOREST)) return;
        session.floor.rotate(cell);
        sendCell(player, cell, session.floor.facing(cell));
        if (session.floor.solved()) activate(player, session);
    }

    private static void activate(SkyBlockPlayer player, Session session) {
        session.floor = null;
        player.getHuntingData().setCounter(StarbornTemple.FOREST.activeKey(), 1);
        player.sendMessage("<7><o>The carpet clicks into place and the altar awakens...");
        player.sendMessage(BANNER);
        player.sendMessage("<f>                            <d><l>STARBORN TEMPLE");
        player.sendMessage("");
        player.sendMessage("<f>               <a>The <e>Forest Starborn Temple <a>is activated!");
        player.sendMessage("<f>    You now permanently gain the passive effect from its <d>Will<f>.");
        player.sendMessage("<f>    Visit the <a>Altar <f>to become a <9>Benefactor <f>for bonus effects!");
        player.sendMessage(BANNER);
        ChapterTriggers.fire(player, "starborn.activate.forest");
    }
}
