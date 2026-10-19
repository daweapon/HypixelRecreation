package dev.daweapon.type.skyblockgeneric.event.actions.player.mobdamage;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.entity.EntityAttackEvent;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.utility.MathUtility;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import dev.daweapon.type.skyblockgeneric.event.value.events.PlayerDamageMobValueUpdateEvent;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Computes and applies melee damage when a player attacks a SkyBlock mob
public class PlayerActionDamageMob implements HypixelEventClass {
    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(EntityAttackEvent event) {
        if (event.getTarget().getEntityType().equals(EntityType.PLAYER)) return;
        if (!event.getEntity().getEntityType().equals(EntityType.PLAYER)) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getEntity();
        SkyBlockItem heldItem = new SkyBlockItem(player.getItemInMainHand());
        if (heldItem.hasComponent(ItemRequirementsComponent.class)
                && !heldItem.getComponent(ItemRequirementsComponent.class).ensureCanUse(player)) return;

        Entity targetEntity = event.getTarget();
        SkyBlockMob mob;
        if (event.getTarget() instanceof SkyBlockMob skyBlockMob)
            mob = skyBlockMob;
        else return;
        LivingEntity targetLivingEntity = (LivingEntity) targetEntity;

        if (heldItem.getAttributeHandler().getPotentialType() == ItemType.ONE_HIT_SWORD) {
            AttackService.obliterate(player, mob);
            return;
        }

        ItemStatistics entityStats = mob.getStatistics();
        Map.Entry<Double, Boolean> hit = player.getStatistics().runPrimaryDamageFormula(entityStats, player, targetLivingEntity);

        double damage = (hit.getKey() + dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks.flatMeleeDamage(player, mob))
                * AttributeEffectService.outgoingDamageMultiplier(player.getHuntingData(), mob, false)
                * (1 + AttributeEffectService.lifelinePercent(player) / 100D)
                * dev.daweapon.type.skyblockgeneric.fishing.lava.CrimsonFishingGear.outgoingMultiplier(player, mob)
                * dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazeSlayerGear.outgoingMultiplier(player, mob)
                * dev.daweapon.type.skyblockgeneric.slayer.zombie.ZombieSlayerGear.outgoingMultiplier(player, mob)
                * dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentHabaneroTactics.outgoingMultiplier(player, mob)
                * dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks.outgoingMultiplier(player, mob);
        boolean critical = hit.getValue();

        PlayerDamageMobValueUpdateEvent valueEvent = new PlayerDamageMobValueUpdateEvent(
                (SkyBlockPlayer) event.getEntity(), (float) damage, mob);
        SkyBlockValueEvent.callValueUpdateEvent(valueEvent);

        if (COOLDOWN.containsKey(player.getUuid()) && System.currentTimeMillis() < COOLDOWN.get(player.getUuid()))
                return;
        COOLDOWN.put(player.getUuid(), System.currentTimeMillis() +
                MathUtility.ticksToMilliseconds(player.getStatistics().getInvulnerabilityTime()));

        float damageAmount = ((Number) valueEvent.getValue()).floatValue()
                * (float) ReforgeLoadout.outgoingMultiplier(player, mob, critical)
                + (float) dev.daweapon.type.skyblockgeneric.essence.DragonEssencePerks.firstStrikeBonus(player, mob);
        if (!AttackService.applyHit(player, mob, damageAmount, critical)) return;
        dev.daweapon.type.skyblockgeneric.item.armor.ArmorAbilityEffects.afterMeleeHit(player, mob, damageAmount, critical);
        dev.daweapon.type.skyblockgeneric.item.armor.ArmorHitEffects.onMeleeHit(player, mob);
        dev.daweapon.type.skyblockgeneric.slayer.zombie.ZombieSlayerGear.onMeleeHit(player);
        player.getPetData().dispatch(new PetEvent.MeleeHit(player, player.getPetData().getEnabledPet(), mob, damageAmount));

        double ferocity = player.getStatistics().allStatistics().getOverall(ItemStatistic.FEROCITY)
                + dev.daweapon.type.skyblockgeneric.essence.DragonEssencePerks.dragonFerocity(player, mob);
        if (player.getPosition().distance(mob.getPosition()) <= AttackService.MAX_MELEE_FEROCITY_DISTANCE)
            AttackService.scheduleExtraHits(player, mob, damageAmount, critical, ferocity);

        // Handle damage event enchantments
        SkyBlockItem mainHandItem = PlayerItemOrigin.getFromCache(player.getUuid()).get(PlayerItemOrigin.MAIN_HAND);
        double damageValue = damageAmount;

        for (SkyBlockEnchantment enchantment : mainHandItem.getAttributeHandler().getEnchantments().toList()) {
            if (enchantment.type().getEnch() instanceof DamageEventEnchant damageEventEnchant) {
                damageEventEnchant.onDamageDealt(player, targetLivingEntity, damageValue,
                        ReforgeLoadout.effectiveEnchantLevel(player, enchantment.type(), enchantment.level()));
            }
        }
    }
}
