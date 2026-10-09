package net.swofty.type.kuudra.run;

import net.minestom.server.entity.Player;
import net.swofty.type.game.game.GameParticipant;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

public final class KuudraParticipant implements GameParticipant {
    private final SkyBlockPlayer player;
    private String gameId;

    public KuudraParticipant(SkyBlockPlayer player) {
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
