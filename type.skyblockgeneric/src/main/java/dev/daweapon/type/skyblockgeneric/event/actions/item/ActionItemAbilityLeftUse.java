package dev.daweapon.type.skyblockgeneric.event.actions.item;

import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbilityComponent;
import dev.daweapon.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.PlayerAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Triggers an item's left-click ability (sneak or regular), handling its cooldown and resource cost
public class ActionItemAbilityLeftUse implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerHandAnimationEvent event) {
        ItemStack itemStack = event.getPlayer().getItemInMainHand();
        SkyBlockItem item = new SkyBlockItem(itemStack);
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (item.hasComponent(ItemRequirementsComponent.class)
                && !item.getComponent(ItemRequirementsComponent.class).ensureCanUse(player)) return;

        if (item.hasComponent(AbilityComponent.class)) {
            AbilityComponent abilityComponent = item.getComponent(AbilityComponent.class);

            RegisteredAbility ability = null;
            if(player.isSneaking()) {
                ability = abilityComponent.getAbility(RegisteredAbility.AbilityActivation.SNEAK_LEFT_CLICK);
            }
            if (ability == null) {
                ability = abilityComponent.getAbility(RegisteredAbility.AbilityActivation.LEFT_CLICK);
            }

            if (ability != null) {
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

                if(ability.execute(player, item)){
                    abilityHandler.startAbilityCooldown(item);
                }
            }
        }
    }
}
