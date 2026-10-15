package dev.daweapon.type.kuudra.fight;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.kuudra.fight.world.EnergizedChunk;
import dev.daweapon.type.kuudra.fight.world.KuudraCannon;
import dev.daweapon.type.kuudra.fight.world.KuudraItems;
import dev.daweapon.type.kuudra.fight.world.LavaCargo;
import dev.daweapon.type.kuudra.mob.KuudraMob;
import dev.daweapon.type.kuudra.run.KuudraRun;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Set;

final class KuudraInput {
    private final KuudraFight fight;
    private final KuudraRun run;
    private final KuudraFightSpec spec;
    private final KuudraPerks perks;
    private final KuudraLives lives;
    private final Set<KuudraMob> mobs;
    private final KuudraStage stage;

    KuudraInput(KuudraFight fight) {
        this.fight = fight;
        this.run = fight.run;
        this.spec = fight.spec;
        this.perks = fight.perks;
        this.lives = fight.lives;
        this.mobs = fight.mobs;
        this.stage = fight.stage;
    }

    /** A left-click swing; true when the fight used it (firing a seat), so abilities must not also trigger. */
    boolean swing(SkyBlockPlayer player) {
        synchronized (run) {
            if (fight.stopped) return false;
            if (fight.stunCycle != null && fight.stunCycle.isInside(player)) return fight.stunCycle.swing(player);
            if (fight.ballista != null && fight.ballista.isRiding(player)) {
                fight.ballistaOps.fireBallista(player);
                return true;
            }
            for (KuudraCannon cannon : fight.artillery.cannons) {
                if (cannon.isRiding(player)) {
                    fight.artillery.fireCannon(player, cannon);
                    return true;
                }
            }
            return fight.supplies.carrying.containsKey(player.getUuid());
        }
    }

    /** A left-click on an entity: building a pile, lifting cargo, the Human Cannonball, or hitting Kuudra. */
    void attack(SkyBlockPlayer player, Entity target) {
        synchronized (run) {
            if (fight.stopped) return;
            if (fight.ballista != null) {
                String pile = fight.ballista.pileOf(target);
                if (pile != null) {
                    fight.ballistaOps.hitPile(pile, player);
                    return;
                }
            }
            for (LavaCargo item : fight.supplies.cargo) {
                if (item.owns(target)) {
                    fight.supplies.useCargo(item, player);
                    return;
                }
            }
            for (KuudraCannon cannon : fight.artillery.cannons) {
                if (cannon.owns(target)) {
                    fight.combat.humanCannonball(player);
                    return;
                }
            }
            for (EnergizedChunk chunk : fight.combat.chunks) {
                if (chunk.owns(target)) {
                    chunk.hit(player);
                    return;
                }
            }
            if (fight.boss != null && fight.boss.is(target)) fight.combat.hitKuudra(player);
        }
    }

    void sneak(SkyBlockPlayer player) {
        synchronized (run) {
            if (fight.ballista != null && fight.ballista.isRiding(player)) fight.ballista.dismount();
            fight.artillery.knockOffCannon(player);
        }
    }

    /** Right-click with a run item; true when it was one. */
    boolean useItem(SkyBlockPlayer player, ItemStack held) {
        KuudraItems.Kind kind = KuudraItems.kindOf(held);
        if (kind == null) return false;
        synchronized (run) {
            if (fight.stopped) return true;
            switch (kind) {
                case ELLES_LAVA_ROD -> {
                    if (fight.rod != null && !lives.isGhost(player.getUuid())) fight.rod.use(player);
                }
                case REMOTE_PERK_SHOP -> fight.openPerkShop(player);
                case MINIATURE_NUKE -> {
                    if (lives.isGhost(player.getUuid()) || !KuudraItems.consumeHeld(player, kind)) return true;
                    double range = spec.support().nukeRange();
                    stage.ring(player.getPosition(), range / 2, Particle.EXPLOSION_EMITTER);
                    stage.playAt(player.getPosition(), "entity.generic.explode", 0.5f);
                    for (KuudraMob mob : List.copyOf(mobs)) {
                        if (!mob.isDead() && mob.getPosition().distance(player.getPosition()) <= range) {
                            mob.damage(new EntityDamage(player, (float) (mob.getMaxHealth() * spec.support().nukeFraction())));
                        }
                    }
                }
                case ELLES_PICKAXE -> {
                }
            }
            return true;
        }
    }

    void damaged(SkyBlockPlayer player, EntityDamageEvent event) {
        synchronized (run) {
            if (fight.stopped || run.state().phase() == null || lives.isInvulnerable(player.getUuid(), fight.tick)) {
                event.setCancelled(true);
                return;
            }
            double reduction = 0;
            for (SkyBlockPlayer supporter : fight.alivePlayers()) {
                if (supporter.getPosition().distance(player.getPosition()) <= spec.support().protectiveAuraRange()) {
                    reduction = Math.max(reduction, perks.fraction(supporter.getUuid(), "PROTECTIVE_AURA"));
                }
            }
            if (reduction > 0) event.getDamage().setAmount((float) (event.getDamage().getAmount() * (1 - reduction)));
            if (fight.supplies.carrying.containsKey(player.getUuid()) && CargoCarry.dropsOnHit(event.getDamage().getAmount(),
                    player.getMaxHealth(), spec.supplies().dropDamageFraction())) {
                fight.supplies.dropCarried(player.getUuid(), true);
            }
        }
    }
}
