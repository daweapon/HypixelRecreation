package dev.daweapon.type.skyblockgeneric.communityupgrades;

import lombok.Getter;
import net.minestom.server.item.Material;

import java.util.List;

@Getter
public enum CommunityUpgradeId {
    ISLAND_SIZE(Scope.PROFILE, "Island Size", Material.GRASS_BLOCK, "Increases the width of your private island."),
    MINION_SLOTS(Scope.PROFILE, "Minion Slots", Material.CRAFTING_TABLE, "Adds extra minion slots to your island."),
    GUESTS_LIMIT(Scope.PROFILE, "Guests Limit", Material.PLAYER_HEAD, "Raises how many guests can visit your island."),
    COOP_SLOTS(Scope.PROFILE, "Co-op Slots", Material.LEAD, "Raises how many players can join your co-op."),
    COINS_ALLOWANCE(Scope.PROFILE, "Coins Allowance", Material.GOLD_INGOT, "Pays coins each day you log in."),
    ARMOR_WARDROBE(Scope.ACCOUNT, "Armor Wardrobe Slots", Material.LEATHER_CHESTPLATE, "Adds Armor Wardrobe slots."),
    EQUIPMENT_WARDROBE(Scope.ACCOUNT, "Equipment Wardrobe Slots", Material.CHAINMAIL_HELMET, "Adds Equipment Wardrobe slots."),
    LOADOUT_SLOTS(Scope.ACCOUNT, "Loadout Slots", Material.ENDER_CHEST, "Adds Loadout slots."),
    HOTM_SLOTS(Scope.ACCOUNT, "Heart of the Mountain Slots", Material.EMERALD, "Adds Heart of the Mountain tree slots."),
    HOTF_SLOTS(Scope.ACCOUNT, "Heart of the Forest Slots", Material.JUNGLE_SAPLING, "Adds Heart of the Forest tree slots."),
    ENDER_CHEST_PAGES(Scope.ACCOUNT, "Ender Chest Pages", Material.ENDER_CHEST, "Adds Ender Chest pages."),
    ACCESSORY_BAG(Scope.ACCOUNT, "Accessory Bag Slots", Material.REDSTONE, "Adds Accessory Bag slots."),
    SACK_OF_SACKS(Scope.ACCOUNT, "Sack of Sacks Slots", Material.BUNDLE, "Adds Sack of Sacks slots."),
    MAGIC_FIND(Scope.ACCOUNT, "Magic Find", Material.ENCHANTED_GOLDEN_APPLE, "Grants Magic Find."),
    BAZAAR_FLIPPER(Scope.ACCOUNT, "Bazaar Flipper", Material.GOLD_BLOCK, "More Bazaar orders and a lower tax."),
    GARDEN_FARMING_FORTUNE(Scope.ACCOUNT, "Garden Farming Fortune", Material.WHEAT, "Grants Farming Fortune on the Garden.");

    public enum Scope {
        PROFILE, ACCOUNT
    }

    private final Scope scope;
    private final String displayName;
    private final Material icon;
    private final String description;

    CommunityUpgradeId(Scope scope, String displayName, Material icon, String description) {
        this.scope = scope;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public boolean isImplemented() {
        return this != ISLAND_SIZE && this != BAZAAR_FLIPPER;
    }

    public String formatValue(double value) {
        return switch (this) {
            case ISLAND_SIZE -> (int) value + "x" + (int) value;
            case COINS_ALLOWANCE -> String.format("%,d coins a day", (long) value);
            case BAZAAR_FLIPPER -> "+" + (int) value + " orders";
            case GARDEN_FARMING_FORTUNE -> "+" + (int) value + " Farming Fortune";
            case MAGIC_FIND -> "+" + (int) value + " Magic Find";
            default -> "+" + (int) value;
        };
    }

    public List<String> lore() {
        return List.of(description);
    }
}
