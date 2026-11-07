package dev.daweapon.type.catacombs.secret;

import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.type.catacombs.arena.RoomMechanisms;
import dev.daweapon.type.catacombs.engine.DungeonHost;
import dev.daweapon.type.catacombs.engine.GateLift;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * The secret levers of the run's real rooms. A pull clears the blocks the lever guards the way Hypixel's gates go (the
 * blocks turn to barriers and sink away as falling blocks, then become air); the lever itself never shows as powered and
 * every later pull is refused.
 */
public final class SecretLevers {
    private static final Sound PULL = Sound.sound(Key.key("block.anvil.break"), Sound.Source.BLOCK, 1f, 1.698f);

    public record PlacedLever(PlacedRoom room, RoomMechanisms.Lever lever, AtomicBoolean used) {
        boolean at(int x, int y, int z) {
            int[] at = lever.at();
            return at[0] == x && at[1] == y && at[2] == z;
        }
    }

    private final DungeonHost host;
    private final List<PlacedLever> levers;

    public SecretLevers(DungeonHost host, FloorLayout layout, RoomMechanisms mechanisms) {
        this.host = host;
        this.levers = placed(layout, host.arena().blocks()::isReal, mechanisms);
    }

    public static List<PlacedLever> placed(FloorLayout layout, Predicate<PlacedRoom> real, RoomMechanisms mechanisms) {
        List<PlacedLever> placed = new ArrayList<>();
        for (PlacedRoom room : layout.rooms()) {
            if (!real.test(room)) continue;
            for (RoomMechanisms.Lever lever : mechanisms.levers(room)) placed.add(new PlacedLever(room, lever, new AtomicBoolean()));
        }
        return List.copyOf(placed);
    }

    public void start() {
        for (PlacedLever lever : levers) {
            int[] at = lever.lever().at();
            host.instance().loadChunk(new Pos(at[0], at[1], at[2])).thenRun(() -> {
                Block current = host.instance().getBlock(at[0], at[1], at[2]);
                if (current.compare(Block.LEVER)) host.instance().setBlock(at[0], at[1], at[2], current.withProperty("powered", "false"));
                else if (current.air() && host.instance().getBlock(at[0], at[1] - 1, at[2]).solid()) {
                    host.instance().setBlock(at[0], at[1], at[2], Block.LEVER.withProperty("face", "floor"));
                }
            });
        }
    }

    /** A right-click on a lever; true when the block was one of the run's secret levers. */
    public boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        PlacedLever lever = levers.stream().filter(each -> each.at(x, y, z)).findFirst().orElse(null);
        if (lever == null) return false;
        Block shown = host.instance().getBlock(x, y, z);
        player.sendPacket(new BlockChangePacket(new Pos(x, y, z), shown));
        if (!lever.used().compareAndSet(false, true)) {
            player.sendMessage(SecretMessages.LEVER_USED);
            return true;
        }
        pull(lever);
        player.sendMessage(SecretMessages.LEVER_OPENS);
        Pos at = player.getPosition();
        player.playSound(PULL, at.x(), at.y(), at.z());
        return true;
    }

    private void pull(PlacedLever lever) {
        List<int[]> blocks = lever.lever().opens().stream()
                .filter(at -> !host.instance().getBlock(at[0], at[1], at[2]).air())
                .toList();
        if (blocks.isEmpty()) return;
        List<Block> looks = blocks.stream().map(at -> host.instance().getBlock(at[0], at[1], at[2])).toList();
        GateLift.run(host.instance(), blocks, at -> looks.get(blocks.indexOf(at)), GateLift.Motion.LEVER);
    }

    public List<PlacedLever> levers() {
        return levers;
    }
}
