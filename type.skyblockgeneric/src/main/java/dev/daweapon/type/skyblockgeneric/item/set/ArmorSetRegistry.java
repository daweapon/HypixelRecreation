package dev.daweapon.type.skyblockgeneric.item.set;

import lombok.Getter;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;
import dev.daweapon.type.skyblockgeneric.item.set.sets.*;
import dev.daweapon.type.skyblockgeneric.item.set.sets.terror.HydraStrikeArmorSet;
import org.jetbrains.annotations.Nullable;

@Getter
public enum ArmorSetRegistry {
	LEAFLET(LeafletSet.class, ItemType.LEAFLET_BOOTS, ItemType.LEAFLET_LEGGINGS, ItemType.LEAFLET_CHESTPLATE, ItemType.LEAFLET_HELMET),
	MINER_OUTFIT(MinerOutfitSet.class, ItemType.MINERS_OUTFIT_BOOTS, ItemType.MINERS_OUTFIT_LEGGINGS, ItemType.MINERS_OUTFIT_CHESTPLATE, ItemType.MINERS_OUTFIT_HELMET),
	CHEAP_TUXEDO(CheapTuxedoSet.class, ItemType.CHEAP_TUXEDO_BOOTS, ItemType.CHEAP_TUXEDO_LEGGINGS, ItemType.CHEAP_TUXEDO_CHESTPLATE, null),
	FANCY_TUXEDO(FancyTuxedoSet.class, ItemType.FANCY_TUXEDO_BOOTS, ItemType.FANCY_TUXEDO_LEGGINGS, ItemType.FANCY_TUXEDO_CHESTPLATE, null),
	ELEGANT_TUXEDO(ElegantTuxedoSet.class, ItemType.ELEGANT_TUXEDO_BOOTS, ItemType.ELEGANT_TUXEDO_LEGGINGS, ItemType.ELEGANT_TUXEDO_CHESTPLATE, null),
	MUSHROOM(MushroomSet.class, ItemType.MUSHROOM_BOOTS, ItemType.MUSHROOM_LEGGINGS, ItemType.MUSHROOM_CHESTPLATE, ItemType.MUSHROOM_HELMET),
	LAPIS(LapisArmorSet.class, ItemType.LAPIS_ARMOR_BOOTS, ItemType.LAPIS_ARMOR_LEGGINGS, ItemType.LAPIS_ARMOR_CHESTPLATE, ItemType.LAPIS_ARMOR_HELMET),
    MINER(MinerArmorSet.class, ItemType.MINER_ARMOR_BOOTS, ItemType.MINER_ARMOR_LEGGINGS, ItemType.MINER_ARMOR_CHESTPLATE, ItemType.MINER_ARMOR_HELMET),
	PROSPECTING(ProspectingSet.class, ItemType.MINER_OUTFIT_BOOTS, ItemType.MINER_OUTFIT_LEGGINGS, ItemType.MINER_OUTFIT_CHESTPLATE, ItemType.MINER_OUTFIT_HELMET),
	BLAZE(BlazeArmorSet.class, ItemType.BLAZE_BOOTS, ItemType.BLAZE_LEGGINGS, ItemType.BLAZE_CHESTPLATE, ItemType.BLAZE_HELMET),
	BERSERKER(BerserkerArmorSet.class, ItemType.BERSERKER_BOOTS, ItemType.BERSERKER_LEGGINGS, ItemType.BERSERKER_CHESTPLATE, ItemType.BERSERKER_HELMET),
	SHIMMERING_LIGHT(ShimmeringLightSet.class, ItemType.SHIMMERING_LIGHT_SLIPPERS, ItemType.SHIMMERING_LIGHT_TROUSERS, ItemType.SHIMMERING_LIGHT_TUNIC, ItemType.SHIMMERING_LIGHT_HOOD),
	REKINDLED_EMBER(RekindledEmberSet.class, ItemType.REKINDLED_EMBER_BOOTS, ItemType.REKINDLED_EMBER_LEGGINGS, ItemType.REKINDLED_EMBER_CHESTPLATE, ItemType.REKINDLED_EMBER_HELMET),
	PARK_ARMOR(null, ItemType.MELODY_SHOES, ItemType.CHARLIE_TROUSERS, ItemType.KELLY_TSHIRT, ItemType.MOLE_HAT),
	TERROR(HydraStrikeArmorSet.Basic.class, ItemType.TERROR_BOOTS, ItemType.TERROR_LEGGINGS, ItemType.TERROR_CHESTPLATE, ItemType.TERROR_HELMET),
	HOT_TERROR(HydraStrikeArmorSet.Hot.class, ItemType.HOT_TERROR_BOOTS, ItemType.HOT_TERROR_LEGGINGS, ItemType.HOT_TERROR_CHESTPLATE, ItemType.HOT_TERROR_HELMET),
	BURNING_TERROR(HydraStrikeArmorSet.Burning.class, ItemType.BURNING_TERROR_BOOTS, ItemType.BURNING_TERROR_LEGGINGS, ItemType.BURNING_TERROR_CHESTPLATE, ItemType.BURNING_TERROR_HELMET),
	FIERY_TERROR(HydraStrikeArmorSet.Fiery.class, ItemType.FIERY_TERROR_BOOTS, ItemType.FIERY_TERROR_LEGGINGS, ItemType.FIERY_TERROR_CHESTPLATE, ItemType.FIERY_TERROR_HELMET),
	INFERNAL_TERROR(HydraStrikeArmorSet.Infernal.class, ItemType.INFERNAL_TERROR_BOOTS, ItemType.INFERNAL_TERROR_LEGGINGS, ItemType.INFERNAL_TERROR_CHESTPLATE, ItemType.INFERNAL_TERROR_HELMET),
	SPEEDSTER(SpeedsterSet.class, ItemType.SPEEDSTER_BOOTS, ItemType.SPEEDSTER_LEGGINGS, ItemType.SPEEDSTER_CHESTPLATE, ItemType.SPEEDSTER_HELMET),
	SUPERIOR_DRAGON(SuperiorDragonSet.class, ItemType.SUPERIOR_DRAGON_BOOTS, ItemType.SUPERIOR_DRAGON_LEGGINGS, ItemType.SUPERIOR_DRAGON_CHESTPLATE, ItemType.SUPERIOR_DRAGON_HELMET),
	YOUNG_DRAGON(YoungDragonSet.class, ItemType.YOUNG_DRAGON_BOOTS, ItemType.YOUNG_DRAGON_LEGGINGS, ItemType.YOUNG_DRAGON_CHESTPLATE, ItemType.YOUNG_DRAGON_HELMET),
	PROTECTOR_DRAGON(ProtectorDragonSet.class, ItemType.PROTECTOR_DRAGON_BOOTS, ItemType.PROTECTOR_DRAGON_LEGGINGS, ItemType.PROTECTOR_DRAGON_CHESTPLATE, ItemType.PROTECTOR_DRAGON_HELMET),
	GLACITE(GlaciteArmorSet.class, ItemType.GLACITE_BOOTS, ItemType.GLACITE_LEGGINGS, ItemType.GLACITE_CHESTPLATE, ItemType.GLACITE_HELMET),
	WISE_DRAGON(WiseDragonArmorSet.class, ItemType.WISE_DRAGON_BOOTS, ItemType.WISE_DRAGON_LEGGINGS, ItemType.WISE_DRAGON_CHESTPLATE, ItemType.WISE_DRAGON_HELMET),
	GOLEM_ARMOR(GolemArmorSet.class, ItemType.GOLEM_ARMOR_BOOTS, ItemType.GOLEM_ARMOR_LEGGINGS, ItemType.GOLEM_ARMOR_CHESTPLATE, ItemType.GOLEM_ARMOR_HELMET),
	EMERALD(EmeraldArmorSet.class, ItemType.EMERALD_ARMOR_BOOTS, ItemType.EMERALD_ARMOR_LEGGINGS, ItemType.EMERALD_ARMOR_CHESTPLATE, ItemType.EMERALD_ARMOR_HELMET),
	CACTUS(CactusArmorSet.class, ItemType.CACTUS_BOOTS, ItemType.CACTUS_LEGGINGS, ItemType.CACTUS_CHESTPLATE, ItemType.CACTUS_HELMET),
	RABBIT(RabbitArmorSet.class, ItemType.RABBIT_BOOTS, ItemType.RABBIT_LEGGINGS, ItemType.RABBIT_CHESTPLATE, ItemType.RABBIT_HELMET),
	FAIRY_OUTFIT(FairyOutfitSet.class, ItemType.FAIRY_BOOTS, ItemType.FAIRY_LEGGINGS, ItemType.FAIRY_CHESTPLATE, ItemType.FAIRY_HELMET),
	SPONGE(SpongeSet.class, ItemType.SPONGE_BOOTS, ItemType.SPONGE_LEGGINGS, ItemType.SPONGE_CHESTPLATE, ItemType.SPONGE_HELMET),
	SHARK_SCALE(SharkScaleSet.class, ItemType.SHARK_SCALE_BOOTS, ItemType.SHARK_SCALE_LEGGINGS, ItemType.SHARK_SCALE_CHESTPLATE, ItemType.SHARK_SCALE_HELMET),
	HOLY_DRAGON(HolyBloodSet.class, ItemType.HOLY_DRAGON_BOOTS, ItemType.HOLY_DRAGON_LEGGINGS, ItemType.HOLY_DRAGON_CHESTPLATE, ItemType.HOLY_DRAGON_HELMET),
	STRONG_DRAGON(StrongDragonSet.class, ItemType.STRONG_DRAGON_BOOTS, ItemType.STRONG_DRAGON_LEGGINGS, ItemType.STRONG_DRAGON_CHESTPLATE, ItemType.STRONG_DRAGON_HELMET),
	ANGLER(AnglerArmorSet.class, ItemType.ANGLER_BOOTS, ItemType.ANGLER_LEGGINGS, ItemType.ANGLER_CHESTPLATE, ItemType.ANGLER_HELMET),
	BACKWATER(BackwaterArmorSet.class, ItemType.BACKWATER_BOOTS, ItemType.BACKWATER_LEGGINGS, ItemType.BACKWATER_CHESTPLATE, ItemType.BACKWATER_HELMET),
	SNORKELING(SnorkelingArmorSet.class, ItemType.SNORKELING_BOOTS, ItemType.SNORKELING_LEGGINGS, ItemType.SNORKELING_CHESTPLATE, ItemType.SNORKELING_HELMET),
	RESISTANCE(dev.daweapon.type.skyblockgeneric.dante.ResistanceArmorSet.class, ItemType.ARMOR_OF_THE_RESISTANCE_BOOTS, ItemType.ARMOR_OF_THE_RESISTANCE_LEGGINGS, ItemType.ARMOR_OF_THE_RESISTANCE_CHESTPLATE, ItemType.ARMOR_OF_THE_RESISTANCE_HELMET),
	GENERALS_RESISTANCE(dev.daweapon.type.skyblockgeneric.dante.GeneralsResistanceArmorSet.class, ItemType.GENERALS_ARMOR_OF_THE_RESISTANCE_BOOTS, ItemType.GENERALS_ARMOR_OF_THE_RESISTANCE_LEGGINGS, ItemType.GENERALS_ARMOR_OF_THE_RESISTANCE_CHESTPLATE, ItemType.GENERALS_ARMOR_OF_THE_RESISTANCE_HELMET),
	;

	private final Class<? extends ArmorSet> clazz;
	private final ItemType boots;
	private final ItemType leggings;
	private final ItemType chestplate;
	private final ItemType helmet;

	ArmorSetRegistry(Class<? extends ArmorSet> clazz, ItemType boots, ItemType legging,
					 ItemType chestplate, ItemType helmet) {
		this.clazz = clazz;
		this.boots = boots;
		this.leggings = legging;
		this.chestplate = chestplate;
		this.helmet = helmet;
	}

	public static @Nullable ArmorSetRegistry getArmorSet(Class<? extends ArmorSet> clazz) {
		for (ArmorSetRegistry armorSetRegistry : values()) {
			if (armorSetRegistry.getClazz() == clazz) {
				return armorSetRegistry;
			}
		}
		return null;
	}

	public static @Nullable ArmorSetRegistry getArmorSet(@Nullable ItemType item) {
		if (item == null) return null;
		for (ArmorSetRegistry armorSetRegistry : values()) {
			if (armorSetRegistry.getBoots() == item
					|| armorSetRegistry.getLeggings() == item
					|| armorSetRegistry.getChestplate() == item
					|| armorSetRegistry.getHelmet() == item) {
				return armorSetRegistry;
			}
		}
		return null;
	}

	public static int getPieceCount(ArmorSetRegistry armorSetRegistry) {
		int count = 0;
		if (armorSetRegistry.getBoots() != null) count++;
		if (armorSetRegistry.getLeggings() != null) count++;
		if (armorSetRegistry.getChestplate() != null) count++;
		if (armorSetRegistry.getHelmet() != null) count++;
		return count;
	}

	public static ArmorSetRegistry getArmorSet(ItemType boots, ItemType leggings, ItemType chestplate, ItemType helmet) {
		for (ArmorSetRegistry armorSetRegistry : values()) {
			if ((armorSetRegistry.getBoots() == null || armorSetRegistry.getBoots() == boots)
					&& (armorSetRegistry.getLeggings() == null || armorSetRegistry.getLeggings() == leggings)
					&& (armorSetRegistry.getChestplate() == null || armorSetRegistry.getChestplate() == chestplate)
					&& (armorSetRegistry.getHelmet() == null || armorSetRegistry.getHelmet() == helmet)) {
				return armorSetRegistry;
			}
		}
		return null;
	}

    public String getDisplayName() {
        return StringUtility.toNormalCase(name().replace("_", " "));
    }

    public int getWornPieceCount(ItemType boots, ItemType leggings, ItemType chestplate, ItemType helmet) {
        int count = 0;
        if (this.boots != null && this.boots == boots) count++;
        if (this.leggings != null && this.leggings == leggings) count++;
        if (this.chestplate != null && this.chestplate == chestplate) count++;
        if (this.helmet != null && this.helmet == helmet) count++;
        return count;
    }
}
