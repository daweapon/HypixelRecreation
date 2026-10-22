package dev.daweapon.type.skyblockgeneric.event.actions.farming;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.farming.animal.AnimalLeash;
import dev.daweapon.type.skyblockgeneric.farming.animal.FarmAnimal;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;

// Right-clicking a farm animal with a Lead makes it follow the player, so it can be brought to Jake
public class ActionAnimalLeash implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN || !(event.getTarget() instanceof SkyBlockMob mob) || mob.isDead()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType() != ItemType.LEAD) return;
        FarmAnimal animal = FarmAnimal.of(mob);
        if (animal == null || !animal.canBeLeashed()) return;
        AnimalLeash.toggle(player, mob, animal);
    }
}
