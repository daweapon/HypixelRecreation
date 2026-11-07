package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzleFactory;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.dungeons.catacombs.puzzle.RoomBlock;
import dev.daweapon.dungeons.catacombs.puzzle.creeperbeams.CreeperBeams;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.monster.CreeperMeta;
import net.minestom.server.entity.metadata.monster.GuardianMeta;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creeper Beams (F1 run 2 capture): shooting a sea lantern and then a second one joins them with a guardian beam and
 * turns both to prismarine; shooting a joined lantern breaks the beam. Arrows fly through the creeper and the beam
 * guardians, and one beam that misses the creeper is already joined when the room loads. Once four beams cross the
 * creeper it primes, explodes after about a second and a half, and its pedestal is blown away top to bottom to reveal
 * the blessing chest.
 */
public final class CreeperBeamsRoom extends PuzzleRoom {
    private static final int FUSE_TICKS = 29;
    private static final int[][] PEDESTAL_STAGES = {{6, 99, 0}, {4, 5, 4}, {2, 3, 10}, {1, 1, 14}};

    private record Beam(Entity guardian, Entity target) {
        void remove() {
            guardian.remove();
            target.remove();
        }
    }

    private final PuzzleCatalogue.CreeperBeamsRoom data;
    private final CreeperBeams puzzle;
    private final Map<Long, RoomBlock> lanterns = new HashMap<>();
    private final Map<CreeperBeams.Beam, Beam> beams = new HashMap<>();
    private Entity creeper;
    private PuzzleChest chest;
    private boolean exploding;

    public CreeperBeamsRoom(PuzzleContext context, PlacedRoom room, PuzzleCatalogue.CreeperBeamsRoom data) {
        super(context, room, CatacombsPuzzle.CREEPER_BEAMS);
        this.data = data;
        this.puzzle = new CreeperBeams(data.lanterns(), data.space(), data.required());
    }

    @Override
    public void start() {
        for (RoomBlock lantern : data.lanterns()) {
            int[] at = frame.block(lantern);
            lanterns.put(RoomFrame.key(at), lantern);
            instance().setBlock(at[0], at[1], at[2], Block.SEA_LANTERN);
        }
        creeper = prop(EntityType.CREEPER);
        creeper.setNoGravity(true);
        creeper.setInstance(instance(), frame.spot(data.creeper()));
        chest = chest(frame.block(data.chest()));
        if (data.startBeam().size() == 2) join(puzzle.connect(data.startBeam().get(0), data.startBeam().get(1)));
    }

    static Entity prop(EntityType type) {
        return new Entity(type) {
            {
                collidesWithEntities = false;
            }
        };
    }

    @Override
    public synchronized boolean arrowHit(SkyBlockPlayer player, int x, int y, int z) {
        RoomBlock lantern = lanterns.get(RoomFrame.key(x, y, z));
        if (lantern == null) return false;
        if (exploding || over()) return true;
        switch (puzzle.shoot(lantern)) {
            case CreeperBeams.Shot.Joined joined -> join(joined.beam());
            case CreeperBeams.Shot.Broken broken -> breakBeam(broken.beam());
            default -> {
            }
        }
        if (puzzle.solved()) prime();
        return true;
    }

    private void join(CreeperBeams.Beam beam) {
        setLantern(beam.first(), Block.PRISMARINE);
        setLantern(beam.second(), Block.PRISMARINE);
        Entity target = new Entity(EntityType.ARMOR_STAND);
        target.editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setInvisible(true);
            meta.setMarker(true);
        });
        target.setNoGravity(true);
        Entity guardian = prop(EntityType.GUARDIAN);
        guardian.setInvisible(true);
        guardian.setNoGravity(true);
        guardian.setSilent(true);
        target.setInstance(instance(), frame.centre(beam.second()).add(0, 0.5, 0)).thenRun(() ->
                guardian.setInstance(instance(), frame.centre(beam.first())).thenRun(() ->
                        guardian.editEntityMeta(GuardianMeta.class, meta -> meta.setTarget(target))));
        beams.put(beam, new Beam(guardian, target));
    }

    private void breakBeam(CreeperBeams.Beam beam) {
        setLantern(beam.first(), Block.SEA_LANTERN);
        setLantern(beam.second(), Block.SEA_LANTERN);
        Beam shown = beams.remove(beam);
        if (shown != null) shown.remove();
    }

    private void setLantern(RoomBlock lantern, Block block) {
        int[] at = frame.block(lantern);
        instance().setBlock(at[0], at[1], at[2], block);
    }

    private void prime() {
        if (exploding) return;
        exploding = true;
        advance(CatacombsPuzzleFactory.BEAMS_ALIGNED);
        completeRoom();
        beams.values().forEach(Beam::remove);
        beams.clear();
        creeper.editEntityMeta(CreeperMeta.class, meta -> meta.setState(CreeperMeta.State.FUSE));
        Pos at = creeper.getPosition();
        sound("entity.creeper.primed", 1f, 0.492f, at.x(), at.y(), at.z());
        later(FUSE_TICKS, this::explode);
    }

    private void explode() {
        Pos at = creeper.getPosition();
        sound("entity.generic.explode", 4f, 0.714f, at.x(), at.y() + 1, at.z());
        instance().sendGroupedPacket(new ParticlePacket(Particle.EXPLOSION_EMITTER, at.x(), at.y() + 1, at.z(), 0, 0, 0, 0, 1));
        creeper.remove();
        for (int[] stage : PEDESTAL_STAGES) {
            int lowest = stage[0];
            int highest = stage[1];
            later(stage[2] + 1, () -> blowPedestal(lowest, highest));
        }
        later(PEDESTAL_STAGES[PEDESTAL_STAGES.length - 1][2] + 2, () -> chest.unlock());
    }

    private void blowPedestal(int lowest, int highest) {
        Pos centre = frame.centre(new RoomBlock(data.chest().x(), lowest, data.chest().z()));
        sound("entity.generic.explode", 1f, 1f, centre.x(), centre.y(), centre.z());
        for (RoomBlock block : data.pedestal()) {
            if (block.y() < lowest || block.y() > highest) continue;
            int[] at = frame.block(block);
            instance().setBlock(at[0], at[1], at[2], Block.AIR);
        }
    }

    @Override
    public synchronized boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        if (!chest.at(x, y, z)) return false;
        chest.click(player);
        return true;
    }

    @Override
    public synchronized void solve(SkyBlockPlayer staff) {
        prime();
    }

    @Override
    public List<String> info() {
        List<String> lines = new ArrayList<>();
        lines.add("<7>Beams through the creeper: <e>" + puzzle.beamsThroughCreeper() + "<7>/<e>" + puzzle.required());
        puzzle.beams().forEach(beam -> lines.add("<7>" + beam.first() + " - " + beam.second()
                + (puzzle.crossesCreeper(beam) ? " <a>crosses" : " <c>misses")));
        return lines;
    }

    @Override
    public void dispose() {
        beams.values().forEach(Beam::remove);
        beams.clear();
        if (creeper != null) creeper.remove();
    }
}
