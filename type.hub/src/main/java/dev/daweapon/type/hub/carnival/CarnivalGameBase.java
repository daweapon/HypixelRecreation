package dev.daweapon.type.hub.carnival;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameSession;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalSessions;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * The shared life of a Carnival game: tickets in, tool in the hotbar, a per-tick loop against a
 * time limit, then tokens, goals and a results message. Everything a game shows the player
 * (blocks, mobs, fish) is sent to that player alone.
 */
public abstract class CarnivalGameBase implements CarnivalGameSession {
    protected static final String RULE = "<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

    protected final SkyBlockPlayer player;
    private final CarnivalGame game;
    private final int durationTicks;
    private final List<Point> fakeBlocks = new ArrayList<>();
    protected long tick;
    private Task task;
    private boolean over;

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
        player.addEffect(new Potion(PotionEffect.MINING_FATIGUE, (byte) 4, durationTicks + 40));
        CarnivalSessions.begin(player, this);
        onStart();
        task = MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (over) return TaskSchedule.stop();
            if (!player.isOnline() || player.getInstance() == null) {
                CarnivalSessions.abandon(player);
                return TaskSchedule.stop();
            }
            tick++;
            onTick();
            if (tick % 10 == 0) showStatus();
            if (tick >= durationTicks || isDone()) {
                finish();
                return TaskSchedule.stop();
            }
            return TaskSchedule.tick(1);
        });
    }

    protected int secondsLeft() {
        return (int) Math.max(0, (durationTicks - tick + 19) / 20);
    }

    private void showStatus() {
        SkyBlockActionBar bar = SkyBlockActionBar.getFor(player);
        bar.addReplacement(SkyBlockActionBar.BarSection.HEALTH, Text.of("<e>Time Left: <f>{}s", secondsLeft()), 15, 100);
        bar.addReplacement(SkyBlockActionBar.BarSection.DEFENSE, Text.empty(), 15, 100);
        bar.addReplacement(SkyBlockActionBar.BarSection.MANA, Text.of("<e>Score: <a>{}", StringUtility.commaify(score())), 15, 100);
    }

    private void finish() {
        if (over) return;
        over = true;
        cleanUp();
        CarnivalGameResult result = result();
        int tokens = Carnivals.finishGame(player, result);
        player.removeEffect(PotionEffect.MINING_FATIGUE);
        List<String> lines = new ArrayList<>();
        lines.add(RULE);
        lines.addAll(summary(result));
        lines.add("");
        lines.add("<7>You earned <e>" + StringUtility.commaify(tokens) + " Carnival Tokens<7>!");
        lines.add(RULE);
        lines.forEach(player::sendMessage);
        CarnivalSessions.end(player, this);
    }

    @Override
    public void abandon() {
        if (over) return;
        over = true;
        if (task != null) task.cancel();
        cleanUp();
        player.removeEffect(PotionEffect.MINING_FATIGUE);
        CarnivalSessions.end(player, this);
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

    protected Vec eye() {
        return player.getPosition().add(0, player.getEyeHeight(), 0).asVec();
    }

    protected Vec look() {
        return player.getPosition().direction();
    }

    protected abstract long score();

    protected abstract void onStart();

    protected abstract void onTick();

    protected boolean isDone() {
        return false;
    }

    protected abstract void onEnd();

    protected abstract CarnivalGameResult result();

    protected abstract List<String> summary(CarnivalGameResult result);
}
