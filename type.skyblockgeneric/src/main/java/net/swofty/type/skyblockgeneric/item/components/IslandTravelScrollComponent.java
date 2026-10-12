package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.warps.TravelScrollAwards;
import net.swofty.type.skyblockgeneric.warps.TravelScrollIslands;

import java.util.ArrayList;
import java.util.List;

@Getter
public class IslandTravelScrollComponent extends SkyBlockItemComponent {
    private final TravelScrollIslands island;

    public IslandTravelScrollComponent(String islandName) {
        this.island = TravelScrollIslands.getFromInternalName(islandName);
        if (island == null) {
            throw new IllegalArgumentException("Unknown travel scroll island: " + islandName);
        }
        addInheritedComponent(new LoreUpdateComponent(List.of(
                "<7>Consume this item to add its",
                "<7>destination to your fast travel",
                "<7>options."
        ), false));
        addInheritedComponent(new ExtraRarityComponent("TRAVEL SCROLL"));
        addInheritedComponent(new InteractableComponent(this::onInteract, this::onInteract, null));
        addInheritedComponent(new TrackedUniqueComponent());
    }

    private void onInteract(SkyBlockPlayer player, SkyBlockItem item) {
        DatapointStringList visited = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.VISITED_ISLANDS, DatapointStringList.class);
        if (visited.getValue().contains(island.getInternalName())) {
            player.sendMessage("<c>You have already unlocked this travel scroll!");
            return;
        }
        if (item.hasComponent(ItemRequirementsComponent.class)
                && !item.getComponent(ItemRequirementsComponent.class).ensureCanUse(player)) {
            return;
        }
        String blocked = island.entryBlockReason(player);
        if (blocked != null) {
            player.sendMessage(blocked);
            return;
        }

        player.takeItem(item.getAttributeHandler().getPotentialType(), 1);
        List<String> updated = new ArrayList<>(visited.getValue());
        updated.add(island.getInternalName());
        visited.setValue(updated);
        TravelScrollAwards.syncSkyBlockXP(player);
        player.sendMessage("<a>You have unlocked the {} <a>travel scroll!", island.getDescriptiveName());
    }
}
