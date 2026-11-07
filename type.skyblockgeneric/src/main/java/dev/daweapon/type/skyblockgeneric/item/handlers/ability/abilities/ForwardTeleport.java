package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.RelativeFlags;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Hypixel's short-range teleport (Aspect of the End, Wither Impact): travel along the look
 * direction and stop at the last block the player's body fits in, so walls and bars end the jump
 * instead of being passed through. Like Hypixel, the player always lands centred in a block
 * (X and Z on .5, a whole-number Y), keeps their momentum, and keeps looking wherever the client
 * is looking rather than being snapped back to the angle they clicked at.
 */
public final class ForwardTeleport {
    private static final double STEP = 0.1;

    @FunctionalInterface
    public interface Passable {
        boolean at(int x, int y, int z);
    }

    private ForwardTeleport() {
    }

    public static Pos destination(Pos start, double maxDistance, Passable passable) {
        Vec direction = start.direction();
        int feetY = (int) Math.floor(start.y());
        if (!passable.at(start.blockX(), feetY, start.blockZ()) && start.y() > feetY) feetY++;
        double yOffset = feetY - start.y();
        Pos last = null;
        for (double travelled = STEP; travelled <= maxDistance + 1e-9; travelled += STEP) {
            Pos point = start.add(direction.mul(travelled));
            int x = (int) Math.floor(point.x());
            int y = (int) Math.floor(point.y() + yOffset);
            int z = (int) Math.floor(point.z());
            if (!passable.at(x, y, z) || !passable.at(x, y + 1, z)) break;
            if (x == start.blockX() && y == feetY && z == start.blockZ()) continue;
            last = new Pos(x + 0.5, y, z + 0.5);
        }
        return last == null ? null : last.withView(start.yaw(), start.pitch());
    }

    public static Pos destination(SkyBlockPlayer player, double maxDistance) {
        Instance instance = player.getInstance();
        return destination(player.getPosition(), maxDistance, (x, y, z) -> passable(instance.getBlock(x, y, z)));
    }

    public static Pos teleport(SkyBlockPlayer player, double maxDistance) {
        Pos destination = destination(player, maxDistance);
        if (destination == null) {
            player.sendMessage("<c>There are blocks in the way!");
            return player.getPosition();
        }
        player.teleport(destination.withView(0, 0), null, RelativeFlags.VIEW);
        return destination;
    }

    private static boolean passable(Block block) {
        return !block.solid() && !block.compare(Block.IRON_BARS);
    }
}
