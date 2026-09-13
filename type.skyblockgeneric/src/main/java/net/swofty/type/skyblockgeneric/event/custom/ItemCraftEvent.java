package net.swofty.type.skyblockgeneric.event.custom;

import lombok.Getter;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMinionData;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.MinionComponent;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
// Fired when a player crafts an item; also records newly crafted minion types/tiers
public class ItemCraftEvent implements PlayerInstanceEvent {
    private final SkyBlockPlayer player;
    private final SkyBlockItem craftedItem;
    private final SkyBlockRecipe<?> recipe;

    public ItemCraftEvent(SkyBlockPlayer player, SkyBlockItem craftedItem, SkyBlockRecipe<?> recipe) {
        this.player = player;
        this.craftedItem = craftedItem;
        this.recipe = recipe;

        if (craftedItem.hasComponent(MinionComponent.class)) {
            var datapoint = player.getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.MINION_DATA, DatapointMinionData.class);
            DatapointMinionData.ProfileMinionData playerData = datapoint.getValue();
            String name = craftedItem.getAttributeHandler().getMinionType().name();
            Integer tier = craftedItem.getAttributeHandler().getMinionData().tier();
            List<Map.Entry<String, List<Integer>>> craftedMinions = new ArrayList<>(playerData.craftedMinions());
            AbstractMap.SimpleEntry<String, List<Integer>> minion = new AbstractMap.SimpleEntry<>(name, List.of(tier));
            boolean minionTypeInList = false;
            boolean alreadyHasTier = false;

            for (int i = 0; i < craftedMinions.size(); i++) {
                if (Objects.equals(craftedMinions.get(i).getKey(), name)) {
                    minionTypeInList = true;
                    for (Integer integer : craftedMinions.get(i).getValue()) {
                        if (Objects.equals(integer, tier)) {
                            alreadyHasTier = true;
                            break;
                        }
                    }
                    if (!alreadyHasTier) {
                        List<Integer> tiers = new ArrayList<>(craftedMinions.get(i).getValue());
                        tiers.add(tier);
                        minion.setValue(tiers);
                        craftedMinions.set(i, minion);
                    }
                    break;
                }
            }

            if (alreadyHasTier) return;

            if (!minionTypeInList) {
                craftedMinions.add(minion);
            }

            datapoint.setValue(new DatapointMinionData.ProfileMinionData(craftedMinions));
        }
    }
}
