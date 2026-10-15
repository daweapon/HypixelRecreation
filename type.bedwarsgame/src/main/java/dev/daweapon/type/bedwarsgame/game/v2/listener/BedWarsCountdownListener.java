package dev.daweapon.type.bedwarsgame.game.v2.listener;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.game.game.event.CountdownCancelledEvent;
import dev.daweapon.type.game.game.event.CountdownTickEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

// Announces pre-game countdown ticks and cancellations to players with sounds/messages.
public class BedWarsCountdownListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onCountdownTick(CountdownTickEvent event) {
        // Find the game for this event
        BedWarsGame game = (BedWarsGame) event.game();
        if (game == null) return;

        // Only announce at specific intervals
        if (!event.shouldAnnounce()) return;

        Text message = createCountdownMessage(event.remainingSeconds());

        if (message == null) return;

        for (BedWarsPlayer player : game.getPlayers()) {
            player.sendMessage(message);
            player.playSound(
                Sound.sound()
                    .type(Key.key("minecraft:block.note_block.pling"))
                    .volume(1f)
                    .pitch(1f)
                    .build()
            );
        }
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onCountdownCancelled(CountdownCancelledEvent event) {
        BedWarsGame game = (BedWarsGame) event.game();
        if (game == null) return;

        game.broadcastMessage(Text.of(event.reason()));
    }

    private Text createCountdownMessage(int seconds) {
        if (seconds > 10) {
            return Text.of("<e>The game starts in {} seconds!", seconds);
        } else if (seconds == 10) {
            return Text.of("<e>The game starts in <6>10</6> seconds!");
        } else if (seconds > 0) {
            return Text.of("<e>The game starts in <c>{}</c> seconds!", seconds);
        }
        return null;
    }
}
