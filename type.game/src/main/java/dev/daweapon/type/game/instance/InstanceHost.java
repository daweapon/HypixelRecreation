package dev.daweapon.type.game.instance;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.game.game.GameObject;

import java.util.List;

public interface InstanceHost {
    ServerType type();

    List<GameObject> instances();

    int remainingSlots();
}
