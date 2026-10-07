package net.swofty.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Hypixel's short-range teleport (Aspect of the End, Wither Impact): travel along the look
 * direction and stop at the last spot the player's body fits, so walls and bars end the jump
 * instead of being passed through.
 */
public final class ForwardTeleport {
    private static final double STEP = 0.25;

    private ForwardTeleport() {
    }

    public static Pos destination(SkyBlockPlayer player, double maxDistance) {
        Instance instance = player.getInstance();
        Pos start = player.getPosition();
        Vec direction = start.direction();
        Pos last = start;
        for (double travelled = STEP; travelled <= maxDistance; travelled += STEP) {
            Pos candidate = start.add(direction.mul(travelled));
            if (!fits(instance, candidate)) break;
            last = candidate;
        }
        return last.withView(start.yaw(), start.pitch());
    }

    public static Pos teleport(SkyBlockPlayer player, double maxDistance) {
        Pos destination = destination(player, maxDistance);
        player.teleport(destination);
        return destination;
    }

    private static boolean fits(Instance instance, Pos feet) {
        return passable(instance.getBlock(feet)) && passable(instance.getBlock(feet.add(0, 1, 0)));
    }

    private static boolean passable(Block block) {
        return !block.solid() && !block.compare(Block.IRON_BARS);
    }
}
