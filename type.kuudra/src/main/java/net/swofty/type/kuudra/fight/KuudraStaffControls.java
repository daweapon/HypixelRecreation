package net.swofty.type.kuudra.fight;

import net.swofty.type.kuudra.run.KuudraPhase;
import net.swofty.type.kuudra.run.KuudraRun;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

final class KuudraStaffControls {
    private final KuudraFight fight;
    private final KuudraRun run;
    private final KuudraFightSpec spec;
    private final KuudraBossState bossState;
    private final BallistaFuel fuel;

    KuudraStaffControls(KuudraFight fight) {
        this.fight = fight;
        this.run = fight.run;
        this.spec = fight.spec;
        this.bossState = fight.bossState;
        this.fuel = fight.fuel;
    }

    void completeSupplies() {
        synchronized (run) {
            if (run.state().phase() == KuudraPhase.SUPPLIES) run.advancePhase();
        }
    }

    void completeBuild() {
        synchronized (run) {
            if (fight.build != null && run.state().phase() == KuudraPhase.BUILD) fight.build.completeAll();
        }
    }

    void completeFuel() {
        synchronized (run) {
            if (run.state().phase() != KuudraPhase.FUEL) return;
            fuel.fill();
            fight.supplies.clearCargo();
            run.advancePhase();
        }
    }

    private KuudraFight.StaffResult stunReady() {
        if (fight.stunCycle == null) return KuudraFight.StaffResult.NOT_A_STUN_TIER;
        if (!fight.emerged || fight.finishing()) return KuudraFight.StaffResult.NOT_READY;
        KuudraPhase phase = run.state().phase();
        if (phase == KuudraPhase.SUPPLIES || phase == KuudraPhase.BUILD) {
            if (fight.build == null) fight.build = new BallistaBuild(fight.arena.piles().keySet(), spec.build().pileSeconds());
            run.enterPhase(KuudraPhase.FUEL);
        }
        fight.stunCycle.begin();
        if (bossState.stance() != KuudraBossState.Stance.SURFACED) {
            bossState.setCycling(true);
            bossState.rise();
            return KuudraFight.StaffResult.NOT_READY;
        }
        return KuudraFight.StaffResult.DONE;
    }

    /** Swallows the player (staff), skipping ahead to the stun cycle when needed. */
    KuudraFight.StaffResult eat(SkyBlockPlayer player) {
        synchronized (run) {
            KuudraFight.StaffResult ready = stunReady();
            if (ready != KuudraFight.StaffResult.DONE) return ready;
            fight.stunCycle.stomach().setOpen(true);
            return fight.stunCycle.eat(player) ? KuudraFight.StaffResult.DONE : KuudraFight.StaffResult.NOT_READY;
        }
    }

    /** Breaks a pod as if the player mined it (staff), stunning Kuudra. */
    KuudraFight.StaffResult stun(SkyBlockPlayer player) {
        synchronized (run) {
            KuudraFight.StaffResult ready = stunReady();
            if (ready != KuudraFight.StaffResult.DONE) return ready;
            return fight.stunCycle.breakAPod(player) ? KuudraFight.StaffResult.DONE : KuudraFight.StaffResult.NOT_READY;
        }
    }

    /** Spits the player out of Kuudra's mouth (staff). */
    KuudraFight.StaffResult spit(SkyBlockPlayer player) {
        synchronized (run) {
            if (fight.stunCycle == null) return KuudraFight.StaffResult.NOT_A_STUN_TIER;
            if (!fight.stunCycle.isInside(player)) return KuudraFight.StaffResult.NOT_READY;
            fight.stunCycle.launchOut(player);
            return KuudraFight.StaffResult.DONE;
        }
    }

    /** Takes Kuudra to 25% and starts the Infernal skip; a second use plays the rest of it at once (staff). */
    KuudraFight.StaffResult lair() {
        synchronized (run) {
            if (fight.lair == null) return KuudraFight.StaffResult.NOT_INFERNAL;
            if (!fight.emerged || fight.finishing) return KuudraFight.StaffResult.NOT_READY;
            if (fight.lair.started()) {
                fight.lair.skip();
                return KuudraFight.StaffResult.DONE;
            }
            bossState.damage(bossState.maxHealth());
            fight.combat.afterDamage();
            return fight.lair.started() ? KuudraFight.StaffResult.DONE : KuudraFight.StaffResult.NOT_READY;
        }
    }
}
