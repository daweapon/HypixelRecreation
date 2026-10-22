package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

public record BeaconBeat(Color color, Speed speed, Pitch pitch) {
    public enum Color { RED, ORANGE, YELLOW, LIME, LIGHT_BLUE, PURPLE }

    public enum Speed {
        SLOW(10), MEDIUM(5), FAST(2);

        private final int halfPeriodTicks;

        Speed(int halfPeriodTicks) {
            this.halfPeriodTicks = halfPeriodTicks;
        }

        public int halfPeriodTicks() {
            return halfPeriodTicks;
        }

        public long halfPeriodMillis() {
            return halfPeriodTicks * 50L;
        }
    }

    public enum Pitch {
        LOW(0.6f), MID(1.0f), HIGH(1.6f);

        private final float value;

        Pitch(float value) {
            this.value = value;
        }

        public float value() {
            return value;
        }
    }

    public static BeaconBeat random(IntUnaryOperator nextInt) {
        return new BeaconBeat(
                Color.values()[nextInt.applyAsInt(Color.values().length)],
                Speed.values()[nextInt.applyAsInt(Speed.values().length)],
                Pitch.values()[nextInt.applyAsInt(Pitch.values().length)]);
    }

    public static List<BeaconBeat> sequence(int panels, IntUnaryOperator nextInt) {
        List<BeaconBeat> beats = new ArrayList<>(panels);
        for (int index = 0; index < panels; index++) beats.add(random(nextInt));
        return beats;
    }

    public boolean matches(Color pickedColor, Speed pickedSpeed, Pitch pickedPitch) {
        return color == pickedColor && speed == pickedSpeed && pitch == pickedPitch;
    }

    public boolean lit(long elapsedMillis) {
        return (elapsedMillis / speed.halfPeriodMillis()) % 2 == 0;
    }
}
