package net.swofty.type.skyblockgeneric.event.actions.custom.hotm;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.hotm.HotmAbilities;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.DrillComponent;
import net.swofty.type.skyblockgeneric.item.components.PickaxeComponent;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionHotmAbilityUse implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemEvent event) {
        SkyBlockItem item = new SkyBlockItem(event.getItemStack());
        if (!item.hasComponent(PickaxeComponent.class) && !item.hasComponent(DrillComponent.class)) return;
        if (item.getEffectiveAbility(RegisteredAbility.AbilityActivation.RIGHT_CLICK) != null) return;
        HotmAbilities.activate((SkyBlockPlayer) event.getPlayer(), item);
    }
}
