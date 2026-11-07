package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.dungeons.catacombs.puzzle.RoomBlock;
import dev.daweapon.dungeons.catacombs.puzzle.higherorlower.HigherOrLower;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Higher Or Lower (M7 run 2 capture): ten blazes hang in the tall chamber, each with its own health. Killing them in
 * order (weakest first, as the chest starts at the bottom of the column) sends the chest up the iron-bar column a few
 * blocks per kill, one block every five ticks; once it reaches the top the puzzle is solved. A blaze killed out of
 * turn fails it.
 */
public final class HigherOrLowerRoom extends PuzzleRoom {
    private static final int CLIMB_TICKS = 5;
    private static final Block BARS = Block.IRON_BARS.withProperty("north", "true").withProperty("east", "true")
            .withProperty("south", "true").withProperty("west", "true");

    private final PuzzleCatalogue.HigherOrLowerRoom data;
    private final List<PuzzleBlaze> blazes = new ArrayList<>();
    private HigherOrLower puzzle;
    private PuzzleChest chest;
    private Block chestBlock = Block.CHEST;
    private int chestY;
    private int targetY;
    private boolean climbing;

    public HigherOrLowerRoom(PuzzleContext context, PlacedRoom room, PuzzleCatalogue.HigherOrLowerRoom data) {
        super(context, room, CatacombsPuzzle.HIGHER_OR_LOWER);
        this.data = data;
    }

    @Override
    public void start() {
        List<Integer> healths = HigherOrLower.dealHealths(data.blazes().size(), data.minHealth(), data.maxHealth(), ThreadLocalRandom.current());
        puzzle = new HigherOrLower(healths, data.order());
        int[] bottom = column(data.bottom());
        Block original = context.host().arena().blocks().blockAt(bottom[0], bottom[1], bottom[2]);
        if (original != null && original.compare(Block.CHEST)) chestBlock = original;
        chestY = data.order() == HigherOrLower.Order.LOWEST_FIRST ? data.bottom() : data.top();
        targetY = chestY;
        for (int y = data.bottom(); y <= data.top(); y++) setColumn(y, y == chestY ? chestBlock : BARS);
        for (int index = 0; index < data.blazes().size(); index++) {
            int blaze = index;
            PuzzleBlaze mob = new PuzzleBlaze(healths.get(index), data.level(), killer -> killed(blaze, killer));
            mob.setInstance(instance(), frame.spot(data.blazes().get(index)));
            blazes.add(mob);
        }
    }

    private int[] column(int y) {
        return frame.block(new RoomBlock(data.columnX(), y, data.columnZ()));
    }

    private void setColumn(int y, Block block) {
        int[] at = column(y);
        instance().setBlock(at[0], at[1], at[2], block);
    }

    private synchronized void killed(int blaze, SkyBlockPlayer killer) {
        switch (puzzle.kill(blaze)) {
            case IN_ORDER, LAST -> {
                select(String.valueOf(puzzle.killed()));
                Pos at = killer.getPosition();
                sound("block.note_block.pling", 1f, 2f, at.x(), at.y(), at.z());
                climbTo(travelled());
            }
            case OUT_OF_ORDER -> fail(killer, PuzzleMessages.failed(killer, "killed a Blaze in the wrong order!"));
            case IGNORED -> {
            }
        }
    }

    private int travelled() {
        int travel = data.top() - data.bottom();
        int offset = puzzle.chestOffset(travel);
        return data.order() == HigherOrLower.Order.LOWEST_FIRST ? data.bottom() + offset : data.top() - offset;
    }

    private void climbTo(int y) {
        targetY = y;
        if (climbing) return;
        climbing = true;
        later(CLIMB_TICKS, this::climb);
    }

    private synchronized void climb() {
        if (chestY == targetY) {
            climbing = false;
            if (puzzle.done() && chest == null) reachedEnd();
            return;
        }
        int next = chestY + Integer.signum(targetY - chestY);
        setColumn(chestY, BARS);
        setColumn(next, chestBlock);
        chestY = next;
        int[] at = column(chestY);
        sound("entity.horse.armor", 3f, 0.492f, at[0] + 0.5, at[1] + 0.5, at[2] + 0.5);
        later(CLIMB_TICKS, this::climb);
    }

    private void reachedEnd() {
        chest = chest(column(chestY));
        chest.unlock();
        completeRoom();
    }

    @Override
    public synchronized boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        int[] bottom = column(data.bottom());
        if (x != bottom[0] || z != bottom[2] || y < bottom[1] || y > column(data.top())[1]) return false;
        if (chest != null && chest.at(x, y, z)) chest.click(player);
        return true;
    }

    @Override
    public synchronized void solve(SkyBlockPlayer staff) {
        if (over()) return;
        while (!puzzle.done()) {
            int next = puzzle.next();
            puzzle.kill(next);
            blazes.get(next).remove();
            select(String.valueOf(puzzle.killed()));
        }
        climbTo(travelled());
    }

    @Override
    public List<String> info() {
        List<String> lines = new ArrayList<>();
        lines.add("<7>Order: <e>" + data.order() + "<7>, killed <e>" + puzzle.killed() + "<7>/<e>" + blazes.size());
        StringBuilder order = new StringBuilder("<7>Next: ");
        for (int index = puzzle.killed(); index < puzzle.sequence().size(); index++) {
            order.append("<a>").append(puzzle.healths().get(puzzle.sequence().get(index))).append(' ');
        }
        lines.add(order.toString().trim());
        return lines;
    }

    @Override
    public void dispose() {
        blazes.forEach(PuzzleBlaze::remove);
        blazes.clear();
    }
}
