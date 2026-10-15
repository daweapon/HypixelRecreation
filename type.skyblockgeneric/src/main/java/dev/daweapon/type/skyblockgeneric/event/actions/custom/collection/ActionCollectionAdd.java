package dev.daweapon.type.skyblockgeneric.event.actions.custom.collection;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategory;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCollection;
import dev.daweapon.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.CoopContributionTracker;

// Adds dropped blocks/items to the player's collection and fires collection update events
public class ActionCollectionAdd implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (event.getPlayerPlaced()) return;

        SkyBlockPlayer player = event.getPlayer();

        // Process each dropped item for collection
        for (dev.daweapon.type.skyblockgeneric.item.SkyBlockItem drop : event.getDrops()) {
            ItemType type = drop.getAttributeHandler().getPotentialType();
            if (type == null) continue;

            int oldAmount = player.getCollection().get(type);
            int dropAmount = player.getCollection().increase(player, type, drop.getAmount());
            CoopContributionTracker.record(player.getUuid(), dropAmount);

            player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COLLECTION, DatapointCollection.class).setValue(
                    player.getCollection()
            );

            HypixelEventHandler.callCustomEvent(new CollectionUpdateEvent(player, type, oldAmount));

            CollectionCategory category = CollectionCategories.getCategory(type);
            if (category == null) continue;
            CollectionCategory.ItemCollection collection = category.getCollection(type);

            final int finalDropAmount = dropAmount;
            final ItemType finalType = type;
            ScheduleUtility.delay(() -> {
                SkyBlockActionBar bar = SkyBlockActionBar.getFor(player);
                int startingPriority = 5;
                int addedAmount = finalDropAmount;

                SkyBlockActionBar.DisplayReplacement existingReplacement = bar.getReplacement(SkyBlockActionBar.BarSection.DEFENSE);
                if (existingReplacement != null) {
                    startingPriority = existingReplacement.priority() + 1;
                    String plain = existingReplacement.display().plain();
                    int separator = plain.indexOf(' ');
                    if (separator > 0) {
                        try {
                            addedAmount = Integer.parseInt(plain.substring(0, separator)) + finalDropAmount;
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (player.getCollection().getReward(collection) != null) {
                    bar.addReplacement(
                            SkyBlockActionBar.BarSection.DEFENSE,
                            Text.of("<2>+{} {} <7>({}/{})",
                                    addedAmount,
                                    finalType.getDisplayName(),
                                    StringUtility.commaify(player.getCollection().get(finalType)),
                                    StringUtility.shortenNumber(player.getCollection().getReward(collection).requirement())),
                            20,
                            startingPriority
                    );
                } else { //if Collection is maxed
                    bar.addReplacement(
                            SkyBlockActionBar.BarSection.DEFENSE,
                            Text.of("<2>+{} {} <7>({})",
                                    addedAmount,
                                    finalType.getDisplayName(),
                                    StringUtility.commaify(player.getCollection().get(finalType))),
                            20,
                            startingPriority
                    );
                }
            }, 5);
        }
    }
}
