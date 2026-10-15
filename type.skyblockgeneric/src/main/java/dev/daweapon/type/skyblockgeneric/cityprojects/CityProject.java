package dev.daweapon.type.skyblockgeneric.cityprojects;

import lombok.Getter;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.item.ItemType;

import java.util.List;

/** The City Project the community is funding, with the wiki's components, costs and Fame. */
@Getter
public enum CityProject {
    HUB_REVAMP("Hub Revamp", Material.BRICKS, 400_000, ItemType.VILLAGER_DOLL, 10,
            "Donate materials to help spruce up\nthe <a>Hub Village <7>for the new year!",
            List.of(
                    new Component("Combat Settlement", Material.WOODEN_SWORD,
                            "Everything from the Archer Range\nto the Thaumaturgist, now all in the\nsame place!",
                            List.of(new Cost(ItemType.ENCHANTED_OAK_LOG, 8), new Cost(ItemType.ENCHANTED_COBBLESTONE, 8)), 0, 200),
                    new Component("Community Center Touch-ups", Material.CLOCK,
                            "Work together to elevate the\nCommunity Center... quite literally!",
                            List.of(new Cost(ItemType.ENCHANTED_ACACIA_LOG, 16)), 0, 200),
                    new Component("Museum Restructure", Material.QUARTZ,
                            "Help give the Museum some\nmuch-needed touch-ups, including a\nnew categorization system!",
                            List.of(new Cost(ItemType.ENCHANTED_GOLD_INGOT, 32)), 0, 200),
                    new Component("Sewer System", Material.IRON_BARS,
                            "You can't have a functioning village\nwithout a functioning sewer system.\nWell, you could, but I wouldn't want to live there.",
                            List.of(new Cost(ItemType.ENCHANTED_IRON_BLOCK, 1), new Cost(ItemType.MAGICAL_WATER_BUCKET, 1)), 200, 1000)
            ));

    public static final CityProject CURRENT = HUB_REVAMP;

    private final String displayName;
    private final Material icon;
    private final long requiredContributions;
    private final ItemType memento;
    private final int mementoContributions;
    private final String description;
    private final List<Component> components;

    CityProject(String displayName, Material icon, long requiredContributions, ItemType memento,
                int mementoContributions, String description, List<Component> components) {
        this.displayName = displayName;
        this.icon = icon;
        this.requiredContributions = requiredContributions;
        this.memento = memento;
        this.mementoContributions = mementoContributions;
        this.description = description;
        this.components = components;
    }

    public record Cost(ItemType item, int amount) {
    }

    public record Component(String name, Material icon, String description, List<Cost> costs, int bits, int fame) {
    }
}
