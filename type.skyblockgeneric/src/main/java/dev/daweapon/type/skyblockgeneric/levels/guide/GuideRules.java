package dev.daweapon.type.skyblockgeneric.levels.guide;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategories;
import dev.daweapon.type.skyblockgeneric.collection.CollectionCategory;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMinionData;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AccessoryComponent;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.minion.MinionRegistry;
import dev.daweapon.type.skyblockgeneric.museum.MuseumCatalogue;
import dev.daweapon.type.skyblockgeneric.museum.MuseumEntry;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerDefinition;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRegistry;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRequirement;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;

public final class GuideRules {
    private GuideRules() {
    }

    public static GuideRule of(GuideKind kind, String ref) {
        if (ref == null) return GuideRule.UNAVAILABLE;
        try {
            return switch (kind) {
                case SKILL -> skill(ref);
                case SLAYER -> slayer(ref);
                case COLLECTION -> collection(ref);
                case MINION -> minion(ref);
                case MOB_TYPE -> mobType(ref);
                case MUSEUM -> museum(ref);
                case ACCESSORY -> accessory(ref);
                case PET -> pet(ref);
                case OTHER -> GuideRule.UNAVAILABLE;
            };
        } catch (RuntimeException exception) {
            return GuideRule.UNAVAILABLE;
        }
    }

    private static GuideRule skill(String ref) {
        String[] parts = ref.split(":");
        SkillCategories category = SkillCategories.valueOf(parts[0]);
        int level = Integer.parseInt(parts[1]);
        boolean available = !category.isCosmetic() && level <= category.asCategory().getHighestLevel();
        return new GuideRule(available, scan -> scan.player().getSkills().getCurrentLevel(category) >= level);
    }

    private static GuideRule slayer(String ref) {
        String[] parts = ref.split(":");
        SlayerType type = SlayerType.valueOf(parts[0]);
        int level = Integer.parseInt(parts[1]);
        SlayerDefinition definition = SlayerRegistry.get(type);
        boolean available = definition != null && definition.enabled() && level <= definition.levels().size();
        return new GuideRule(available, scan -> SlayerRequirement.level(scan.player(), type) >= level);
    }

    private static GuideRule collection(String ref) {
        String[] parts = ref.split(":");
        ItemType type = ItemType.get(parts[0]);
        int tier = Integer.parseInt(parts[1]);
        CollectionCategory category = type == null ? null : CollectionCategories.getCategory(type);
        CollectionCategory.ItemCollection collection = category == null ? null : category.getCollection(type);
        if (collection == null || collection.rewards().length < tier) return GuideRule.UNAVAILABLE;
        int required = collection.rewards()[tier - 1].requirement();
        return new GuideRule(true, scan -> scan.player().getCollection().get(type) >= required);
    }

    private static GuideRule minion(String ref) {
        String[] parts = ref.split(":");
        MinionRegistry registry = MinionRegistry.valueOf(parts[0]);
        int tier = Integer.parseInt(parts[1]);
        boolean exists = registry.asSkyBlockMinion().getTiers().stream().anyMatch(candidate -> candidate.tier() == tier);
        return new GuideRule(exists, scan -> craftedMinion(scan.player(), registry.name(), tier));
    }

    private static boolean craftedMinion(SkyBlockPlayer player, String name, int tier) {
        DatapointMinionData.ProfileMinionData data = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.MINION_DATA, DatapointMinionData.class).getValue();
        for (Map.Entry<String, List<Integer>> entry : data.craftedMinions()) {
            if (entry.getKey().equals(name) && entry.getValue().contains(tier)) return true;
        }
        return false;
    }

    private static GuideRule mobType(String ref) {
        MobType type = MobType.valueOf(ref);
        return new GuideRule(true, scan -> scan.player().getCollectedMobTypesData().hasClaimed(type.name()));
    }

    private static GuideRule museum(String ref) {
        MuseumEntry entry = MuseumCatalogue.get().entry(ref).orElse(null);
        if (entry == null || !entry.isAvailable()) return GuideRule.UNAVAILABLE;
        return new GuideRule(true, scan -> scan.museumEntries().contains(entry.id()));
    }

    private static GuideRule accessory(String ref) {
        ItemType type = ItemType.get(ref);
        ConfigurableSkyBlockItem config = ConfigurableSkyBlockItem.getFromID(ref);
        if (type == null || config == null || !config.hasComponent(AccessoryComponent.class) || config.getUnavailable() != null) {
            return GuideRule.UNAVAILABLE;
        }
        return new GuideRule(true, scan -> scan.player().getAccessoryBag().hasDiscoveredAccessory(type)
                || scan.accessories().contains(type));
    }

    private static GuideRule pet(String ref) {
        ItemType type = ItemType.get(ref);
        ConfigurableSkyBlockItem config = ConfigurableSkyBlockItem.getFromID(ref);
        if (type == null || config == null || !config.hasComponent(PetComponent.class) || config.getUnavailable() != null) {
            return GuideRule.UNAVAILABLE;
        }
        return new GuideRule(true, scan -> scan.pets().contains(type));
    }
}
