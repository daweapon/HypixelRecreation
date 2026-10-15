package dev.daweapon.type.skyblockgeneric.event.actions.custom.hotm;

import net.minestom.server.event.player.PlayerUseItemEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.hotm.HotmAbilities;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.DrillComponent;
import dev.daweapon.type.skyblockgeneric.item.components.PickaxeComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionHotmAbilityUse implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemEvent event) {
        SkyBlockItem item = new SkyBlockItem(event.getItemStack());
        if (!item.hasComponent(PickaxeComponent.class) && !item.hasComponent(DrillComponent.class)) return;
        if (item.getEffectiveAbility(RegisteredAbility.AbilityActivation.RIGHT_CLICK) != null) return;
        HotmAbilities.activate((SkyBlockPlayer) event.getPlayer(), item);
    }
}
