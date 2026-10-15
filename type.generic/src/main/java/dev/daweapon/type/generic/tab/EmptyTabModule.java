package dev.daweapon.type.generic.tab;

import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.List;

public class EmptyTabModule extends TablistModule {

    public EmptyTabModule() {}

    public List<TablistEntry> getEntries(HypixelPlayer player) {
        return List.of();
    }
}
