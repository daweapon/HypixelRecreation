package dev.daweapon.type.skyblockgeneric.event.actions.item;

import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.attribute.attributes.ItemAttributePotionData;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.potion.PotionEffectService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Handles potion consumption when a player finishes drinking a potion.
 * Uses PlayerFinishItemUseEvent which fires after the drinking animation completes.
 */
public class ActionDrinkPotion implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerFinishItemUseEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        ItemStack itemStack = event.getItemStack();

        // Only handle potions (not splash or lingering)
        if (itemStack.material() != Material.POTION) {
            return;
        }

        // Check if this is a SkyBlock item with potion data
        if (!SkyBlockItem.isSkyBlockItem(itemStack)) {
            return;
        }

        SkyBlockItem skyBlockItem = new SkyBlockItem(itemStack);
        ItemAttributePotionData.PotionData potionData = skyBlockItem.getAttributeHandler().getPotionData();

        // If no potion data, it's a vanilla potion or water bottle - ignore
        if (potionData == null) {
            return;
        }

        // Don't process base potions (water, awkward, thick, mundane)
        String effectType = potionData.getEffectType();
        if (effectType.equals("WATER") || effectType.equals("AWKWARD") ||
            effectType.equals("THICK") || effectType.equals("MUNDANE")) {
            return;
        }

        // Apply the potion effect
        PotionEffectService.applyToPlayer(player, potionData);
        awardDrinkAchievements(player, effectType, potionData.getLevel());

        // Replace the potion with a glass bottle
        int heldSlot = player.getHeldSlot();
        ItemStack currentItem = player.getInventory().getItemStack(heldSlot);

        if (currentItem.amount() > 1) {
            // If stacked, reduce count and try to add glass bottle to inventory
            player.getInventory().setItemStack(heldSlot, currentItem.withAmount(currentItem.amount() - 1));
            // Try to add glass bottle
            if (!player.getInventory().addItemStack(ItemStack.of(Material.GLASS_BOTTLE))) {
                // Inventory full, drop it
                player.dropItem(ItemStack.of(Material.GLASS_BOTTLE));
            }
        } else {
            // Single potion, replace with glass bottle
            player.getInventory().setItemStack(heldSlot, ItemStack.of(Material.GLASS_BOTTLE));
        }
    }

    private static void awardDrinkAchievements(SkyBlockPlayer player, String effectType, int level) {
        switch (effectType) {
            case "BURNING" -> player.getAchievementHandler().completeAchievement("skyblock.no_enchants_needed");
            case "AGILITY" -> player.getAchievementHandler().completeAchievement("skyblock.agile");
            case "ADRENALINE" -> player.getAchievementHandler().completeAchievement("skyblock.brain_power");
            case "CRITICAL" -> {
                if (level >= 3) player.getAchievementHandler().completeAchievement("skyblock.overkill");
            }
            default -> {
            }
        }
    }
}
