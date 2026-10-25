package dev.daweapon.type.skyblockgeneric.event.actions.custom.spooky;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookEvent;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookIngredients;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerMinibossMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionGreatSpookIngredients implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void sweetFlesh(PlayerKilledSkyBlockMobEvent event) {
        SkyBlockPlayer player = event.getPlayer();
        SkyBlockMob mob = event.getKilledMob();
        if (!isSweetFleshKill(mob) || !GreatSpookIngredients.questOpen(player)) return;
        if (!GreatSpookIngredients.SWEET_AXE_ID.equals(WornArmor.id(player.getItemInMainHand()))) return;
        if (LootRoll.roll(player.getUuid(), ItemType.SWEET_FLESH, GreatSpookIngredients.SWEET_FLESH_CHANCE)) {
            GreatSpookIngredients.give(player, ItemType.SWEET_FLESH);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void deadSeed(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN || !event.getBlock().compare(Block.DEAD_BUSH)) return;
        if (!(event.getPlayer() instanceof SkyBlockPlayer player) || !GreatSpookEvent.isActive()) return;
        if (!GreatSpookIngredients.ENCHANTED_BONE_MEAL_ID.equals(WornArmor.id(player.getItemInMainHand()))) return;
        event.setCancelled(true);
        TimedConsumables.consumeHeld(player, new SkyBlockItem(player.getItemInMainHand()));
        if (LootRoll.roll(player.getUuid(), ItemType.DEAD_SEED, GreatSpookIngredients.DEAD_SEED_CHANCE)) {
            GreatSpookIngredients.give(player, ItemType.DEAD_SEED);
        }
    }

    static boolean isSweetFleshKill(SkyBlockMob mob) {
        return GreatSpookIngredients.ZOMBIES.contains(mob.getEntityType())
                && !(mob instanceof SlayerBossMob) && !(mob instanceof SlayerMinibossMob);
    }
}
