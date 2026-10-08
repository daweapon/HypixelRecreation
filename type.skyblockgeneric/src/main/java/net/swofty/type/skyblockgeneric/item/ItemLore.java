package net.swofty.type.skyblockgeneric.item;

import net.swofty.type.skyblockgeneric.accessory.AccessoryPower;
import net.swofty.type.skyblockgeneric.accessory.effect.AccessoryEffect;
import net.swofty.type.skyblockgeneric.accessory.enrichment.Enrichments;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.attributes.*;
import net.swofty.commons.skyblock.item.reforge.Reforge;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.commons.text.TextBody;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.essence.EssenceStars;
import net.swofty.type.skyblockgeneric.fishing.rod.FishingRodLoreBuilder;
import net.swofty.type.skyblockgeneric.gems.GemRarity;
import net.swofty.type.skyblockgeneric.gems.Gemstone;
import net.swofty.type.skyblockgeneric.item.components.*;
import net.swofty.type.skyblockgeneric.item.handlers.ability.AbilityRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.set.impl.TieredArmorSet;
import net.swofty.type.skyblockgeneric.potion.PotionEffectType;
import net.swofty.type.skyblockgeneric.text.LoreText;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemLore {
	private static final List<ItemStatistic> RENDERED_STATISTICS = List.of(
			ItemStatistic.DAMAGE, ItemStatistic.STRENGTH, ItemStatistic.CRITICAL_CHANCE, ItemStatistic.CRITICAL_DAMAGE,
			ItemStatistic.SEA_CREATURE_CHANCE, ItemStatistic.BONUS_ATTACK_SPEED, ItemStatistic.ABILITY_DAMAGE, ItemStatistic.HEALTH, ItemStatistic.DEFENSE,
			ItemStatistic.SPEED, ItemStatistic.INTELLIGENCE, ItemStatistic.MAGIC_FIND, ItemStatistic.PET_LUCK, ItemStatistic.TRUE_DEFENSE, ItemStatistic.HEALTH_REGENERATION,
			ItemStatistic.MENDING, ItemStatistic.VITALITY, ItemStatistic.FEROCITY, ItemStatistic.MINING_SPEED, ItemStatistic.MINING_FORTUNE,
			ItemStatistic.FARMING_FORTUNE, ItemStatistic.FORAGING_FORTUNE, ItemStatistic.BONUS_PEST_CHANCE, ItemStatistic.COLD_RESISTANCE, ItemStatistic.PRISTINE,
			ItemStatistic.SWING_RANGE, ItemStatistic.RIFT_DAMAGE);

	@Getter
	private ItemStack stack;

	public ItemLore(ItemStack stack) {
		this.stack = stack;
	}

	@SneakyThrows
	public void updateLore(@Nullable SkyBlockPlayer player) {
		SkyBlockItem item = new SkyBlockItem(stack);
		ItemAttributeHandler handler = item.getAttributeHandler();
		Rarity rarity = handler.getEffectiveRarity();

		Text displayName = displayName(item, stack.material());

		if (item.hasComponent(FishingRodMetadataComponent.class)) {
			FishingRodLoreBuilder.FishingRodLore rodLore = FishingRodLoreBuilder.build(item, player);
			if (rodLore != null) {
				applyFishingRodLore(item, player, rodLore);
				return;
			}
		}

		if (item.hasComponent(LoreUpdateComponent.class)) {
			LoreUpdateComponent loreUpdateComponent = item.getComponent(LoreUpdateComponent.class);
			if (loreUpdateComponent.isAbsolute()) {
				applyAbsoluteLore(item, player, loreUpdateComponent.getHandler());
				return;
			}
		}

		LoreText lore = new LoreText(item, player);
		fillUnderName(lore);
		fillBreakingPower(lore);
		fillStatistics(lore, rarity);
		fillAccessoryPower(lore);
		fillGemstones(lore);
		fillPotion(lore);
		fillEnchantments(lore);
		fillRune(lore);
		fillCustomLore(lore);
		fillConfigLore(lore);
		fillAccessoryEffects(lore);
		fillAbilities(lore);
		fillFullSetBonus(lore);
		fillRecipes(lore);
		fillReforgeable(lore);
		fillSoulbound(lore);
		fillStatsWhenShot(lore);
		fillUnfinished(lore);
		fillBazaarPlaceholder(lore);
		lore.section(LoreText.Sections.RARITY_FOOTER).line("{}", displayRarity(item, rarity));

		this.stack = ItemStacks.lines(stack.builder(), lore.render())
				.amount(item.getAmount())
				.set(DataComponents.CUSTOM_NAME, reforgedName(item, displayName).asComponent()
						.color(rarity.getColor()).decoration(TextDecoration.ITALIC, false))
				.build();
	}

	private void applyFishingRodLore(SkyBlockItem item, @Nullable SkyBlockPlayer player,
									 FishingRodLoreBuilder.FishingRodLore rodLore) {
		LoreText lore = new LoreText(item, player);
		TextBody.Section section = lore.section(LoreText.Sections.ABSOLUTE);
		rodLore.lore().forEach(line -> section.line(Text.parseLenient(line)));

		this.stack = ItemStacks.name(ItemStacks.lines(stack.builder(), lore.render()),
				Text.parseLenient(rodLore.displayName())).build();
	}

	private void applyAbsoluteLore(SkyBlockItem item, @Nullable SkyBlockPlayer player, LoreConfig loreConfig) {
		Text forcedDisplayName;
		if (item.hasComponent(CustomDisplayNameComponent.class)) {
			forcedDisplayName = item.getComponent(CustomDisplayNameComponent.class).getDisplayName(item);
		} else {
			forcedDisplayName = Text.literal(StringUtility.toNormalCase(item.getAttributeHandler().getTypeAsString()));
		}

		if (loreConfig.displayNameGenerator() != null) {
			forcedDisplayName = Text.parseLenient(loreConfig.displayNameGenerator().apply(item, player));
		}

		LoreText lore = new LoreText(item, player);
		if (loreConfig.loreGenerator() != null) {
			TextBody.Section section = lore.section(LoreText.Sections.ABSOLUTE);
			loreConfig.loreGenerator().apply(item, player).forEach(line -> section.line(LoreText.gray(line)));
		}

		this.stack = ItemStacks.name(ItemStacks.lines(stack.builder(), lore.render()),
				forcedDisplayName).build();
	}

	private static void fillUnderName(LoreText lore) {
		SkyBlockItem item = lore.item();
		TextBody.Section section = lore.section(LoreText.Sections.UNDER_NAME);

		if (item.hasComponent(ExtraUnderNameComponent.class)) {
			item.getComponent(ExtraUnderNameComponent.class).getDisplays()
					.forEach(display -> section.line(LoreText.darkGray(display)));
		}

		ItemType type = item.getAttributeHandler().getPotentialType();
		if (type != null && CollectionCategories.getCategory(type) != null) {
			section.line(Text.key("items.lore.collection_item"));
		}
	}

	private static void fillBreakingPower(LoreText lore) {
		ItemAttributeHandler handler = lore.item().getAttributeHandler();
		if (!handler.isMiningTool()) return;

		lore.section(LoreText.Sections.BREAKING_POWER)
				.line(Text.key("items.lore.breaking_power", handler.getBreakingPower()));
	}

	private static void fillStatistics(LoreText lore, Rarity rarity) {
		SkyBlockItem item = lore.item();
		ItemAttributeHandler handler = item.getAttributeHandler();
		ItemStatistics statistics = handler.getStatistics();
		TextBody.Section section = lore.section(LoreText.Sections.STATS);

		for (ItemStatistic statistic : RENDERED_STATISTICS) {
			Text line = statisticLine(item, statistic, statistics.getOverall(statistic), handler.getReforge(), rarity);
			if (line != null) section.line(line);
		}

		if (item.hasComponent(ShortBowComponent.class)) {
			section.line(Text.key("items.lore.shot_cooldown",
					item.getComponent(ShortBowComponent.class).getCooldown()));
		}
	}

	private static @Nullable Text statisticLine(SkyBlockItem item, ItemStatistic statistic, double overallValue,
												Reforge reforge, Rarity rarity) {
		if (!statistic.isRendered()) return null;

		double reforgeValue = 0;
		double gemstoneValue = Gemstone.getExtraStatisticFromGemstone(statistic, item);
		if (reforge != null) {
			reforgeValue = reforge.getAfterCalculation(ItemStatistics.empty(), rarity.ordinal() + 1).getOverall(statistic);
			overallValue += reforgeValue;
		}
		overallValue += gemstoneValue;

		double hpbValue = 0;
		ItemAttributeHotPotatoBookData.HotPotatoBookData hotPotatoBookData = item.getAttributeHandler().getHotPotatoBookData();
		if (hotPotatoBookData.hasAppliedItem()) {
			for (Map.Entry<ItemStatistic, Double> entry : hotPotatoBookData.getPotatoType().stats.entrySet()) {
				if (entry.getKey() == statistic) hpbValue += entry.getValue() * hotPotatoBookData.getTotalAmount();
			}
		}
		overallValue += hpbValue;

		if (overallValue == 0) return null;

		String name = StringUtility.toNormalCase(statistic.getDisplayName());
		TextColor colour = statistic.getLoreColor();
		Text line = statistic.getIsPercentage()
				? Text.of("<7>{}: <color:{}>{}%", name, colour, Math.round(overallValue))
				: Text.of("<7>{}: <color:{}>+{}", name, colour, Math.round(overallValue));

		long hotPotato = Math.round(hpbValue);
		long reforged = Math.round(reforgeValue);
		long gems = Math.round(gemstoneValue);
		line = line.appendIf(hpbValue != 0, "<e> ({}{})", hotPotato >= 1 ? "+" : "", hotPotato);
		line = line.appendIf(reforgeValue != 0, "<9> ({}{})", reforged > 0 ? "+" : "", reforged);
		line = line.appendIf(gemstoneValue != 0, "<d> ({}{})", gems >= 1 ? "+" : "", gems);

		return line;
	}

	private static void fillGemstones(LoreText lore) {
		SkyBlockItem item = lore.item();
		if (!item.hasComponent(GemstoneComponent.class)) return;

		GemstoneComponent gemstoneComponent = item.getComponent(GemstoneComponent.class);
		ItemAttributeGemData.GemData gemData = item.getAttributeHandler().getGemData();
		Text gemstoneLore = Text.of(" ");

		int index = -1;
		for (GemstoneComponent.GemstoneSlot entry : gemstoneComponent.getSlots()) {
			index++;
			Gemstone.Slots gemstone = entry.slot();

			if (gemData.getGem(index) == null || !gemData.getGem(index).isUnlocked()) {
				gemstoneLore = gemstoneLore.append("<8>[{}] ", gemstone.getSymbol());
				continue;
			}

			ItemType filledWith = gemData.getGem(index).filledWith;
			if (filledWith == null) {
				gemstoneLore = gemstoneLore.append("<8>[<7>{}<8>]", gemstone.getSymbol());
				continue;
			}

			GemstoneImplComponent gemstoneImplComponent = new SkyBlockItem(filledWith).getComponent(GemstoneImplComponent.class);
			GemRarity gemRarity = gemstoneImplComponent.getGemRarity();
			Gemstone gemstoneEnum = gemstoneImplComponent.getGemstone();
			Gemstone.Slots gemstoneSlot = Gemstone.Slots.getFromGemstone(gemstoneEnum);

			gemstoneLore = gemstoneLore.append("<color:{0}>[<color:{1}>{2}<color:{0}>] ",
					gemRarity.getBracketColor(), gemstoneEnum.getColor(), gemstoneSlot.getSymbol());
		}

		if (gemstoneLore.plain().isBlank()) return;
		lore.section(LoreText.Sections.GEMSTONES).line(gemstoneLore);
	}

	private static void fillPotion(LoreText lore) {
		ItemAttributePotionData.PotionData potionData = lore.item().getAttributeHandler().getPotionData();
		if (potionData == null) return;

		PotionEffectType effectType = PotionEffectType.fromName(potionData.getEffectType());
		if (effectType == null || effectType.getCategory() == null) return;

		String durationStr = "";
		if (potionData.getBaseDurationSeconds() > 0) {
			int totalSeconds = potionData.getBaseDurationSeconds();
			durationStr = " (" + totalSeconds / 60 + ":" + String.format("%02d", totalSeconds % 60) + ")";
		}

		TextBody.Section section = lore.section(LoreText.Sections.POTION);
		section.line(Text.parseLenient(effectType.getColor()
				+ effectType.getLevelDisplay(potionData.getLevel()) + durationStr));

		String description = effectType.getDescription(potionData.getLevel());
		if (!description.isEmpty()) section.line(LoreText.gray(description));
	}

	private static void fillEnchantments(LoreText lore) {
		SkyBlockItem item = lore.item();
		if (!item.hasComponent(EnchantableComponent.class)) return;
		if (!item.getComponent(EnchantableComponent.class).showEnchantLores()) return;

		List<SkyBlockEnchantment> enchantments = item.getAttributeHandler().getEnchantments()
				.sorted(java.util.Comparator.comparing((SkyBlockEnchantment enchantment) -> !enchantment.type().getEnch().isUltimate()))
				.toList();
		if (enchantments.isEmpty()) return;

		TextBody.Section section = lore.section(LoreText.Sections.ENCHANTS);
		if (enchantments.size() >= 4) {
			fillCompactEnchantments(section, enchantments);
			return;
		}

		SkyBlockPlayer viewer = lore.viewer();
		for (SkyBlockEnchantment enchantment : enchantments) {
			section.line(enchantmentTitle(enchantment));
			if (viewer == null) continue;

			Text.of("<7><wrap:34>{}</wrap>",
							Text.parseLenient(enchantment.type().getDescription(enchantment.level(), viewer)))
					.lines().forEach(section::line);
		}
	}

	private static void fillCompactEnchantments(TextBody.Section section, List<SkyBlockEnchantment> enchantments) {
		Text separator = Text.of("<7>, ");
		List<Text> current = new ArrayList<>();
		int length = 0;

		for (SkyBlockEnchantment enchantment : enchantments) {
			Text name = enchantmentTitle(enchantment);
			int nameLength = name.plain().length();

			if (!current.isEmpty() && length + 2 + nameLength > 34) {
				section.line(Text.join(separator, current));
				current = new ArrayList<>();
				length = 0;
			}

			length += current.isEmpty() ? nameLength : nameLength + 2;
			current.add(name);
		}

		if (!current.isEmpty()) section.line(Text.join(separator, current));
	}

	public static Text enchantmentTitle(SkyBlockEnchantment enchantment) {
		String format = enchantment.type().getEnch().isUltimate() ? "<d><l>{} {:roman}" : "<9>{} {:roman}";
		return Text.of(format, Text.parseLenient(enchantment.type().getName()), enchantment.level());
	}

	private static void fillRune(LoreText lore) {
		ItemAttributeRuneInfusedWith.RuneData runeData = lore.item().getAttributeHandler().getRuneData();
		if (runeData == null || !runeData.hasRune()) return;

		RuneComponent runeComponent = new SkyBlockItem(runeData.getRuneType()).getComponent(RuneComponent.class);
		lore.section(LoreText.Sections.RUNE)
				.line(Text.parseLenient(runeComponent.getDisplayName(runeData.getRuneType(), runeData.getLevel())));
	}

	private static void fillCustomLore(LoreText lore) {
		SkyBlockItem item = lore.item();
		if (!item.hasComponent(LoreUpdateComponent.class)) return;

		LoreConfig loreConfig = item.getComponent(LoreUpdateComponent.class).getHandler();
		if (loreConfig == null)
			throw new RuntimeException("Lore update handler is null for " + item.getAttributeHandler().getTypeAsString());
		if (loreConfig.loreGenerator() == null) return;

		TextBody.Section section = lore.section(loreConfig.location() == LoreConfig.LoreConfigLocation.AFTER_ABILITY
				? LoreText.Sections.CUSTOM_AFTER_ABILITY
				: LoreText.Sections.CUSTOM_BEFORE_ABILITY);
		loreConfig.loreGenerator().apply(item, lore.viewer()).forEach(line -> section.line(LoreText.gray(line)));
	}

	private static void fillAccessoryPower(LoreText lore) {
		SkyBlockItem item = lore.item();
		if (!item.hasComponent(AccessoryComponent.class)) return;
		String enrichment = Enrichments.loreLine(item);
		if (enrichment != null) lore.section(LoreText.Sections.STATS).line(Text.of(enrichment));
		int power = AccessoryPower.itemPower(item, 0, false);
		if (power <= 0) return;
		lore.section(LoreText.Sections.STATS).line(Text.of("<7>Accessory Power: <6>+{}", power));
	}

	private static void fillAccessoryEffects(LoreText lore) {
		SkyBlockItem item = lore.item();
		if (!item.hasComponent(AccessoryComponent.class)) return;
		TextBody.Section section = lore.section(LoreText.Sections.CONFIG_LORE);
		for (AccessoryEffect effect : item.getComponent(AccessoryComponent.class).getEffects()) {
			if (effect.text().isEmpty()) continue;
			if (!section.isEmpty()) section.line(Text.empty());
			effect.text().forEach(line -> section.line(LoreText.gray(line)));
			if (!effect.kind().isActive()) section.line(LoreText.darkGray("Not active yet"));
		}
	}

	private static void fillConfigLore(LoreText lore) {
		List<String> configLore = lore.item().getConfigLore();
		if (configLore == null) return;

		TextBody.Section section = lore.section(LoreText.Sections.CONFIG_LORE);
		configLore.forEach(line -> section.line(LoreText.gray(line)));
	}

	private static void fillAbilities(LoreText lore) {
		SkyBlockItem item = lore.item();
		List<RegisteredAbility> effectiveAbilities = item.getEffectiveAbilityIds().stream()
				.map(AbilityRegistry::getAbility)
				.filter(Objects::nonNull)
				.toList();
		if (effectiveAbilities.isEmpty()) return;

		TextBody.Section abilities = lore.section(LoreText.Sections.ABILITIES);
		int index = 0;
		for (RegisteredAbility ability : effectiveAbilities) {
			TextBody.Section section = abilities.child("ability_" + index++).separated();

			section.line(Text.key("items.lore.ability_label",
					Text.parseLenient(ability.getName()),
					ability.getActivation().getDisplay()));
			Text.of("<7><wrap:40>{}</wrap>",
							Text.parseLenient(ability.getDescription().apply(lore.viewer(), item)))
					.lines().forEach(section::line);

			String costDisplay = ability.getCost().getLoreDisplay();
			if (costDisplay != null) section.line(Text.parseLenient(costDisplay));

			if (ability.getCooldownTicks() > 20) {
				section.line("<key:'items.lore.ability_cooldown':'{:.1}'>",
						(double) ability.getCooldownTicks() / 20);
			}
		}
	}

	@SneakyThrows
	private static void fillFullSetBonus(LoreText lore) {
		SkyBlockItem item = lore.item();
		ArmorSetRegistry registry = ArmorSetRegistry.getArmorSet(item.getAttributeHandler().getPotentialType());
		if (registry == null || registry.getClazz() == null) return;

		ArmorSet armorSet = registry.getClazz().getDeclaredConstructor().newInstance();
		SkyBlockPlayer viewer = lore.viewer();

		int wearingAmount = 0;
		if (viewer != null && viewer.isWearingItem(item)) {
			for (SkyBlockItem armorItem : viewer.getArmor()) {
				if (armorItem == null) continue;
				ArmorSetRegistry armorSetRegistry = ArmorSetRegistry.getArmorSet(armorItem.getAttributeHandler().getPotentialType());
				if (armorSetRegistry == null) continue;
				if (armorSetRegistry.getClazz() == armorSet.getClass()) {
					wearingAmount++;
				}
			}
		}

		ArmorSetRegistry setRegistry = ArmorSetRegistry.getArmorSet(armorSet.getClass());
		int totalPieces = setRegistry == null ? 4 : ArmorSetRegistry.getPieceCount(setRegistry);

		TextBody.Section section = lore.section(LoreText.Sections.FULL_SET_BONUS);
		if (armorSet instanceof TieredArmorSet tiered) {
			String key = wearingAmount >= tiered.minimumPieces() ? "items.lore.tiered_bonus_active" : "items.lore.tiered_bonus";
			section.line(Text.key(key, Text.parseLenient(armorSet.getName()), wearingAmount, totalPieces));
			tiered.getDescription(Math.max(wearingAmount, tiered.minimumPieces())).forEach(line -> section.line(LoreText.gray(line)));
			return;
		}
		section.line(Text.key("items.lore.full_set_bonus", Text.parseLenient(armorSet.getName()), wearingAmount, totalPieces));
		armorSet.getDescription().forEach(line -> section.line(LoreText.gray(line)));
	}

	private static void fillRecipes(LoreText lore) {
		if (!lore.item().hasComponent(RightClickRecipeComponent.class)) return;

		lore.section(LoreText.Sections.RECIPES).line(Text.key("items.lore.right_click_recipes"));
	}

	private static void fillReforgeable(LoreText lore) {
		if (!lore.item().hasComponent(ReforgableComponent.class)) return;

		lore.section(LoreText.Sections.REFORGEABLE).line(Text.key("items.lore.reforgeable"));
	}

	private static void fillSoulbound(LoreText lore) {
		ItemAttributeSoulbound.SoulBoundData bound = lore.item().getAttributeHandler().getSoulBoundData();
		if (bound == null) return;

		lore.section(LoreText.Sections.SOULBOUND).line(Text.key("items.lore.soulbound", bound.isCoopAllowed()
				? Text.key("items.lore.soulbound_coop_prefix")
				: Text.empty()));
	}

	private static void fillStatsWhenShot(LoreText lore) {
		if (!lore.item().hasComponent(ArrowComponent.class)) return;

		lore.section(LoreText.Sections.STATS_WHEN_SHOT).line(Text.key("items.lore.stats_when_shot"));
	}

	private static void fillUnfinished(LoreText lore) {
		if (!lore.item().hasComponent(NotFinishedYetComponent.class)) return;

		lore.section(LoreText.Sections.UNFINISHED)
				.line(Text.key("items.lore.not_finished"))
				.line(Text.empty());
	}

	// Bazaar-catalog parity stub: item was added purely to match real Hypixel's bazaar listing,
	// with no real drop source/recipe/use implemented yet.
	private static void fillBazaarPlaceholder(LoreText lore) {
		if (!lore.item().hasComponent(BazaarPlaceholderComponent.class)) return;

		lore.section(LoreText.Sections.BAZAAR_PLACEHOLDER)
				.line(Text.of("<c><l>ITEM NOT ADDED YET JUST FOR BAZAAR PLACEHOLDER"))
				.line(Text.empty());
	}

	/** The item's coloured, reforged name, built without rendering its lore. */
	public static String nameMarkup(SkyBlockItem item) {
		Rarity rarity = item.getAttributeHandler().getEffectiveRarity();
		return Text.of("<color:{0}>{1}", rarity.getColor(),
				reforgedName(item, displayName(item, item.getMaterial()))).serialize();
	}

	private static Text displayName(SkyBlockItem item, Material material) {
		if (item.hasComponent(CustomDisplayNameComponent.class)) {
			return item.getComponent(CustomDisplayNameComponent.class).getDisplayName(item);
		}

		ItemAttributePotionData.PotionData potionData = item.getAttributeHandler().getPotionData();
		PotionEffectType effect = potionData != null && potionData.getEffectType() != null
				&& !potionData.getEffectType().equals("WATER")
				? PotionEffectType.fromName(potionData.getEffectType())
				: null;
		if (effect == null) {
			return getConfiguredOrMaterialName(item, material);
		}

		Text effectDisplay = Text.parseLenient(effect.getLevelDisplay(potionData.getLevel()));
		return potionData.isSplash()
				? Text.of("{} <key:items.lore.splash_potion_suffix>", effectDisplay)
				: Text.of("{} <key:items.lore.potion_suffix>", effectDisplay);
	}

	private static Text getConfiguredOrMaterialName(SkyBlockItem item, Material material) {
		ConfigurableSkyBlockItem config = item.getConfig();
		return Text.parseLenient(fallbackDisplayName(
				config == null ? null : config.getId(),
				config == null ? null : config.getName(),
				material));
	}

	static String fallbackDisplayName(@Nullable String itemId, @Nullable String configuredName, Material material) {
		if (configuredName != null) return configuredName;
		if (itemId != null && !itemId.isBlank()) return StringUtility.toNormalCase(itemId);
		// Component.translatable(material) only resolves client-side; any server code that
		// flattens the name to plain text (e.g. SkyBlockItem.getDisplayName(), used by the bazaar
		// GUI and chat messages) would otherwise print the raw key like "item.minecraft.wheat".
		return materialDisplayName(material);
	}

	static String materialDisplayName(Material material) {
		return StringUtility.toNormalCase(material.key().value());
	}

	private static Text reforgedName(SkyBlockItem item, Text displayName) {
		Reforge reforge = item.getAttributeHandler().getReforge();
		Text named = !item.hasComponent(ReforgableComponent.class) || reforge == null
				? displayName
				: Text.of("{} ", Text.parseLenient(reforge.getPrefix())).append(displayName);
		int stars = item.getAttributeHandler().getUpgradeLevel();
		return stars > 0 ? named.append(" " + EssenceStars.display(stars)) : named;
	}

	private static Text displayRarity(SkyBlockItem item, Rarity rarity) {
		Component display = rarity.getDisplay();

		if (item.hasComponent(ExtraRarityComponent.class)) {
			display = display.appendSpace().append(Text.parseLenient(
					item.getComponent(ExtraRarityComponent.class).getExtraRarityDisplay(item)).asComponent());
		}

		Text displayRarity = Text.of("{}", display);

		if (item.getAttributeHandler().isRecombobulated()) {
			displayRarity = Text.of("<color:{0}><k>L</k> {1} <k>L</k>", rarity.getColor(), displayRarity);
		}

		return displayRarity;
	}
}
