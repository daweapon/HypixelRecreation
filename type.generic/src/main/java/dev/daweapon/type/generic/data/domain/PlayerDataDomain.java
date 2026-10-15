package dev.daweapon.type.generic.data.domain;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.data.DataHandler;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.UUID;

public interface PlayerDataDomain<H extends DataHandler> {
    DomainKey<H> key();

    boolean appliesTo(ServerType type);

    void load(UUID uuid);

    default void attach(HypixelPlayer player) {}

    void applyToPlayer(HypixelPlayer player);

    void save(HypixelPlayer player);

    default void cleanup(HypixelPlayer player) {}

    void unload(UUID uuid);

    default int order() {
        return 0;
    }
}
