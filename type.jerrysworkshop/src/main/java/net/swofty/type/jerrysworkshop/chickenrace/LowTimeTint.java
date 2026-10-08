package net.swofty.type.jerrysworkshop.chickenrace;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.network.packet.server.play.WorldBorderCenterPacket;
import net.minestom.server.network.packet.server.play.WorldBorderWarningReachPacket;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Turns the racer's screen pink, then red, as the countdown runs low, using the vanilla world border
 * warning vignette: a private border centred on the player, far away, whose warning distance grows
 * until the player is "inside" the warning zone. The client tints by 1 - distance / warning distance.
 * The vignette only draws on Fancy graphics or better.
 */
final class LowTimeTint {
    private static final double DISTANCE = 1000;
    private static final double STRONGEST = 0.95;

    private final SkyBlockPlayer player;
    private final double startSeconds;
    private final double fullSeconds;
    private int warning = -1;

    LowTimeTint(SkyBlockPlayer player, double startSeconds, double fullSeconds) {
        this.player = player;
        this.startSeconds = startSeconds;
        this.fullSeconds = fullSeconds;
    }

    static double strength(double remainingSeconds, double startSeconds, double fullSeconds) {
        if (remainingSeconds >= startSeconds) return 0;
        return Math.min(STRONGEST, (startSeconds - remainingSeconds) / (startSeconds - fullSeconds));
    }

    void update(double remainingSeconds) {
        double strength = strength(remainingSeconds, startSeconds, fullSeconds);
        int target = strength <= 0 ? 0 : (int) Math.ceil(DISTANCE / (1 - strength));
        Pos position = player.getPosition();
        if (warning < 0) {
            player.sendPacket(new WorldBorder(DISTANCE * 2, position.x(), position.z(), target, 0)
                    .createInitializePacket(DISTANCE * 2, 0));
            warning = target;
            return;
        }
        if (target > 0) player.sendPacket(new WorldBorderCenterPacket(position.x(), position.z()));
        if (target != warning) {
            player.sendPacket(new WorldBorderWarningReachPacket(target));
            warning = target;
        }
    }

    void clear() {
        if (warning < 0) return;
        warning = -1;
        Instance instance = player.getInstance();
        if (instance != null) player.sendPacket(instance.createInitializeWorldBorderPacket());
    }
}
