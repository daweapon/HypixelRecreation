package net.swofty.type.skyblockgeneric.gui.inventories.centauri;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.components.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
public enum ItemCategory {
    REGULAR("Regular Items", Material.DIAMOND_SWORD, "weapons, armor, tools, blocks and everything else"),
    PETS("Pets", Material.BONE, "pets, pet items, candy, skins and pet upgrade items"),
    TALISMANS("Talismans", Material.EMERALD, "talismans, rings, artifacts and other accessories"),
    MATERIALS("Materials", Material.IRON_INGOT, "crafting materials, drops, ores, fish and compacted items");

    private static final List<Class<? extends SkyBlockItemComponent>> PET_COMPONENTS = List.of(
            PetComponent.class, PetItemComponent.class, PetCandyComponent.class, PetSkinComponent.class);

    private static final List<Class<? extends SkyBlockItemComponent>> FUNCTIONAL_COMPONENTS = List.of(
            ArmorComponent.class, AxeComponent.class, BowComponent.class, ShortBowComponent.class, PickaxeComponent.class,
            DrillComponent.class, HoeComponent.class, ShovelComponent.class, FishingRodComponent.class,
            FishingRodPartComponent.class, FishingShipPartComponent.class, AbilityComponent.class,
            StandardItemComponent.class, BackpackComponent.class, SackComponent.class, MinionComponent.class,
            MinionUpgradeComponent.class, MinionFuelComponent.class, MinionSkinComponent.class, PotionDataComponent.class,
            RuneComponent.class, TravelScrollComponent.class, PowerStoneComponent.class, InteractableComponent.class,
            ArrowComponent.class, FishingBaitComponent.class, GemstoneComponent.class, ReforgeStoneComponent.class,
            AttributeShardComponent.class, DecorationHeadComponent.class, AbiphoneComponent.class, KatComponent.class);

    private static Map<ItemCategory, List<ItemType>> cache;

    private final String title;
    private final Material icon;
    private final String description;

    ItemCategory(String title, Material icon, String description) {
        this.title = title;
        this.icon = icon;
        this.description = description;
    }

    public static ItemCategory of(ItemType type) {
        SkyBlockItem item = new SkyBlockItem(type);
        ConfigurableSkyBlockItem config = ConfigurableSkyBlockItem.getFromID(type.name());
        if (config != null && config.getCentauriCategory() != null) {
            return valueOf(config.getCentauriCategory().toUpperCase());
        }
        if (hasAny(item, PET_COMPONENTS)) return PETS;
        if (item.hasComponent(AccessoryComponent.class)) return TALISMANS;
        if (hasAny(item, FUNCTIONAL_COMPONENTS) || isVanillaGear(item.getMaterial())) return REGULAR;
        if (item.hasComponent(EnchantedComponent.class)) return MATERIALS;
        return isBuildingBlock(item) ? REGULAR : MATERIALS;
    }

    public static synchronized List<ItemType> items(ItemCategory category) {
        if (cache == null) {
            Map<ItemCategory, List<ItemType>> built = new EnumMap<>(ItemCategory.class);
            for (ItemCategory value : values()) built.put(value, new ArrayList<>());
            for (ItemType type : ItemType.values()) built.get(of(type)).add(type);
            cache = built;
        }
        return cache.get(category);
    }

    private static final List<String> GEAR_MATERIAL_SUFFIXES = List.of("_boots", "_leggings", "_chestplate", "_helmet",
            "_sword", "_pickaxe", "_axe", "_shovel", "_hoe", "_boat", "_raft", "_minecart", "_spawn_egg");
    private static final List<String> GEAR_MATERIALS = List.of("minecart", "bow", "crossbow", "shears", "fishing_rod",
            "shield", "elytra", "carrot_on_a_stick", "flint_and_steel", "air");

    static boolean isVanillaGear(Material material) {
        String name = material.key().value();
        return GEAR_MATERIALS.contains(name) || GEAR_MATERIAL_SUFFIXES.stream().anyMatch(name::endsWith);
    }

    static boolean isBuildingBlock(SkyBlockItem item) {
        if (item.hasComponent(SkullHeadComponent.class)) return false;
        Material material = item.getMaterial();
        return material != Material.PLAYER_HEAD && material.isBlock();
    }

    private static boolean hasAny(SkyBlockItem item, List<Class<? extends SkyBlockItemComponent>> components) {
        for (Class<? extends SkyBlockItemComponent> component : components) {
            if (item.hasComponent(component)) return true;
        }
        return false;
    }
}
