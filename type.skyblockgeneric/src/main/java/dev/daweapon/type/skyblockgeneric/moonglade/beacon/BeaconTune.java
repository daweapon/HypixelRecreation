package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

public record BeaconTune(BeaconPane target, BeaconPane player) {
    public static BeaconTune random(IntUnaryOperator nextInt, long now) {
        int color = nextInt.applyAsInt(BeaconPane.COLORS);
        int speed = BeaconPane.MIN_LEVEL + nextInt.applyAsInt(BeaconPane.MAX_LEVEL);
        int pitch = BeaconPane.MIN_LEVEL + nextInt.applyAsInt(BeaconPane.MAX_LEVEL);
        BeaconPane target = BeaconPane.start(color, speed, pitch, now);
        BeaconPane player = BeaconPane.start((color + 1 + nextInt.applyAsInt(BeaconPane.COLORS - 1)) % BeaconPane.COLORS,
                speed % BeaconPane.MAX_LEVEL + 1, pitch % BeaconPane.MAX_LEVEL + 1, now).pausedAt(now);
        return new BeaconTune(target, player);
    }

    public static List<BeaconTune> sequence(int rounds, IntUnaryOperator nextInt, long now) {
        List<BeaconTune> tunes = new ArrayList<>(rounds);
        for (int index = 0; index < rounds; index++) tunes.add(random(nextInt, now));
        return tunes;
    }

    public boolean settingsMatch() {
        return target.sameSettings(player);
    }

    public boolean solved(long now) {
        return settingsMatch() && target.position(now) == player.position(now);
    }

    public BeaconTune withPlayer(BeaconPane pane) {
        return new BeaconTune(target, pane);
    }
}
