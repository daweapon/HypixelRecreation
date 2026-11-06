package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobDefinition;
import dev.daweapon.dungeons.catacombs.mob.DungeonMobRegistry;
import dev.daweapon.dungeons.catacombs.run.DungeonRunPhase;
import dev.daweapon.type.catacombs.arena.BossArena;
import dev.daweapon.type.catacombs.arena.RoomFeatures;
import dev.daweapon.type.catacombs.engine.bonzo.BonzoArenaSpots;
import dev.daweapon.type.catacombs.engine.bonzo.BonzoFight;
import dev.daweapon.type.catacombs.engine.look.MobLooks;
import dev.daweapon.type.catacombs.run.CatacombsMessages;
import dev.daweapon.type.catacombs.run.ScoreScreen;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The way to the boss and the fight. When the Watcher is done, nether portal blocks fill the obsidian frame on the far
 * side of the Blood Room (turned with the room) with a lightning strike, thunder and a bang, as captured; stepping into
 * it takes the party to the boss. Floor I has its captured arena and {@link BonzoFight}; every other floor still uses a
 * plain arena where its boss has its registry health in two lives.
 */
final class BossEncounter implements CatacombsMob.Listener {
    private static final int FALLBACK_PORTAL_RADIUS = 1;
    private static final int ARRIVAL_DELAY_TICKS = 60;
    private static final int GAP_TO_ARENA = 20;
    private static final Sound THUNDER = Sound.sound(Key.key("entity.lightning_bolt.thunder"), Sound.Source.WEATHER, 10000f, 0.8571429f);
    private static final Sound BANG = Sound.sound(Key.key("entity.generic.explode"), Sound.Source.BLOCK, 2f, 0.63492066f);

    private final DungeonHost host;
    private final CatacombsFloor floor;
    private final PlacedRoom bloodRoom;
    private final BossArenaBox box;
    private final DungeonMobDefinition bossDefinition;
    private final Optional<BossArena> realArena;
    private final List<int[]> portal = new ArrayList<>();
    private final List<SkyBlockPlayer> left = new ArrayList<>();
    private BonzoFight bonzo;
    private boolean portalOpen;
    private boolean entered;
    private int phase;
    private boolean won;

    BossEncounter(DungeonHost host, CatacombsFloor floor, PlacedRoom bloodRoom) {
        this.host = host;
        this.floor = floor;
        this.bloodRoom = bloodRoom;
        this.box = new BossArenaBox(host.arena().plan().maxX() + GAP_TO_ARENA, CellGeometry.ORIGIN_Z, CellGeometry.FLOOR_Y);
        this.bossDefinition = DungeonMobRegistry.defaults().mob(bossId(floor));
        this.realArena = floor == CatacombsFloor.FLOOR_ONE ? host.arena().bossArena() : Optional.empty();
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
        if (portalOpen) return;
        portalOpen = true;
        List<int[]> frame = host.arena().blocks().isReal(bloodRoom) ? RoomFeatures.shared().portal(bloodRoom) : List.of();
        Block block;
        if (frame.isEmpty()) {
            Pos centre = host.arena().roomCentre(bloodRoom).add(0, 0, 4);
            for (int x = -FALLBACK_PORTAL_RADIUS; x <= FALLBACK_PORTAL_RADIUS; x++) {
                for (int z = -FALLBACK_PORTAL_RADIUS; z <= FALLBACK_PORTAL_RADIUS; z++) {
                    portal.add(new int[]{centre.blockX() + x, centre.blockY() - 1, centre.blockZ() + z});
                }
            }
            block = Block.END_PORTAL;
        } else {
            portal.addAll(frame);
            block = Block.NETHER_PORTAL.withProperty("axis", RoomFeatures.portalAlongX(bloodRoom) ? "x" : "z");
        }
        for (int[] at : portal) host.instance().setBlock(at[0], at[1], at[2], block);
        strike();
    }

    private void strike() {
        double x = 0, y = Double.MAX_VALUE, z = 0;
        for (int[] at : portal) {
            x += at[0] + 0.5;
            z += at[2] + 0.5;
            y = Math.min(y, at[1]);
        }
        Pos at = new Pos(x / portal.size(), y + 0.5, z / portal.size());
        Entity bolt = new Entity(EntityType.LIGHTNING_BOLT);
        bolt.setInstance(host.instance(), at);
        bolt.scheduleRemove(Duration.ofSeconds(1));
        host.instance().playSound(THUNDER, at.x(), at.y(), at.z());
        host.instance().playSound(BANG, at.x(), at.y(), at.z());
    }

    boolean poll() {
        if (!portalOpen || entered) return false;
        for (SkyBlockPlayer player : host.alivePlayers()) {
            Pos feet = player.getPosition();
            for (int[] at : portal) {
                boolean inside = feet.blockX() == at[0] && feet.blockZ() == at[2]
                        && (feet.blockY() == at[1] || feet.blockY() + 1 == at[1]);
                if (inside) {
                    enter();
                    return true;
                }
            }
        }
        return false;
    }

    synchronized void enter() {
        if (entered) return;
        entered = true;
        host.advanceTo(DungeonRunPhase.BOSS);
        if (realArena.isPresent()) {
            enterBonzo();
            return;
        }
        box.build(host.instance());
        for (SkyBlockPlayer player : host.alivePlayers()) player.teleport(box.spawn());
        host.broadcast(CatacombsMessages.bossIntro(floor));
        host.later(ARRIVAL_DELAY_TICKS, () -> spawnBoss(1));
    }

    private void enterBonzo() {
        for (SkyBlockPlayer player : host.alivePlayers()) player.teleport(BonzoArenaSpots.ARRIVAL);
        bonzo = new BonzoFight(host, new BonzoFight.Ending() {
            @Override
            public void scoreScreen() {
                host.win();
            }

            @Override
            public void openGate(int gate) {
                if (gate == 1) ScoreScreen.requeue(floor, host.state().config().floor().mode()).forEach(host::broadcast);
                BossArena arena = realArena.orElseThrow();
                GateLift.run(host.instance(), gate == 1 ? BonzoArenaSpots.FIRST_GATE : BonzoArenaSpots.SECOND_GATE,
                        at -> arena.blockAt(at[0], at[1], at[2]), GateLift.Motion.BOSS_GATE);
            }
        });
        Pos spawn = BonzoArenaSpots.BONZO_SPAWN;
        host.instance().loadChunk(spawn).thenRun(bonzo::start);
    }

    /** Where the reward chests go: around Bonzo's spawn in the real arena, near the placeholder boss, or null before any boss. */
    @Nullable Pos rewardCentre() {
        if (bonzo != null) return BonzoArenaSpots.BONZO_SPAWN;
        return entered ? box.bossSpot() : null;
    }

    boolean realArena() {
        return bonzo != null;
    }

    /** After the run: anyone stepping into the exit portal at the end of the corridor goes back to the Dungeon Hub. */
    void pollExit(List<SkyBlockPlayer> members, Consumer<SkyBlockPlayer> toHub) {
        if (bonzo == null) return;
        for (SkyBlockPlayer player : members) {
            if (left.contains(player)) continue;
            Pos feet = player.getPosition();
            if (BonzoArenaSpots.inExitPortal(feet.blockX(), feet.blockY(), feet.blockZ())
                    || BonzoArenaSpots.inExitPortal(feet.blockX(), feet.blockY() + 1, feet.blockZ())) {
                left.add(player);
                toHub.accept(player);
            }
        }
    }

    void hideBar(SkyBlockPlayer player) {
        if (bonzo != null) bonzo.hideBar(player);
    }

    boolean killBossLife() {
        if (bonzo != null) return bonzo.killLife();
        return false;
    }

    void dispose() {
        if (bonzo != null) bonzo.dispose();
    }

    private synchronized void spawnBoss(int number) {
        phase = number;
        String lookId = number == 1 ? bossDefinition.id() : bossDefinition.id() + "_REVIVED";
        CatacombsMob boss = new CatacombsMob(bossDefinition, -1, false, this,
                MobLooks.shared().look(lookId).orElseGet(() -> CatacombsMob.lookOf(bossDefinition.id())));
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

    List<int[]> portalBlocks() {
        return List.copyOf(portal);
    }
}
