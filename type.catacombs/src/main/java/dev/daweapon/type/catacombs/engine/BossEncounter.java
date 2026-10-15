package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.dungeons.catacombs.run.DungeonRunPhase;
import dev.daweapon.type.catacombs.run.CatacombsMessages;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * The placeholder boss: a portal in the cleared Blood Room takes the party to a plain arena where the floor's boss has
 * its registry health in two phases, the second coming back at full health. Real fights replace this per floor.
 */
final class BossEncounter implements CatacombsMob.Listener {
    private static final int PORTAL_RADIUS = 1;
    private static final double PORTAL_REACH = 2.0;
    private static final int ARRIVAL_DELAY_TICKS = 60;
    private static final int GAP_TO_ARENA = 20;

    private final DungeonHost host;
    private final CatacombsFloor floor;
    private final PlacedRoom bloodRoom;
    private final BossArenaBox box;
    private final DungeonMobDefinition bossDefinition;
    private @Nullable Pos portal;
    private boolean entered;
    private int phase;
    private boolean won;

    BossEncounter(DungeonHost host, CatacombsFloor floor, PlacedRoom bloodRoom) {
        this.host = host;
        this.floor = floor;
        this.bloodRoom = bloodRoom;
        this.box = new BossArenaBox(host.arena().plan().maxX() + GAP_TO_ARENA, CellGeometry.ORIGIN_Z, CellGeometry.FLOOR_Y);
        this.bossDefinition = DungeonMobRegistry.defaults().mob(bossId(floor));
    }

    private static String bossId(CatacombsFloor floor) {
        return switch (floor) {
            case ENTRANCE, FLOOR_ONE -> "BONZO";
            case FLOOR_TWO -> "SCARF";
            case FLOOR_THREE -> "PROFESSOR";
            case FLOOR_FOUR -> "THORN";
            case FLOOR_FIVE -> "LIVID";
            case FLOOR_SIX -> "SADAN";
            case FLOOR_SEVEN -> "NECRON";
        };
    }

    void openPortal() {
        if (portal != null) return;
        Pos centre = host.arena().roomCentre(bloodRoom);
        portal = centre.add(0, 0, 4);
        for (int x = -PORTAL_RADIUS; x <= PORTAL_RADIUS; x++) {
            for (int z = -PORTAL_RADIUS; z <= PORTAL_RADIUS; z++) {
                host.instance().setBlock(portal.blockX() + x, CellGeometry.FLOOR_Y, portal.blockZ() + z, Block.END_PORTAL);
            }
        }
        host.broadcast(CatacombsMessages.portalOpened());
    }

    void poll() {
        if (portal == null || entered) return;
        for (SkyBlockPlayer player : host.alivePlayers()) {
            double dx = player.getPosition().x() - (portal.x());
            double dz = player.getPosition().z() - (portal.z());
            if (Math.sqrt(dx * dx + dz * dz) <= PORTAL_REACH && player.getPosition().y() < CellGeometry.FLOOR_Y + 3) {
                enter();
                return;
            }
        }
    }

    synchronized void enter() {
        if (entered) return;
        entered = true;
        host.advanceTo(DungeonRunPhase.BOSS);
        box.build(host.instance());
        for (SkyBlockPlayer player : host.alivePlayers()) player.teleport(box.spawn());
        host.broadcast(CatacombsMessages.bossIntro(floor));
        host.later(ARRIVAL_DELAY_TICKS, () -> spawnBoss(1));
    }

    private synchronized void spawnBoss(int number) {
        phase = number;
        CatacombsMob boss = new CatacombsMob(bossDefinition, -1, false, this);
        boss.setInstance(host.instance(), box.bossSpot());
    }

    @Override
    public synchronized void mobDied(CatacombsMob mob) {
        if (phase == 1) {
            host.broadcast(CatacombsMessages.bossSecondPhase(floor));
            host.later(40, () -> spawnBoss(2));
            return;
        }
        if (won) return;
        won = true;
        host.broadcast(CatacombsMessages.bossDefeated(floor));
        host.later(1, host::win);
    }

    boolean entered() {
        return entered;
    }
}
