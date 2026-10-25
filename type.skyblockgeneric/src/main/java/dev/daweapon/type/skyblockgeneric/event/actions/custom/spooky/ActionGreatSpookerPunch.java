package dev.daweapon.type.skyblockgeneric.event.actions.custom.spooky;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookFear;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookStaff;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.event.entity.EntityAttackEvent;

public class ActionGreatSpookerPunch implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof SkyBlockPlayer spooker) || !(event.getTarget() instanceof SkyBlockPlayer target)) return;
        if (!GreatSpookFear.STAFF_ID.equals(WornArmor.id(spooker.getItemInMainHand()))) return;
        GreatSpookStaff.spooker(spooker, target);
    }
}
