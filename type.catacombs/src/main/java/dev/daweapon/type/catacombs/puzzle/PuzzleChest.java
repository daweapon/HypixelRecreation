package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import dev.daweapon.type.catacombs.secret.SecretMessages;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockActionPacket;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * A puzzle's reward chest. Once the puzzle lets it open, the first player to open it hears the five-note jingle from
 * every captured puzzle chest and finds a level V blessing for the party; the room is marked complete at that moment.
 */
public final class PuzzleChest {
    public static final int BLESSING_LEVEL = 5;
    static final List<BlessingType> BLESSINGS = List.of(BlessingType.LIFE, BlessingType.POWER, BlessingType.STONE, BlessingType.WISDOM);
    private static final float[] JINGLE = {0.794f, 0.889f, 1.0f, 1.095f, 1.19f};
    private static final int NOTE_TICKS = 5;
    private static final Sound OPEN = Sound.sound(Key.key("block.chest.open"), Sound.Source.BLOCK, 0.5f, 0.92f);

    private final PuzzleContext context;
    private final int[] at;
    private final Consumer<SkyBlockPlayer> opened;
    private volatile boolean unlocked;
    private volatile boolean searched;

    public PuzzleChest(PuzzleContext context, int[] at, Consumer<SkyBlockPlayer> opened) {
        this.context = context;
        this.at = at;
        this.opened = opened;
    }

    public boolean at(int x, int y, int z) {
        return at[0] == x && at[1] == y && at[2] == z;
    }

    public int[] position() {
        return at;
    }

    public void unlock() {
        unlocked = true;
    }

    public boolean unlocked() {
        return unlocked;
    }

    public boolean searched() {
        return searched;
    }

    /** A right-click on the chest; always handled so a locked chest never opens its empty inventory. */
    public synchronized void click(SkyBlockPlayer player) {
        if (!unlocked) return;
        if (searched) {
            player.sendMessage(SecretMessages.CHEST_SEARCHED);
            return;
        }
        searched = true;
        Instance instance = context.host().instance();
        Pos centre = new Pos(at[0] + 0.5, at[1] + 0.5, at[2] + 0.5);
        Block chest = instance.getBlock(at[0], at[1], at[2]);
        instance.sendGroupedPacket(new BlockActionPacket(new Pos(at[0], at[1], at[2]), (byte) 1, (byte) 1,
                chest.compare(Block.TRAPPED_CHEST) ? Block.TRAPPED_CHEST : Block.CHEST));
        instance.playSound(OPEN, centre.x(), centre.y(), centre.z());
        for (int note = 0; note < JINGLE.length; note++) {
            float pitch = JINGLE[note];
            boolean last = note == JINGLE.length - 1;
            context.host().later(NOTE_TICKS * (note + 1), () -> {
                instance.playSound(Sound.sound(Key.key("block.note_block.harp"), Sound.Source.BLOCK, 1f, pitch), centre.x(), centre.y(), centre.z());
                if (last) reward(player);
            });
        }
    }

    private void reward(SkyBlockPlayer player) {
        BlessingType type = BLESSINGS.get(ThreadLocalRandom.current().nextInt(BLESSINGS.size()));
        context.blessings().grant(type, BLESSING_LEVEL, player.isOnline() ? player : null);
        opened.accept(player);
    }
}
