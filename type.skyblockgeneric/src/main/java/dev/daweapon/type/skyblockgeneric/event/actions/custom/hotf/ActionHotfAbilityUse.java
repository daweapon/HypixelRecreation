package dev.daweapon.type.skyblockgeneric.event.actions.custom.hotf;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.hotf.HotfAbilities;
import dev.daweapon.type.skyblockgeneric.hotf.throwing.AxeThrowing;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AxeComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.event.player.PlayerUseItemEvent;

public class ActionHotfAbilityUse implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemEvent event) {
        SkyBlockItem item = new SkyBlockItem(event.getItemStack());
        if (!item.hasComponent(AxeComponent.class)) return;
        if (item.getEffectiveAbility(RegisteredAbility.AbilityActivation.RIGHT_CLICK) != null) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (AxeThrowing.tossWindowOpen(player) && AxeThrowing.throwWithCooldown(player, item, null)) return;
        HotfAbilities.activate(player);
    }
}
