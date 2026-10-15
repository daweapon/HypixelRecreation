package dev.daweapon.type.ravengardgeneric.event.actions.player;

import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.ravengardgeneric.entity.mob.RavengardMob;
import dev.daweapon.type.ravengardgeneric.item.RavengardItemType;
import dev.daweapon.type.ravengardgeneric.item.attribute.RavengardItemAttributeHandler;
import dev.daweapon.type.ravengardgeneric.item.statistics.RavengardItemStatistic;
import dev.daweapon.type.ravengardgeneric.user.RavengardPlayer;

public class ActionPlayerCombat implements HypixelEventClass {
    private static final double DISPLAY_DAMAGE_SCALE = 4.0;
    private static final float FIST_DAMAGE = 1f;

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onAttack(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof RavengardPlayer player)
                || !(event.getTarget() instanceof RavengardMob mob)) {
            return;
        }

        ItemStack held = player.getItemInMainHand();
        RavengardItemAttributeHandler handler = new RavengardItemAttributeHandler(held);
        RavengardItemType type = handler.getType();
        if (type != null && !type.usableBy(player.getRavengardClass())) {
            player.sendMessage("<c>Your class cannot use this item!");
            return;
        }

        float damage = FIST_DAMAGE;
        if (type != null) {
            double weapon = type.statistic(RavengardItemStatistic.DAMAGE) * handler.getStatBoost();
            if (weapon > 0) {
                damage = (float) Math.max(FIST_DAMAGE, weapon / DISPLAY_DAMAGE_SCALE);
            }
        }
        mob.damage(DamageType.PLAYER_ATTACK, damage);
    }
}
