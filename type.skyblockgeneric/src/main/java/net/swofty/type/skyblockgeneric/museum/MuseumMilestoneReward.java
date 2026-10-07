package net.swofty.type.skyblockgeneric.museum;

import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Getter
public enum MuseumMilestoneReward {
    DELUXE_BANK_UPGRADE(3, "<d>Deluxe Bank Upgrade", null, null),
    APPRAISAL_SERVICE(3, "<f>Museum Appraisal Service", null, null),
    TRAVEL_SCROLL_TO_MUSEUM(5, "<9>Travel Scroll to Museum", "HUB_MUSEUM_TRAVEL_SCROLL", null),
    SUPER_DELUXE_BANK_UPGRADE(6, "<5>Super Deluxe Bank Upgrade", null, null),
    THE_HEX(8, "<d>The Hex", null, "The Hex isn't in this server yet."),
    DUST_THE_FISH(10, "<c>Dust the Fish", "DUST_THE_FISH", null),
    PREMIER_BANK_UPGRADE(10, "<c>Premier Bank Upgrade", null, null),
    SHENS_AUCTION(10, "<9>Shen's Auction", null, null),
    FLOWER_MINION_XII_RECIPE(10, "<9>Flower Minion XII <7>Recipe", null, "Minion recipe unlocks aren't in this server yet."),
    COLLECTING_TOP_HAT_EMBLEM(12, "<a>Collecting Top Hat Emblem <7>ᱪ", null, null),
    LUXURIOUS_BANK_UPGRADE(14, "<3>Luxurious Bank Upgrade", null, null),
    PALATIAL_BANK_UPGRADE(18, "<4>Palatial Bank Upgrade", null, null),
    EON_THE_FISH(20, "<c>Eon the Fish", "EON_THE_FISH", null),
    SHENS_SPECIAL(20, "<b>Shen's Special", null, null),
    GOLDEN_COLLECTING_TOP_HAT_EMBLEM(24, "<a>Golden Collecting Top Hat Emblem <7>ᱪ", null, null);

    private final int milestone;
    private final String display;
    private final @Nullable String itemId;
    private final @Nullable String unavailableReason;

    MuseumMilestoneReward(int milestone, String display, @Nullable String itemId, @Nullable String unavailableReason) {
        this.milestone = milestone;
        this.display = display;
        this.itemId = itemId;
        this.unavailableReason = unavailableReason;
    }

    public @Nullable ItemType itemType() {
        return itemId == null ? null : ItemType.get(itemId);
    }

    public boolean isAvailable() {
        return unavailableReason == null && (itemId == null || itemType() != null);
    }

    public String reason() {
        if (unavailableReason != null) return unavailableReason;
        return "This item isn't in this server yet.";
    }

    public static List<MuseumMilestoneReward> forMilestone(int milestone) {
        return Arrays.stream(values()).filter(reward -> reward.milestone == milestone).toList();
    }
}
