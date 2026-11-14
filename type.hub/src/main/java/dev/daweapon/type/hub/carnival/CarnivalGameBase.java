package dev.daweapon.type.hub.carnival;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameSession;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGoal;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalSessions;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The shared life of a Carnival game: tickets in, tool in the hotbar and Leave Game in the menu slot, a per-tick loop
 * against a time limit with the game's block on the sidebar, then the centred summary, tokens and, two seconds later,
 * any completed goals. Everything a game shows the player (blocks, mobs, fish) is sent to that player alone.
 */
public abstract class CarnivalGameBase implements CarnivalGameSession {
    private static final int END_GRACE_TICKS = 20;
    private static final int GOALS_DELAY_TICKS = 40;
    private static final int GAIN_SHOWN_TICKS = 20;

    protected final SkyBlockPlayer player;
    private final CarnivalGame game;
    private final int durationTicks;
    private final List<Point> fakeBlocks = new ArrayList<>();
    protected long tick;
    private Task task;
    private boolean over;
    private long lastGain;
    private long lastGainAt = -GAIN_SHOWN_TICKS;

    protected CarnivalGameBase(SkyBlockPlayer player, CarnivalGame game, int durationSeconds) {
        this.player = player;
        this.game = game;
        this.durationTicks = durationSeconds * 20;
    }

    @Override
    public CarnivalGame game() {
        return game;
    }

    public void start(int toolSlot) {
        Carnivals.payForGame(player);
        player.getInventory().setItemStack(toolSlot, new SkyBlockItem(game.getTool()).getItemStack());
        player.setHeldItemSlot((byte) toolSlot);
        Pos start = startPosition();
        if (start != null) player.teleport(start);
        CarnivalSessions.begin(player, this);
        play("entity.experience_orb.pickup", 1f, 1.4920635f);
        onStart();
        task = MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (over) return TaskSchedule.stop();
            if (!player.isOnline() || player.getInstance() == null) {
                CarnivalSessions.abandon(player);
                return TaskSchedule.stop();
            }
            tick++;
            onTick();
            if (tick >= durationTicks + END_GRACE_TICKS || isDone()) {
                finish();
                return TaskSchedule.stop();
            }
            return TaskSchedule.tick(1);
        });
    }

    protected boolean timeUp() {
        return tick >= durationTicks;
    }

    protected int secondsLeft() {
        return (int) Math.max(0, (durationTicks - tick + 19) / 20);
    }

    protected void gain(long points) {
        lastGain = points;
        lastGainAt = tick;
    }

    @Override
    public List<String> sidebarLines() {
        List<String> lines = new ArrayList<>();
        lines.add(game.getColor() + "<l>" + game.getDisplayName());
        lines.add(CarnivalChat.timeLeft(secondsLeft()));
        lines.addAll(sidebarBody());
        boolean showGain = tick - lastGainAt < GAIN_SHOWN_TICKS && lastGain > 0;
        lines.add("<f>Score: <e>" + StringUtility.commaify(score()) + (showGain ? " <6>" + gainLabel(lastGain) : ""));
        return lines;
    }

    protected String gainLabel(long gain) {
        return "(+" + StringUtility.commaify(gain) + ")";
    }

    private void finish() {
        if (over) return;
        over = true;
        cleanUp();
        CarnivalGameResult result = result();
        Carnivals.Finish finish = Carnivals.finishGame(player, result);
        Pos end = endPosition();
        if (end != null) player.teleport(end);
        CarnivalChat.summary(game, result.score(), summary(result), finish.tokens()).forEach(player::sendMessage);
        chime();
        CarnivalSessions.end(player, this);
        if (!finish.goals().isEmpty()) announceGoals(finish.goals());
    }

    private void chime() {
        play("block.note_block.harp", 1f, 0.6666667f);
        play("block.note_block.harp", 1f, 0.8888889f);
        player.scheduler().buildTask(() -> {
            for (float pitch : new float[]{0.5873016f, 0.74603176f, 0.8888889f, 1.1746032f}) {
                play("block.note_block.harp", 1f, pitch);
            }
        }).delay(TaskSchedule.tick(2)).schedule();
    }

    private void announceGoals(List<CarnivalGoal> goals) {
        player.scheduler().buildTask(() -> {
            if (!player.isOnline()) return;
            Carnivals.goalsCompleteLines(goals).forEach(player::sendMessage);
            play("entity.player.levelup", 1f, 1f);
        }).delay(TaskSchedule.tick(GOALS_DELAY_TICKS)).schedule();
        player.scheduler().buildTask(() -> {
            if (player.isOnline()) play("entity.experience_orb.pickup", 1f, 1.4920635f);
        }).delay(TaskSchedule.tick(GOALS_DELAY_TICKS + 10)).schedule();
    }

    @Override
    public void abandon() {
        if (over) return;
        over = true;
        if (task != null) task.cancel();
        cleanUp();
        CarnivalSessions.end(player, this);
    }

    @Override
    public void leave() {
        if (over) return;
        abandon();
        Carnivals.refundGame(player);
        Pos end = endPosition();
        if (end != null) player.teleport(end);
        player.sendMessage("<c>You left the " + game.getDisplayName() + " minigame!");
    }

    private void cleanUp() {
        onEnd();
        if (player.getInstance() != null) {
            for (Point point : fakeBlocks) {
                player.sendPacket(new BlockChangePacket(point, player.getInstance().getBlock(point)));
            }
        }
        fakeBlocks.clear();
    }

    protected void showBlock(Point point, Block block) {
        if (!fakeBlocks.contains(point)) fakeBlocks.add(point);
        player.sendPacket(new BlockChangePacket(point, block));
    }

    protected void play(String sound, float volume, float pitch) {
        player.playSound(Sound.sound(Key.key(sound), Sound.Source.PLAYER, volume, pitch));
    }

    protected Vec eye() {
        return player.getPosition().add(0, player.getEyeHeight(), 0).asVec();
    }

    protected Vec look() {
        return player.getPosition().direction();
    }

    protected @Nullable Pos startPosition() {
        return null;
    }

    protected @Nullable Pos endPosition() {
        return null;
    }

    protected abstract long score();

    protected abstract List<String> sidebarBody();

    protected abstract void onStart();

    protected abstract void onTick();

    protected boolean isDone() {
        return false;
    }

    protected abstract void onEnd();

    protected abstract CarnivalGameResult result();

    /** The lines between the score and the token line, already centred with {@link CarnivalChat}. */
    protected abstract List<String> summary(CarnivalGameResult result);
}
