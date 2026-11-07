package dev.daweapon.type.catacombs.breaker;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.type.catacombs.engine.DungeonHost;
import dev.daweapon.type.skyblockgeneric.dungeons.breaker.Dungeonbreaker;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import net.minestom.server.worldevent.WorldEvent;

import java.util.List;
import java.util.function.IntPredicate;

/**
 * Dungeonbreaker breaks inside one run: a permitted block costs a charge, vanishes, and comes back 10 seconds later
 * (or early, once its breaker has twenty out); every block still missing comes back when the run ends.
 */
public final class DungeonbreakerEngine {
    public interface Mechanisms {
        boolean protects(int x, int y, int z);
    }

    private final DungeonHost host;
    private final FloorLayout layout;
    private final CatacombsFloor floor;
    private final IntPredicate explored;
    private final Mechanisms mechanisms;
    private final BrokenBlocks<Block> broken = new BrokenBlocks<>();

    public DungeonbreakerEngine(DungeonHost host, FloorLayout layout, CatacombsFloor floor, IntPredicate explored, Mechanisms mechanisms) {
        this.host = host;
        this.layout = layout;
        this.floor = floor;
        this.explored = explored;
        this.mechanisms = mechanisms;
    }

    public boolean mayBreak(int x, int y, int z, Block block) {
        if (broken.isBroken(x, y, z)) return false;
        return BreakerRules.allows(block, area(x, z), mechanisms.protects(x, y, z), floor);
    }

    private BreakerRules.Area area(int x, int z) {
        BreakerRules.Area area = BreakerRules.area(layout, explored, x, z);
        if (area != BreakerRules.Area.OUTSIDE) return area;
        boolean inBoss = host.arena().bossArena().map(arena -> arena.covers(x, z)).orElse(false);
        return inBoss ? BreakerRules.Area.BOSS_ARENA : BreakerRules.Area.OUTSIDE;
    }

    /** A Dungeonbreaker dig: true when the block came out and a charge was spent. */
    public boolean dig(SkyBlockPlayer player, int x, int y, int z) {
        Instance instance = host.instance();
        Block block = instance.getBlock(x, y, z);
        if (!mayBreak(x, y, z, block)) return false;
        long now = System.currentTimeMillis();
        boolean staff = Dungeonbreaker.isStaff(player);
        if (!staff && !Dungeonbreaker.spendHeld(player, now)) return false;
        instance.setBlock(x, y, z, Block.AIR);
        instance.sendGroupedPacket(new WorldEventPacket(WorldEvent.PARTICLES_DESTROY_BLOCK.id(), new Pos(x, y, z), block.stateId(), false));
        restore(broken.record(player.getUuid(), x, y, z, block, now, BrokenBlocks.capFor(staff)));
        return true;
    }

    public void poll() {
        restore(broken.due(System.currentTimeMillis()));
    }

    public void dispose() {
        restore(broken.drain());
    }

    private void restore(List<BrokenBlocks.Broken<Block>> blocks) {
        Instance instance = host.instance();
        for (BrokenBlocks.Broken<Block> each : blocks) {
            if (instance.getBlock(each.x(), each.y(), each.z()).air()) instance.setBlock(each.x(), each.y(), each.z(), each.original());
        }
    }
}
