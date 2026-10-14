package net.swofty.type.game.instance;

import net.swofty.commons.ServerType;
import net.swofty.type.game.game.GameObject;

import java.util.List;

public interface InstanceHost {
    ServerType type();

    List<GameObject> instances();

    int remainingSlots();
}
