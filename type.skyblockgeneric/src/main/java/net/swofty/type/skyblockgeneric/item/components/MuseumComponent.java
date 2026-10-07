package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;

import java.util.List;
import java.util.Map;

@Getter
public class MuseumComponent extends SkyBlockItemComponent {
    private final String category;
    private final String gameStage;
    private final int donationXp;
    private final Map<String, String> parent;
    private final List<String> mappedItemIds;

    public MuseumComponent(String category, String gameStage, int donationXp,
                           Map<String, String> parent, List<String> mappedItemIds) {
        this.category = category;
        this.gameStage = gameStage;
        this.donationXp = donationXp;
        this.parent = parent;
        this.mappedItemIds = mappedItemIds;
    }
}