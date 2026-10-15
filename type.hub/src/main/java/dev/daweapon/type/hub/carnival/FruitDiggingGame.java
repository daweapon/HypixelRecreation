package dev.daweapon.type.hub.carnival;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult.Metric;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard.Dig;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard.DowsingMode;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard.Reveal;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitTile;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Fruit Digging on the sand board beside the Carnival Pirateman. */
public class FruitDiggingGame extends CarnivalGameBase {
    public static final int SECONDS = 5 * 60;
    private static final BlockVec BOARD_CORNER = new BlockVec(-112, 70, 18);
    private static final double REACH = 6;
    private static final long DIG_COOLDOWN_TICKS = 4;
    private static final Map<FruitTile, Block> TILE_BLOCKS = new EnumMap<>(Map.of(
            FruitTile.DRAGONFRUIT, Block.MAGENTA_WOOL, FruitTile.MANGO, Block.ORANGE_WOOL,
            FruitTile.COCONUT, Block.BROWN_WOOL, FruitTile.APPLE, Block.RED_WOOL,
            FruitTile.CHERRY, Block.PINK_WOOL, FruitTile.POMEGRANATE, Block.PURPLE_WOOL,
            FruitTile.DURIAN, Block.YELLOW_WOOL, FruitTile.WATERMELON, Block.MELON,
            FruitTile.BOMB, Block.TNT, FruitTile.RUM, Block.BARREL));

    private final FruitDiggingBoard board = new FruitDiggingBoard(ThreadLocalRandom.current());
    private DowsingMode mode = DowsingMode.MINES;
    private long lastDig = -DIG_COOLDOWN_TICKS;

    public FruitDiggingGame(SkyBlockPlayer player) {
        super(player, CarnivalGame.FRUIT_DIGGING, SECONDS);
    }

    @Override
    protected void onStart() {
        for (int x = 0; x < FruitDiggingBoard.SIZE; x++) {
            for (int z = 0; z < FruitDiggingBoard.SIZE; z++) {
                showBlock(BOARD_CORNER.add(x, 0, z), Block.SAND);
                showBlock(BOARD_CORNER.add(x, 1, z), Block.AIR);
            }
        }
        player.sendMessage("<6>Fruit Digging <7>has started! Dig the sand with your <a>shovel<7>. "
                + "<e>Right-click <7>to change your dowsing mode. <7>Dowsing Mode: " + mode.getDisplayName());
    }

    @Override
    public void onRightClick() {
        mode = mode.next();
        player.sendMessage("<e>Dowsing Mode: " + mode.getDisplayName());
    }

    @Override
    public void onLeftClick() {
        if (tick - lastDig < DIG_COOLDOWN_TICKS) return;
        int[] cell = targetedCell();
        if (cell == null || board.stateAt(cell[0], cell[1]) != FruitDiggingBoard.CellState.HIDDEN) return;
        Dig dig = board.dig(cell[0], cell[1], mode);
        if (dig == null) return;
        lastDig = tick;
        show(dig);
    }

    private int[] targetedCell() {
        Vec eye = eye();
        Vec look = look();
        for (double distance = 0; distance <= REACH; distance += 0.05) {
            Vec point = eye.add(look.mul(distance));
            int x = point.blockX() - BOARD_CORNER.blockX();
            int z = point.blockZ() - BOARD_CORNER.blockZ();
            if (point.blockY() == BOARD_CORNER.blockY() && x >= 0 && z >= 0
                    && x < FruitDiggingBoard.SIZE && z < FruitDiggingBoard.SIZE) {
                return new int[]{x, z};
            }
        }
        return null;
    }

    private void show(Dig dig) {
        for (Reveal reveal : dig.reveals()) {
            showBlock(BOARD_CORNER.add(reveal.x(), 0, reveal.z()), TILE_BLOCKS.get(reveal.tile()));
            player.sendMessage(describe(reveal, dig));
        }
        for (int[] cell : dig.destroyed()) showBlock(BOARD_CORNER.add(cell[0], 0, cell[1]), Block.COARSE_DIRT);
        player.playSound(Sound.sound(Key.key(dig.reveals().getFirst().tile() == FruitTile.BOMB && !dig.bombDefused()
                ? "entity.generic.explode" : "block.sand.break"), Sound.Source.PLAYER, 1f, 1f));
        if (dig.dowsing() != null) player.sendMessage(dig.dowsing());
        player.sendMessage("<7>Digs left: <a>" + board.getDigsLeft() + "<7>/" + FruitDiggingBoard.DIGS);
    }

    private static String describe(Reveal reveal, Dig dig) {
        return switch (reveal.tile()) {
            case BOMB -> dig.bombDefused()
                    ? "<6><l>PHEW! <7>Your <6>Coconut <7>stopped the <c>Bomb <7>from exploding!"
                    : "<c><l>BOOM! <7>A <c>Bomb <7>destroyed <c>" + dig.destroyed().size() + " <7>nearby fruit"
                    + (dig.destroyed().size() == 1 ? "" : "s") + "!";
            case RUM -> "<e><l>RUM! <7>Your shovel won't dowse on your next dig.";
            default -> (reveal.fromWatermelon() ? "<a><l>WATERMELON! <7>It revealed a " : "<7>You dug up a ")
                    + reveal.tile().colored() + "<7>! <a>+" + StringUtility.commaify(Math.round(reveal.points())) + " points";
        };
    }

    @Override
    protected void onTick() {}

    @Override
    protected boolean isDone() {
        return board.isFinished();
    }

    @Override
    protected long score() {
        return Math.round(board.getScore());
    }

    @Override
    protected void onEnd() {}

    @Override
    protected CarnivalGameResult result() {
        return board.result();
    }

    @Override
    protected List<String> summary(CarnivalGameResult result) {
        return List.of(
                "<6><l>Fruit Digging",
                "",
                "<7>Your Score",
                "<a>" + StringUtility.commaify(result.score()),
                "",
                "<7>Digs Used: <e>" + (FruitDiggingBoard.DIGS - board.getDigsLeft())
                        + " <8>(" + result.metric(Metric.UNIQUE_FRUITS) + " unique)");
    }
}
