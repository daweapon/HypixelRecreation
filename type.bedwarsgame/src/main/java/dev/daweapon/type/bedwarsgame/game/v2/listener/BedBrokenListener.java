package dev.daweapon.type.bedwarsgame.game.v2.listener;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import dev.daweapon.commons.bedwars.map.BedWarsMapsConfig;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.bedwarsgame.events.custom.BedDestroyedEvent;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

// Announces a destroyed bed to the game and re-checks win conditions.
public class BedBrokenListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onBedDestroyed(BedDestroyedEvent event) {
        BedWarsGame game = (BedWarsGame) event.game();
        BedWarsMapsConfig.TeamKey teamKey = event.teamKey();
        if (game == null) return;

        BedWarsPlayer destroyer = event.destroyer();
        BedWarsMapsConfig.TeamKey destroyerTeamKey = destroyer.getTeamKey();
        if (destroyerTeamKey == null) {
            throw new IllegalStateException("Destroyer team key is null for player " + destroyer.getUsername());
        }

        game.broadcastMessage(Text.of("\n<f><l>BED DESTRUCTION > </l>{} <7>has been destroyed by {}!\n",
            Text.of("<color:{}>{} Bed", teamKey.chatColor(), teamKey.getName()),
            Text.of("<color:{}>{}", destroyerTeamKey.chatColor(), destroyer.getUsername())));

        for (BedWarsPlayer player : game.getPlayers()) {
            player.playSound(Sound.sound(Key.key("minecraft:entity.wither.death"),
                Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());
        }

        game.checkWinConditions();
    }

}
