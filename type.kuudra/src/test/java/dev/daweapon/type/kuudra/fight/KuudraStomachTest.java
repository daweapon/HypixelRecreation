package dev.daweapon.type.kuudra.fight;

import dev.daweapon.type.kuudra.mob.MagmaBacteria;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraStomachTest {
    private static final KuudraConfig CONFIG = KuudraConfig.load(Path.of("../configuration/skyblock/kuudra"));
    private static final KuudraFightSpec.Stomach RULES = CONFIG.fight().stomach();
    private static final UUID STUNNER = UUID.randomUUID();

    private static KuudraStomach open() {
        KuudraStomach stomach = new KuudraStomach(CONFIG.arena().stomach().pods().keySet());
        stomach.setOpen(true);
        return stomach;
    }

    @Test
    void theCapturedStomachHasThreePods() {
        assertEquals(Set.of("RIGHT", "LEFT", "BACK"), CONFIG.arena().stomach().pods().keySet());
        assertTrue(CONFIG.arena().stomach().contains(CONFIG.arena().stomach().landing()));
        assertEquals("RIGHT", CONFIG.arena().stomach().podAt(-169, 26, -167));
        assertEquals(null, CONFIG.arena().stomach().podAt(-160, 22, -168));
    }

    @Test
    void onlyAnOpenStomachSwallowsPlayersAndOnlyOnce() {
        KuudraStomach stomach = new KuudraStomach(List.of("RIGHT"));
        assertFalse(stomach.eat(STUNNER, 0));
        stomach.setOpen(true);
        assertTrue(stomach.eat(STUNNER, 5));
        assertFalse(stomach.eat(STUNNER, 6));
        assertTrue(stomach.isInside(STUNNER));
        assertEquals(5, stomach.occupiedSince());
    }

    @Test
    void twentyPickaxeHitsBreakAPodAtTheirCooldown() {
        KuudraStomach stomach = open();
        stomach.eat(STUNNER, 0);
        long tick = 0;
        for (int i = 0; i < 19; i++) {
            assertEquals(KuudraStomach.Hit.DAMAGED, stomach.hit(STUNNER, "LEFT", tick, RULES.podHitPercent(), RULES.podHitCooldownTicks()));
            assertEquals(KuudraStomach.Hit.NONE, stomach.hit(STUNNER, "LEFT", tick + 1, RULES.podHitPercent(), RULES.podHitCooldownTicks()));
            tick += RULES.podHitCooldownTicks();
        }
        assertEquals(5, stomach.health("LEFT"), 1e-9);
        assertEquals(KuudraStomach.Hit.DESTROYED, stomach.hit(STUNNER, "LEFT", tick, RULES.podHitPercent(), RULES.podHitCooldownTicks()));
        assertEquals(KuudraStomach.Hit.NONE, stomach.hit(STUNNER, "LEFT", tick + 100, RULES.podHitPercent(), RULES.podHitCooldownTicks()));
        assertEquals(100, stomach.health("RIGHT"));
        stomach.restore();
        assertEquals(100, stomach.health("LEFT"));
    }

    @Test
    void playersOutsideTheStomachCannotMineThePods() {
        KuudraStomach stomach = open();
        assertEquals(KuudraStomach.Hit.NONE, stomach.hit(STUNNER, "BACK", 0, 5, 5));
        assertEquals(KuudraStomach.Hit.NONE, stomach.hit(STUNNER, "NOWHERE", 0, 5, 5));
    }

    @Test
    void miningFrenzySpeedsUpAndHardensEveryHit() {
        assertEquals(5, KuudraStomach.podDamage(5, 0));
        assertEquals(7.5, KuudraStomach.podDamage(5, 1.5));
        assertEquals(5, KuudraStomach.hitCooldown(5, 0));
        assertEquals(3, KuudraStomach.hitCooldown(5, 0.8));
        assertEquals(1, KuudraStomach.hitCooldown(1, 5));
    }

    @Test
    void theConsumptionZoneSwallowsAfterStandingStill() {
        KuudraStomach stomach = open();
        int needed = RULES.consumptionTicks();
        for (int i = 0; i < needed - 1; i++) assertFalse(stomach.standInZone(STUNNER, true, needed));
        assertFalse(stomach.standInZone(STUNNER, false, needed));
        assertEquals(0, stomach.zoneTicks(STUNNER));
        for (int i = 0; i < needed - 1; i++) assertFalse(stomach.standInZone(STUNNER, true, needed));
        assertTrue(stomach.standInZone(STUNNER, true, needed));
        stomach.setOpen(false);
        assertFalse(stomach.standInZone(UUID.randomUUID(), true, 1));
    }

    @Test
    void flushingEmptiesTheStomach() {
        KuudraStomach stomach = open();
        UUID other = UUID.randomUUID();
        stomach.eat(STUNNER, 0);
        stomach.eat(other, 3);
        assertEquals(Set.of(STUNNER, other), Set.copyOf(stomach.flush()));
        assertTrue(stomach.inside().isEmpty());
        assertEquals(-1, stomach.occupiedSince());
    }

    @Test
    void antibioticCutsMagmaBacteriaHits() {
        assertEquals(1750, MagmaBacteria.reduced(1750, 0), 1e-9);
        assertEquals(175, MagmaBacteria.reduced(1750, 0.9), 1e-9);
        assertEquals(0, MagmaBacteria.reduced(1750, 2), 1e-9);
    }

    @Test
    void theStunTiersCapturedTimings() {
        KuudraFightSpec.Stun stun = CONFIG.fight().stun();
        assertEquals(7, stun.stunSeconds());
        assertEquals(60, stun.fuelSurfaceSeconds());
        assertEquals(40, stun.chargedSurfaceSeconds(KuudraTier.BURNING));
        assertEquals(List.of(110, 30, 50), stun.elleEatenTicks());
        assertEquals(72, RULES.exitDelayTicks());
    }
}
