package dev.daweapon.type.kuudra.fight;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import net.minestom.server.entity.GameMode;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.kuudra.mob.KuudraRushers;
import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraDialogue;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.List;
import java.util.Map;

final class KuudraDeaths {
    private static final int REVIVE_TITLE_SECONDS = 5;

    private final KuudraFight fight;
    private final KuudraRun run;
    private final KuudraFightSpec spec;
    private final KuudraArena arena;
    private final KuudraDialogue dialogue;
    private final KuudraPerks perks;
    private final KuudraLives lives;

    KuudraDeaths(KuudraFight fight) {
        this.fight = fight;
        this.run = fight.run;
        this.spec = fight.spec;
        this.arena = fight.arena;
        this.dialogue = fight.dialogue;
        this.perks = fight.perks;
        this.lives = fight.lives;
    }

    boolean death(SkyBlockPlayer player) {
        synchronized (run) {
            return death(player, false);
        }
    }

    /** In the Infernal lair every death is a final kill (research K1 3.5; the Infernal capture). */
    boolean death(SkyBlockPlayer player, boolean forcedFinal) {
        long tick = fight.tick;
        player.setHealth(player.getMaxHealth());
        if (fight.stopped || run.state().phase() == null) return true;
        if (lives.isGhost(player.getUuid())) return true;
        boolean finalKill = forcedFinal || (fight.lair != null && fight.lair.entered()) || (player.getLastDamageSource() != null
                && player.getLastDamageSource().getAttacker() instanceof KuudraRushers.Chaosmite);
        boolean autoRevive = !finalKill && perks.of(player.getUuid()).autoRevives() > 0;
        KuudraLives.Death death = lives.die(player.getUuid(), tick, finalKill, autoRevive, fasterRespawn());
        if (death == KuudraLives.Death.AUTO_REVIVED) {
            perks.of(player.getUuid()).useAutoRevive();
            player.setHealth((float) (player.getMaxHealth() * spec.lives().autoReviveHealth()));
            player.sendMessage("<a>Your Auto Revive brought you back to life!");
            return true;
        }
        ghost(player, death == KuudraLives.Death.FINAL);
        if (lives.wiped(run.onlineMembers().stream().map(SkyBlockPlayer::getUuid).toList())) {
            run.finish(dev.daweapon.type.kuudra.run.KuudraRunState.Outcome.LOSE);
        }
        return true;
    }

    private double fasterRespawn() {
        return run.onlineMembers().stream().mapToDouble(member -> perks.fraction(member.getUuid(), "FASTER_RESPAWN")).max().orElse(0);
    }

    private void ghost(SkyBlockPlayer player, boolean finalKill) {
        fight.release(player);
        if (fight.stunCycle != null) fight.stunCycle.leave(player);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlying(true);
        player.setFlying(true);
        player.setInvisible(true);
        for (SkyBlockPlayer member : run.onlineMembers()) {
            member.sendMessage(member == player ? dialogue.announcement("you_died", Map.of())
                    : dialogue.announcement("died", Map.of("player", player.getUsername())));
        }
        if (finalKill) run.broadcast(dialogue.announcement("final_killed", Map.of("player", player.getUsername())));
        player.sendMessage(finalKill
                ? "<c>You were final killed! Only a Support's revive can bring you back."
                : "<c>You died! You will be respawned in " + lives.secondsToRespawn(player.getUuid(), fight.tick) + " seconds.");
        player.playSound(Sound.sound(Key.key("entity.generic.hurt"), Sound.Source.NEUTRAL, 1f, 0.89f));
        player.showTitle(Text.parse(finalKill ? "<c><l>FINAL KILLED" : "<e>You became a ghost!"),
                Text.parse("<7>Hopefully your teammates will be able to revive you!"),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(3), Duration.ofMillis(500)));
    }

    /** "You will be revived in 5s" ... "1s" under the ghost title (the Infernal capture). */
    void tickReviveTitles(List<SkyBlockPlayer> online) {
        for (SkyBlockPlayer player : online) {
            if (!lives.isGhost(player.getUuid()) || lives.isFinal(player.getUuid())) continue;
            int seconds = lives.secondsToRespawn(player.getUuid(), fight.tick);
            if (seconds < 1 || seconds > REVIVE_TITLE_SECONDS) continue;
            player.showTitle(Text.empty(), Text.parse("<a>You will be revived in " + seconds + "s"),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ZERO));
        }
    }

    void respawn(SkyBlockPlayer player) {
        unghost(player);
        player.teleport(fight.lair != null && fight.lair.entered() && arena.lair() != null ? arena.lair().landing() : arena.respawn());
        player.sendMessage("<a>You have respawned!");
    }

    void unghost(SkyBlockPlayer player) {
        lives.revive(player.getUuid(), fight.tick);
        player.setInvisible(false);
        player.setFlying(false);
        player.setAllowFlying(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getMaxHealth());
    }

    boolean revive(SkyBlockPlayer reviver, boolean finalKilled) {
        synchronized (run) {
            for (SkyBlockPlayer member : run.onlineMembers()) {
                if (!lives.isGhost(member.getUuid()) || lives.isFinal(member.getUuid()) != finalKilled) continue;
                respawn(member);
                run.broadcast(reviver.getFullDisplayName() + " <a>revived " + member.getFullDisplayName() + "<a>!");
                return true;
            }
            return false;
        }
    }

    boolean hasGhost(boolean finalKilled) {
        return run.onlineMembers().stream().anyMatch(member -> lives.isGhost(member.getUuid()) && lives.isFinal(member.getUuid()) == finalKilled);
    }
}
