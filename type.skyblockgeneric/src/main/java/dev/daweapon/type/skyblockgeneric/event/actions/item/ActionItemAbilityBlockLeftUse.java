package dev.daweapon.type.skyblockgeneric.event.actions.item;

import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbilityComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.PlayerAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Triggers an item's block-left-click ability, handling its cooldown and resource cost
public class ActionItemAbilityBlockLeftUse implements HypixelEventClass {
    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockBreakEvent event) {
        ItemStack itemStack = event.getPlayer().getItemInMainHand();
        SkyBlockItem item = new SkyBlockItem(itemStack);
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (item.hasComponent(AbilityComponent.class)) {
            AbilityComponent abilityComponent = item.getComponent(AbilityComponent.class);
            RegisteredAbility ability;
            ability = abilityComponent.getAbility(RegisteredAbility.AbilityActivation.LEFT_CLICK_BLOCK);
            if (ability != null) {
                // Cancelling here will prevent the block from being broken
                event.setCancelled(true);
                if (!ability.isReady(player, item)) return;
                if (!ability.getCost().canUse(player)) {
                    ability.getCost().onFail(player);
                    return;
                }

                PlayerAbilityHandler abilityHandler = player.getAbilityHandler();
                if (!abilityHandler.canUseAbility(item, CombatPetHooks.cooldownTicks(player, ability.getCooldownTicks()))) {
                    player.sendMessage("<c>This ability is on cooldown for {}s.",
                            Math.round((float) abilityHandler.getRemainingCooldown(item, CombatPetHooks.cooldownTicks(player, ability.getCooldownTicks())) / 1000));
                    return;
                }

                abilityHandler.startAbilityCooldown(item);
                if(ability.execute(player, item, event.getBlockPosition(), event.getBlockFace())){
                    abilityHandler.startAbilityCooldown(item);
                }
            }
        }
    }
}
