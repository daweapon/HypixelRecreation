package dev.daweapon.type.skyblockgeneric.event.actions.custom.collection;

import dev.daweapon.type.skyblockgeneric.collection.CollectionNames;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategory;
import dev.daweapon.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.Arrays;

// Sends collection-unlocked and collection-level-up chat messages as a player's collection grows
public class ActionCollectionDisplay implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CollectionUpdateEvent event) {

        if (event.getOldValue() == 0 && CollectionCategories.getCategory(event.getItemType()) != null) {
            event.getPlayer().sendMessage(
                    "<hover:'<e>Click to view your {0} Collection!'><click:run:'/viewcollection {1}'>  <6><l>COLLECTION UNLOCKED </l><e>{0}</click></hover>",
                    CollectionNames.displayName(event.getItemType()), event.getItemType().name()
            );
            return;
        }
        if (CollectionCategories.getCategory(event.getItemType()) == null) return;

        CollectionCategory.ItemCollection collection = CollectionCategories.getCategory(event.getItemType()).getCollection(event.getItemType());
        CollectionCategory.ItemCollectionReward newReward = event.getPlayer().getCollection().getReward(collection);
        CollectionCategory.ItemCollectionReward oldReward = null;

        for (CollectionCategory.ItemCollectionReward reward : collection.rewards()) {
            if (event.getOldValue() < reward.requirement()) {
                oldReward = reward;
                break;
            }
        }

        if (oldReward == newReward) return;

        if (oldReward != null) {
            SkyBlockPlayer player = event.getPlayer();

            player.sendMessage("<e><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            int placement = collection.getPlacementOf(oldReward);

            int unlockedLevel = placement + 1;
            if (placement == 0) {
                player.sendMessage(
                        "<hover:'<e>Click to view your {0} Collection!'><click:run:'/viewcollection {2}'>  <6><l>COLLECTION LEVEL UP </l><e>{0} {1:roman}</click></hover>",
                        CollectionNames.displayName(event.getItemType()), unlockedLevel, event.getItemType().name()
                );
            } else {
                player.sendMessage(
                        "<hover:'<e>Click to view your {0} Collection!'><click:run:'/viewcollection {3}'>  <6><l>COLLECTION LEVEL UP </l><e>{0} <8>{1:roman}➜<e>{2:roman}</click></hover>",
                        CollectionNames.displayName(event.getItemType()), collection.getPlacementOf(oldReward), unlockedLevel, event.getItemType().name()
                );
            }

            player.sendMessage(" ");

            if (oldReward.unlocks().length > 0) {
                player.sendMessage("  <a><l>REWARDS");
                Arrays.stream(oldReward.unlocks()).forEach(unlock -> {
                    switch (unlock.type()) {
                        case RECIPE -> {
                            CollectionCategory.UnlockRecipe recipeUnlock = (CollectionCategory.UnlockRecipe) unlock;
                            if (recipeUnlock.getRecipe() == null) {
                                player.sendMessage("    <7>{} Recipes", recipeUnlock.unlockedItemType().getDisplayName());
                                return;
                            }
                            ItemStack.Builder item = ((CollectionCategory.UnlockRecipe) unlock).getRecipe().getResult().getItemStackBuilder();
                            item = new NonPlayerItemUpdater(item).getUpdatedItem();

                            player.sendMessage("    <7>{} Recipes", StringUtility.getTextFromComponent(item.build().get(DataComponents.CUSTOM_NAME)));
                        }
                        case XP -> {
                            player.sendMessage("    <8>+<b>{} SkyBlock XP", ((CollectionCategory.UnlockXP) unlock).xp());
                        }
                        case SKILL_XP -> {
                            CollectionCategory.UnlockSkillXP skillXP = (CollectionCategory.UnlockSkillXP) unlock;
                            player.getSkills().increase(player, skillXP.skill(), (double) skillXP.xp());
                            player.sendMessage("    " + skillXP.display());
                        }
                        case STAT -> player.sendMessage("    " + ((CollectionCategory.UnlockStat) unlock).display());
                    }
                });
            }

            player.sendMessage("<e><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        }
    }
}
