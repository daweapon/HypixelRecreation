package dev.daweapon.type.kuudra.fight;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.kuudra.run.KuudraPhase;
import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

final class KuudraDisplay {
    private final KuudraFight fight;
    private final KuudraRun run;
    private final KuudraFightSpec spec;
    private final KuudraTokens tokens;
    private final KuudraPerks perks;
    private final KuudraLives lives;
    private final KuudraBossState bossState;
    private final BallistaFuel fuel;

    KuudraDisplay(KuudraFight fight) {
        this.fight = fight;
        this.run = fight.run;
        this.spec = fight.spec;
        this.tokens = fight.tokens;
        this.perks = fight.perks;
        this.lives = fight.lives;
        this.bossState = fight.bossState;
        this.fuel = fight.fuel;
    }

    void tickAuras() {
        List<SkyBlockPlayer> alive = fight.alivePlayers();
        var support = spec.support();
        for (SkyBlockPlayer supporter : alive) {
            double healing = perks.fraction(supporter.getUuid(), "HEALING_AURA");
            double mana = perks.fraction(supporter.getUuid(), "MANA_AURA");
            if (healing <= 0 && mana <= 0) continue;
            for (SkyBlockPlayer ally : alive) {
                double distance = ally.getPosition().distance(supporter.getPosition());
                if (healing > 0 && distance <= support.healingAuraRange()) {
                    ally.setHealth((float) Math.min(ally.getMaxHealth(), ally.getHealth() + ally.getMaxHealth() * healing));
                }
                if (mana > 0 && distance <= support.manaAuraRange()) {
                    float max = ally.getMaxMana();
                    ally.setMana((float) Math.min(max, ally.getMana() + max * support.manaRegenBase() / 100 * mana));
                }
            }
        }
    }

    void tickActionBars(List<SkyBlockPlayer> online) {
        for (SkyBlockPlayer player : online) {
            if (lives.isGhost(player.getUuid())) {
                int seconds = lives.secondsToRespawn(player.getUuid(), fight.tick);
                actionBar(player, seconds < 0 ? "<c>FINAL KILLED" : "<c>Respawning in <e>" + seconds + "s");
            }
        }
    }

    void actionBar(SkyBlockPlayer player, String markup) {
        SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.DEFENSE, Text.parse(markup), 6, 20);
    }

    KuudraFight.Hud hud(SkyBlockPlayer player) {
        KuudraPhase phase = run.state().phase();
        String progress = null;
        if (phase == KuudraPhase.SUPPLIES) progress = fight.suppliesPlaced + "/" + spec.supplies().crates();
        else if (phase == KuudraPhase.BUILD && fight.build != null) progress = fight.build.overallPercent() + "%";
        else if (phase == KuudraPhase.FUEL && !fuel.armed() && fight.stunCycle == null) progress = fuel.loaded() + "/" + fuel.cells();
        String status = null;
        if (lives.isGhost(player.getUuid())) status = lives.isFinal(player.getUuid()) ? "<c>FINAL KILLED" : "<c>DEAD";
        boolean lairStarted = fight.lair != null && fight.lair.started();
        boolean timer = (fuel.everLoaded() || (fight.stunCycle != null && fight.stunCycle.begun())) && !fight.finishing && !lairStarted;
        return new KuudraFight.Hud(tokens.balance(player.getUuid()), progress, timer ? bossState.secondsUntilSubmerge() : -1, status,
                !fight.finishing && !lairStarted, !lairStarted);
    }

    /** The centred end-of-run block from the Burning capture: KUUDRA DOWN, completion, time and tokens. */
    List<String> summary(SkyBlockPlayer player, boolean win) {
        String bar = "<a><l>" + "▬".repeat(64);
        long seconds = run.state().elapsedSeconds();
        int complete = win ? 100 : (int) Math.floor((1 - bossState.fraction()) * 100);
        return List.of(bar,
                "<f>" + " ".repeat(31) + (win ? "<6><l>KUUDRA DOWN!" : "<c><l>DEFEAT"),
                "<f>" + " ".repeat(24) + "<8>Percentage Complete: <f>" + complete + "%",
                "<f>" + " ".repeat(32) + "Time: <a>" + String.format("%02dm %02ds", seconds / 60, seconds % 60),
                "<f>" + " ".repeat(27) + "Tokens Earned: <5>" + tokens.earned(player.getUuid()),
                bar);
    }
}
