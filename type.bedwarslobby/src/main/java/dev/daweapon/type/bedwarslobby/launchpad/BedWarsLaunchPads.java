package dev.daweapon.type.bedwarslobby.launchpad;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.lobby.launchpad.LaunchPad;
import dev.daweapon.type.lobby.launchpad.LaunchPadHandler;

import java.util.List;
import java.util.function.Consumer;

@Getter
public enum BedWarsLaunchPads implements LaunchPad {
    SPAWN(LaunchPadHandler.getSlimeBlocksNear(new Pos(-30, 66, 0)),
            new Pos(0, 90, 0),
            (player) -> {},
            "");

    private final List<Pos> slimeBlocks;
    private final Pos destination;
    private final Consumer<HypixelPlayer> afterFinished;
    private final String rejectionMessage;

    BedWarsLaunchPads(List<Pos> slimeBlocks, Pos destination,
                      Consumer<HypixelPlayer> afterFinished, String rejectionMessage) {
        this.slimeBlocks = slimeBlocks;
        this.destination = destination;
        this.afterFinished = afterFinished;
        this.rejectionMessage = rejectionMessage;
    }
}
