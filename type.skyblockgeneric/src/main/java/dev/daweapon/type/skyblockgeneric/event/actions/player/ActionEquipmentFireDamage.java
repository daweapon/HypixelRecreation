package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.entity.EntityDamageEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.equipment.passive.FireDamageTaken;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Scales the fire damage a player takes for the Delirium Necklace and Scourge Cloak, and remembers it for the Scourge Cloak's next hit
public class ActionEquipmentFireDamage implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof SkyBlockPlayer player)) return;
        if (player.getSkyblockDataHandler() == null) return;
        Damage damage = event.getDamage();
        if (!FireDamageTaken.isFire(damage)) return;
        float amount = (float) (damage.getAmount() * FireDamageTaken.multiplier(player));
        damage.setAmount(amount);
        FireDamageTaken.record(player, amount);
    }
}
