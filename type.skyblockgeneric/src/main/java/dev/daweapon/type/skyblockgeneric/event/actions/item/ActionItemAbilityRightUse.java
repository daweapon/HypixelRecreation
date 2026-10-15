package dev.daweapon.type.skyblockgeneric.event.actions.item;

import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.PlayerAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Triggers an item's right-click ability (sneak or regular), handling its cooldown and resource cost
public class ActionItemAbilityRightUse implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerUseItemEvent event) {
        ItemStack itemStack = event.getItemStack();
        SkyBlockItem item = new SkyBlockItem(itemStack);
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (item.hasComponent(ItemRequirementsComponent.class)
                && !item.getComponent(ItemRequirementsComponent.class).ensureCanUse(player)) return;

        UnderstandableSkyBlockItem understandable = item.toUnderstandable();
        String string = understandable.serialize();


        RegisteredAbility ability = null;
        if (player.isSneaking()) {
            ability = item.getEffectiveAbility(RegisteredAbility.AbilityActivation.SNEAK_RIGHT_CLICK);
        }
        if (ability == null) {
            ability = item.getEffectiveAbility(RegisteredAbility.AbilityActivation.RIGHT_CLICK);
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
