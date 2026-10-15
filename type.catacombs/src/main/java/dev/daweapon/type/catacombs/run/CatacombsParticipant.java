package dev.daweapon.type.catacombs.run;

import dev.daweapon.type.game.game.GameParticipant;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.Player;

import java.util.UUID;

public final class CatacombsParticipant implements GameParticipant {
    private final SkyBlockPlayer player;
    private String gameId;

    public CatacombsParticipant(SkyBlockPlayer player) {
        this.player = player;
    }

    public SkyBlockPlayer player() {
        return player;
    }

    @Override
    public UUID getUuid() {
        return player.getUuid();
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public Player getServerPlayer() {
        return player;
    }
}
