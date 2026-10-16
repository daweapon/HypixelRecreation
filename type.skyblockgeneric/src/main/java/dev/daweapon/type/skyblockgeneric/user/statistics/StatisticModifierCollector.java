package dev.daweapon.type.skyblockgeneric.user.statistics;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import java.util.*;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryOwnership;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryPower;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryStats;
import dev.daweapon.type.skyblockgeneric.accessory.OwnedAccessory;
import dev.daweapon.type.skyblockgeneric.accessory.enrichment.Enrichments;
import dev.daweapon.type.skyblockgeneric.accessory.hatcessory.Hatcessories;
import dev.daweapon.type.skyblockgeneric.accessory.power.AccessoryPowerType;
import dev.daweapon.type.skyblockgeneric.accessory.power.AccessoryPowers;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAccessoryPowers;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkills;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.elections.perks.MayorPerkStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContestProfile;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContestStatistics;
import dev.daweapon.type.skyblockgeneric.farmingcontest.FarmingContests;
import dev.daweapon.type.skyblockgeneric.hotm.HotmStatistics;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.ConstantStatisticsComponent;
import dev.daweapon.type.skyblockgeneric.item.components.SkullHeadComponent;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;
import dev.daweapon.type.skyblockgeneric.item.set.impl.TieredArmorSet;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategory;

final class StatisticModifierCollector {
    private final SkyBlockPlayer player;
    private final PlayerStatistics owner;
    private final ItemStatisticsCalculator calculator;
    private final List<TemporaryStatistic> temporaryStatistics;
    private final List<TemporaryConditionalStatistic> temporaryConditionalStatistics;

    StatisticModifierCollector(SkyBlockPlayer player, PlayerStatistics owner, ItemStatisticsCalculator calculator,
                               List<TemporaryStatistic> temporaryStatistics,
                               List<TemporaryConditionalStatistic> temporaryConditionalStatistics) {
        this.player = player;
        this.owner = owner;
        this.calculator = calculator;
        this.temporaryStatistics = temporaryStatistics;
        this.temporaryConditionalStatistics = temporaryConditionalStatistics;
    }

    List<PlayerStatistics.StatisticSource> statisticSources() {
        Map<String, List<StatisticModifier>> grouped = new java.util.LinkedHashMap<>();
        for (StatisticModifier modifier : statisticModifiers()) {
            String key = modifier.sourceType().name() + "\0" +
                (modifier.parentName() == null ? modifier.name() : modifier.parentName());
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(modifier);
        }

        List<PlayerStatistics.StatisticSource> sources = new ArrayList<>();
        for (List<StatisticModifier> modifiers : grouped.values()) {
            StatisticModifier first = modifiers.getFirst();
            ItemStatistics total = ItemStatistics.empty();
            for (StatisticModifier modifier : modifiers) total = ItemStatistics.add(total, modifier.statistics());
            String name = first.parentName() == null ? first.name() : first.parentName();
            sources.add(new PlayerStatistics.StatisticSource(name, first.material(), first.texture(), total,
                first.sourceType(), List.copyOf(modifiers)));
        }
        return sources;
    }

    List<StatisticModifier> statisticModifiers() {
        List<StatisticModifier> modifiers = new ArrayList<>();
        PlayerItemOrigin.OriginCache cache = PlayerItemOrigin.getFromCache(player.getUuid());
        addItemModifiers(modifiers, cache.get(PlayerItemOrigin.HELMET), StatisticSourceType.ARMOR);
        addItemModifiers(modifiers, cache.get(PlayerItemOrigin.CHESTPLATE), StatisticSourceType.ARMOR);
        addItemModifiers(modifiers, cache.get(PlayerItemOrigin.LEGGINGS), StatisticSourceType.ARMOR);
        addItemModifiers(modifiers, cache.get(PlayerItemOrigin.BOOTS), StatisticSourceType.ARMOR);
        SkyBlockItem worn = cache.get(PlayerItemOrigin.HELMET);
        if (worn != null && !worn.isAir()) {
            addModifier(modifiers, "Party Time!", Hatcessories.wornStatistics(player, worn), StatisticSourceType.ARMOR,
                StatisticModifierType.ABILITY_PASSIVE, worn.getDisplayName(), worn.getMaterial(), texture(worn));
        }
        if (player.getArmorSet() != null) {
            try {
                ArmorSet armorSet = player.getArmorSet().getClazz().getConstructor().newInstance();
                addModifier(modifiers, player.getArmorSet().getDisplayName() + " Set Bonus",
                    armorSet.getStatistics(player), StatisticSourceType.ARMOR,
                    StatisticModifierType.ABILITY_PASSIVE, null, Material.IRON_CHESTPLATE, null);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        addModifier(modifiers, "Tiered Set Bonuses", TieredArmorSet.statistics(player), StatisticSourceType.ARMOR,
                StatisticModifierType.ABILITY_PASSIVE, null, Material.IRON_CHESTPLATE, null);

        DatapointInventory inventory = player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class);
        for (dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem serialized : List.of(
            inventory.getValue().getNecklace(), inventory.getValue().getCloak(),
            inventory.getValue().getBelt(), inventory.getValue().getGloves())) {
            addItemModifiers(modifiers, new SkyBlockItem(serialized), StatisticSourceType.EQUIPMENT);
        }

        addItemModifiers(modifiers, owner.getMainHandItem(), StatisticSourceType.HELD_ITEM);
        addProgressionModifiers(modifiers);
        addModifier(modifiers, "Heart of the Mountain", HotmStatistics.statistics(player),
                StatisticSourceType.HEART_OF_THE_MOUNTAIN, StatisticModifierType.PROGRESSION, null, Material.EMERALD, null);
        addModifier(modifiers, "Attributes", AttributeEffectService.statistics(player),
                StatisticSourceType.ATTRIBUTE, StatisticModifierType.ATTRIBUTE, null, Material.PRISMARINE_SHARD, null);
        addTemporaryModifiers(modifiers);
        addModifier(modifiers, "Mayor Perks",
                MayorPerkStatistics.flatBonuses(ElectionPerks.current(), !HypixelConst.isIslandServer()),
                StatisticSourceType.OTHER, StatisticModifierType.BUFF, null, Material.JUKEBOX, null);

        SkyBlockItem pet = player.getPetData().getEnabledPet();
        if (pet != null) modifiers.add(new StatisticModifier(pet.getDisplayName(), pet.getMaterial(),
            texture(pet), owner.petStatistics(), StatisticSourceType.PET, StatisticModifierType.BASIC, null));

        for (OwnedAccessory accessory : AccessoryOwnership.active(player)) {
            if (!accessory.definition().getRealm().isActive()) continue;
            addItemModifiers(modifiers, accessory.item(), StatisticSourceType.ACCESSORY_BAG);
            SkyBlockItem item = accessory.item();
            ItemStatistics effects = AccessoryStats.effectStatistics(player, accessory);
            addModifier(modifiers, item.getDisplayName() + " Effect", effects, StatisticSourceType.ACCESSORY_BAG,
                StatisticModifierType.ABILITY_PASSIVE, item.getDisplayName(), item.getMaterial(), texture(item));
            addModifier(modifiers, "Enrichment", Enrichments.statistics(item), StatisticSourceType.ACCESSORY_BAG,
                StatisticModifierType.OTHER, item.getDisplayName(), item.getMaterial(), texture(item));
        }
        addThaumaturgyModifiers(modifiers);
        modifiers.add(new StatisticModifier("Base Value", Material.NETHER_STAR, null,
            ItemStatistic.getOfAllBaseValues(), StatisticSourceType.INNATE, StatisticModifierType.INNATE, null));
        return modifiers;
    }

    private void addItemModifiers(List<StatisticModifier> modifiers, SkyBlockItem item, StatisticSourceType sourceType) {
        if (item == null || item.isAir()
            || (item.hasComponent(ConstantStatisticsComponent.class)
            && sourceType != StatisticSourceType.ACCESSORY_BAG)) return;
        String parent = item.getDisplayName();
        Material material = item.getMaterial();
        String texture = texture(item);
        addModifier(modifiers, "Base Value", item.getAttributeHandler().getBaseStatistics(),
            sourceType, StatisticModifierType.BASIC, parent, material, texture);
        addModifier(modifiers, parent + " Passive", item.getAttributeHandler().getExtraDynamicStatistics(),
            sourceType, StatisticModifierType.ABILITY_PASSIVE, parent, material, texture);

        if (item.getAttributeHandler().getReforge() != null) {
            addModifier(modifiers, item.getAttributeHandler().getReforge().getName() + " Reforge",
                calculator.getReforgeStatistics(item, ItemStatistics.empty()), sourceType,
                StatisticModifierType.REFORGE, parent, material, texture);
        }
        if (item.getAttributeHandler().getReforge() != null) {
            addModifier(modifiers, item.getAttributeHandler().getReforge().getName() + " Bonus",
                ReforgeLoadout.statistics(item, player), sourceType,
                StatisticModifierType.ABILITY_PASSIVE, parent, material, texture);
        }
        addModifier(modifiers, "Gemstones", calculator.getGemstoneStatistics(item, ItemStatistics.empty()),
            sourceType, StatisticModifierType.GEMSTONE, parent, material, texture);
        for (SkyBlockEnchantment enchantment : item.getAttributeHandler().getEnchantments().toList()) {
            String name = enchantment.type().getName() + " " + StringUtility.getAsRomanNumeral(enchantment.level());
            addModifier(modifiers, name, enchantment.type().getEnch().getStatistics(enchantment.level()),
                sourceType, StatisticModifierType.ENCHANTMENT, parent, material, texture);
        }
        addModifier(modifiers, "Hot Potato Books", calculator.getHotPotatoBookStatistics(item, ItemStatistics.empty()),
            sourceType, StatisticModifierType.HOT_POTATO_BOOK, parent, material, texture);
        addModifier(modifiers, "Pest Books", dev.daweapon.type.skyblockgeneric.garden.pest.PestBooks.statistics(item),
            sourceType, StatisticModifierType.OTHER, parent, material, texture);
    }

    private void addProgressionModifiers(List<StatisticModifier> modifiers) {
        DatapointSkills.PlayerSkills skills = player.getSkills();
        ItemStatistics reconstructedSkills = ItemStatistics.empty();
        for (SkillCategories category : SkillCategories.values()) {
            ItemStatistics total = ItemStatistics.empty();
            SkillCategory skill = category.asCategory();
            int level = skills.getCurrentLevel(category);
            for (int current = 1; current <= level; current++) {
                for (SkillCategory.Reward reward : skill.getReward(current).unlocks()) {
                    if (reward instanceof SkillCategory.BaseStatisticReward base) {
                        total = total.addBase(base.getStatistic(), base.amountAdded());
                    } else if (reward instanceof SkillCategory.AdditivePercentageStatisticReward additive) {
                        total = total.addAdditive(additive.getStatistic(), additive.amountAdded());
                    }
                }
            }
            reconstructedSkills = ItemStatistics.add(reconstructedSkills, total);
            addModifier(modifiers, category + " Skill", total, StatisticSourceType.SKILL,
                StatisticModifierType.SKILL, null, skill.getDisplayIcon(), null);
        }
        addModifier(modifiers, "Skill Progression", skills.getSkillStatistics().sub(reconstructedSkills),
            StatisticSourceType.SKILL, StatisticModifierType.SKILL, null, Material.CRAFTING_TABLE, null);

        ItemStatistics levelStatistics = dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelRequirement
            .getStatisticsUpTo(player.getSkyBlockExperience().getLevel().asInt());
        addModifier(modifiers, "SkyBlock Level", levelStatistics, StatisticSourceType.SKYBLOCK_LEVELS,
            StatisticModifierType.PROGRESSION, null, Material.NETHER_STAR, null);

        ItemStatistics fairySouls = ItemStatistics.builder().withBase(ItemStatistic.HEALTH,
            (double) player.getFairySouls().getExchangedFairySouls().size() * 2D).build();
        addModifier(modifiers, "Fairy Souls", fairySouls, StatisticSourceType.FAIRY_SOULS,
            StatisticModifierType.PROGRESSION, null, Material.PLAYER_HEAD, null);

        FarmingContestProfile contests = FarmingContests.profile(player);
        addModifier(modifiers, "Anita's Extra Farming Fortune", FarmingContestStatistics.extraFortune(contests),
            StatisticSourceType.OTHER, StatisticModifierType.PROGRESSION, null, Material.WHEAT, null);
        addModifier(modifiers, "Personal Bests", FarmingContestStatistics.personalBests(contests),
            StatisticSourceType.OTHER, StatisticModifierType.PROGRESSION, null, Material.PAPER, null);
        addModifier(modifiers, "Collections", dev.daweapon.type.skyblockgeneric.collection.CollectionStatistics.all(player),
            StatisticSourceType.OTHER, StatisticModifierType.PROGRESSION, null, Material.PAINTING, null);
        addModifier(modifiers, "Garden Chips", dev.daweapon.type.skyblockgeneric.garden.chip.ChipStatistics.all(player),
            StatisticSourceType.OTHER, StatisticModifierType.PROGRESSION, null, Material.PAPER, null);
        addModifier(modifiers, "Anita's Accessory", FarmingContestStatistics.anitaAccessory(player),
            StatisticSourceType.ACCESSORY_BAG, StatisticModifierType.ABILITY_PASSIVE, null, Material.PLAYER_HEAD, null);
    }

    private void addThaumaturgyModifiers(List<StatisticModifier> modifiers) {
        DatapointAccessoryPowers.PowerData powers = AccessoryPowers.data(player);
        int accessoryPower = AccessoryPower.of(player);
        AccessoryPowerType selected = powers.getSelected();
        if (selected != null) addModifier(modifiers, selected.getDisplayName() + " Power",
                AccessoryPowers.powerStatistics(powers, accessoryPower), StatisticSourceType.THAUMATURGIST_POWER,
                StatisticModifierType.PROGRESSION, null, selected.getIcon(), null);
        addModifier(modifiers, "Stats Tuning", AccessoryPowers.tuningStatistics(powers, accessoryPower),
                StatisticSourceType.TUNING, StatisticModifierType.PROGRESSION, null, Material.COMPARATOR, null);
    }

    private void addTemporaryModifiers(List<StatisticModifier> modifiers) {
        synchronized (temporaryStatistics) {
            temporaryStatistics.removeIf(stat -> stat.getExpiration() < System.currentTimeMillis());
            for (TemporaryStatistic temporary : temporaryStatistics) {
                String name = temporary.getDisplayName() == null ? "Temporary Buff" : temporary.getDisplayName();
                addModifier(modifiers, name, temporary.getStatistics(), StatisticSourceType.TEMPORARY_BUFF,
                    StatisticModifierType.BUFF, null, Material.POTION, null);
            }
            temporaryConditionalStatistics.removeIf(temporary -> !temporary.getExpiry().apply(player));
            for (TemporaryConditionalStatistic temporary : temporaryConditionalStatistics) {
                addModifier(modifiers, "Conditional Buff", temporary.getStatistics().apply(player),
                    StatisticSourceType.TEMPORARY_BUFF, StatisticModifierType.BUFF, null, Material.POTION, null);
            }
        }
    }

    private static void addModifier(List<StatisticModifier> modifiers, String name, ItemStatistics statistics,
                                    StatisticSourceType sourceType, StatisticModifierType modifierType,
                                    String parent, Material material, String texture) {
        if (statistics.toString().isEmpty()) return;
        modifiers.add(new StatisticModifier(name, material, texture, statistics, sourceType, modifierType, parent));
    }

    private static String texture(SkyBlockItem item) {
        return item.hasComponent(SkullHeadComponent.class)
            ? item.getComponent(SkullHeadComponent.class).getSkullTexture(item) : null;
    }
}
