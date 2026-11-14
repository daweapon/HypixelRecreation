package dev.daweapon.type.skywarsgame.events;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.entity.EntityAttackEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.game.SkywarsGameStatus;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;

public class ActionMobAttack implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onMobAttack(EntityAttackEvent event) {
        Entity attacker = event.getEntity();
        Entity target = event.getTarget();

        if (attacker instanceof SkywarsPlayer) return;
        if (!(attacker instanceof EntityCreature mob)) return;
        if (!(target instanceof SkywarsPlayer victim)) return;

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(victim);
        if (game == null || game.getGameStatus() != SkywarsGameStatus.IN_PROGRESS) return;
        if (victim.isEliminated()) return;

        float damage = getMobDamage(mob);

        victim.damage(new Damage(DamageType.MOB_ATTACK, mob, mob, null, damage));
    }

    private float getMobDamage(EntityCreature mob) {
        EntityType type = mob.getEntityType();

        if (type == EntityType.ZOMBIE) return 3.0f;
        if (type == EntityType.SKELETON) return 2.0f;
        if (type == EntityType.SPIDER) return 2.0f;
        if (type == EntityType.CAVE_SPIDER) return 2.0f;
        if (type == EntityType.CREEPER) return 0.0f;
        if (type == EntityType.SLIME) return 2.0f;
        if (type == EntityType.SILVERFISH) return 1.0f;
        if (type == EntityType.ENDERMAN) return 7.0f;
        if (type == EntityType.WOLF) return 4.0f;
        if (type == EntityType.BLAZE) return 6.0f;
        if (type == EntityType.WITHER_SKELETON) return 8.0f;
        if (type == EntityType.WITCH) return 0.0f;
        if (type == EntityType.VINDICATOR) return 13.0f;
        if (type == EntityType.PIGLIN) return 5.0f;
        if (type == EntityType.PIGLIN_BRUTE) return 13.0f;
        if (type == EntityType.ZOMBIFIED_PIGLIN) return 5.0f;
        if (type == EntityType.IRON_GOLEM) return 21.0f;
        if (type == EntityType.RAVAGER) return 12.0f;
        return 2.0f;
    }
}
