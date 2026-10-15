package dev.daweapon.type.kuudra.fight;

import dev.daweapon.type.kuudra.fight.world.LavaCargo;
import dev.daweapon.type.kuudra.run.KuudraPhase;
import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraDialogue;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

final class KuudraPhases {
    private final KuudraFight fight;
    private final KuudraRun run;
    private final KuudraFightSpec spec;
    private final KuudraArena arena;
    private final KuudraDialogue dialogue;
    private final KuudraBossState bossState;
    private final BallistaFuel fuel;

    KuudraPhases(KuudraFight fight) {
        this.fight = fight;
        this.run = fight.run;
        this.spec = fight.spec;
        this.arena = fight.arena;
        this.dialogue = fight.dialogue;
        this.bossState = fight.bossState;
        this.fuel = fight.fuel;
    }

    void onPhase(@Nullable KuudraPhase phase) {
        if (phase == null) return;
        synchronized (run) {
            if (fight.stopped) return;
            if (!fight.emerged) fight.emerge();
            switch (phase) {
                case SUPPLIES -> {
                    if (fight.supplies.cargo.stream().noneMatch(each -> each.kind() == LavaCargo.Kind.SUPPLIES)) {
                        fight.supplies.spawnCargo(LavaCargo.Kind.SUPPLIES, spec.supplies().crates() - fight.suppliesPlaced);
                    }
                }
                case BUILD -> {
                    fight.supplies.clearCargo();
                    arena.piles().keySet().forEach(pile -> {
                        fight.supplies.filledPiles.add(pile);
                        if (fight.ballista != null) fight.ballista.supplyReceived(pile);
                    });
                    fight.suppliesPlaced = Math.max(fight.suppliesPlaced, spec.supplies().crates());
                    fight.build = new BallistaBuild(arena.piles().keySet(), spec.build().pileSeconds());
                    bossState.setCycling(false);
                    bossState.sink();
                    fight.later(160, () -> dialogue.chat("submerge_after_supplies", ThreadLocalRandom.current()).forEach(run::broadcast));
                }
                case FUEL -> {
                    fight.supplies.clearCargo();
                    if (fight.build == null) fight.build = new BallistaBuild(arena.piles().keySet(), spec.build().pileSeconds());
                    fight.build.completeAll();
                    if (fight.ballista != null) fight.ballista.showReady(fuel.loaded(), fuel.cells());
                    fight.supplies.spawnCargo(LavaCargo.Kind.FUEL, fight.supplies.missingFuel());
                    bossState.setCycling(true);
                    bossState.rise();
                    if (fight.stunCycle != null) fight.stunCycle.begin();
                }
                case DPS, STUN -> {
                    boolean stunCycleStep = fight.stunCycle != null && fight.stunCycle.begun();
                    if (!stunCycleStep || phase == KuudraPhase.STUN) {
                        fight.supplies.clearCargo();
                        if (fight.build == null) fight.build = new BallistaBuild(arena.piles().keySet(), spec.build().pileSeconds());
                        fight.build.completeAll();
                        fuel.fill();
                        if (fight.ballista != null) fight.ballista.showReady(fuel.loaded(), fuel.cells());
                        bossState.setCycling(true);
                        bossState.rise();
                    }
                    if (fight.stunCycle != null) {
                        fight.stunCycle.begin();
                        if (phase == KuudraPhase.STUN) fight.stunCycle.charged();
                        if (phase == KuudraPhase.DPS && !bossState.isStunned() && !fight.stunCycle.stunNow()) {
                            run.broadcast("<c>Kuudra must be up to be stunned; he will be stunned when a pod breaks.");
                        }
                    }
                }
                case LAIR -> {
                    if (fight.lair != null) fight.lair.begin();
                }
            }
        }
    }
}
