package net.swofty.type.skyblockgeneric.spray;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/** Which way an arrow spray points. The arrow pictures point right, so RIGHT needs no turning. */
@Getter
public enum SprayDirection {
    RIGHT("Right", 0, true),
    UP_RIGHT("Up Right", 45, false),
    UP("Up", 90, true),
    UP_LEFT("Up Left", 135, false),
    LEFT("Left", 180, true),
    DOWN_LEFT("Down Left", 225, false),
    DOWN("Down", 270, true),
    DOWN_RIGHT("Down Right", 315, false);

    private final String displayName;
    private final int degreesAnticlockwise;
    private final boolean cardinal;

    SprayDirection(String displayName, int degreesAnticlockwise, boolean cardinal) {
        this.displayName = displayName;
        this.degreesAnticlockwise = degreesAnticlockwise;
        this.cardinal = cardinal;
    }

    public static List<SprayDirection> offeredBy(Spray spray) {
        return Arrays.stream(values()).filter(direction -> spray.directions() == 8 || direction.cardinal).toList();
    }
}
