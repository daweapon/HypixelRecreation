package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import lombok.Getter;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.fishing.bestiary.FishingBestiaryFamily;
import net.swofty.type.skyblockgeneric.fishing.bestiary.FishingBestiarySection;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Getter
public enum BestiaryCategories {
    YOUR_ISLAND("<a>Your island", BestiaryCategory.PRIVATE_ISLAND.values()),
    HUB("<a>Hub", BestiaryCategory.HUB.values()),
    DEEP_CAVERNS("<b>Deep Caverns", BestiaryCategory.DEEP_CAVERNS.values()),
    DWARVEN_MINES("<2>Dwarven Mines", BestiaryCategory.DWARVEN_MINES.values()),
    MYTHOLOGICAL_CREATURES("<b>Mythological Creatures", BestiaryCategory.MYTHOLOGICAL_CREATURES.values()),
    CRIMSON_ISLE("<c>Crimson Isle", BestiaryCategory.CRIMSON_ISLE.values()),
    KUUDRA("<c>Kuudra", BestiaryCategory.KUUDRA.values()),
    FISHING(FishingBestiarySection.FISHING),
    FISHING_LAVA(FishingBestiarySection.LAVA),
    FISHING_SPOOKY_FESTIVAL(FishingBestiarySection.SPOOKY_FESTIVAL),
    FISHING_FISHING_FESTIVAL(FishingBestiarySection.FISHING_FESTIVAL),
    FISHING_WINTER(FishingBestiarySection.WINTER),
    FISHING_BACKWATER_BAYOU(FishingBestiarySection.BACKWATER_BAYOU),
    ;

    private static final GUIMaterial DEFAULT_ICON = new GUIMaterial("c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56");

    private final String displayName;
    private final BestiaryEntry[] entries;
    private final @Nullable FishingBestiarySection fishingSection;

    BestiaryCategories(String displayName, BestiaryEntry[] entries) {
        this.displayName = displayName;
        this.entries = entries;
        this.fishingSection = null;
    }

    BestiaryCategories(FishingBestiarySection section) {
        this.displayName = section.getDisplayName();
        this.entries = FishingBestiaryFamily.in(section);
        this.fishingSection = section;
    }

    public static BestiaryCategories of(FishingBestiarySection section) {
        return Arrays.stream(values())
                .filter(category -> category.fishingSection == section)
                .findFirst().orElseThrow();
    }

    public static List<BestiaryCategories> fishing() {
        return Arrays.stream(values()).filter(category -> category.fishingSection != null).toList();
    }

    public String getParentTitle() {
        return fishingSection == null ? "Bestiary" : "Fishing";
    }

    public GUIMaterial getIcon() {
        return fishingSection == null ? DEFAULT_ICON : fishingSection.getIcon();
    }

    public List<String> getDescription() {
        if (fishingSection != null) return fishingSection.getDescription();
        return List.of("<7>View all of the mobs that you've",
                "<7>found and killed on " + displayName + "<7>.");
    }

    public List<BestiaryMob> getAllMobs() {
        return Arrays.stream(entries)
                .flatMap(entry -> entry.getMobs().stream())
                .toList();
    }
}
