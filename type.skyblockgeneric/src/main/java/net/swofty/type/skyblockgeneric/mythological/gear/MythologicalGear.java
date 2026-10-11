package net.swofty.type.skyblockgeneric.mythological.gear;

import net.minestom.server.entity.LivingEntity;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointInventory;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.enchantment.impl.EnchantmentChimera;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.mythological.GriffinPet;
import net.swofty.type.skyblockgeneric.mythological.MythologicalBestiary;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.type.skyblockgeneric.mythological.burrow.GriffinBurrowService;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockInventory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Everything the Mythological Ritual gear does against Mythological mobs: Challenger's and
 * Mythos tiered bonuses, Mythos' Might, the Crowns, the Golden Fleece, the Sword of Revelations,
 * the Daedalus Blade and the Mythological attribute shards.
 */
public final class MythologicalGear {
    public static final Set<ItemType> CHALLENGER = Set.of(
            ItemType.CHALLENGER_HELMET, ItemType.CHALLENGER_CHESTPLATE, ItemType.CHALLENGER_LEGGINGS,
            ItemType.CHALLENGER_BOOTS, ItemType.CHALLENGER_NECKLACE, ItemType.CHALLENGER_CLOAK,
            ItemType.CHALLENGER_BELT, ItemType.CHALLENGER_BRACELET);
    public static final Set<ItemType> MYTHOS_ARMOR = Set.of(
            ItemType.MYTHOS_HELMET, ItemType.MYTHOS_CHESTPLATE, ItemType.MYTHOS_LEGGINGS, ItemType.MYTHOS_BOOTS);
    public static final Set<ItemType> MYTHOS_EQUIPMENT = Set.of(
            ItemType.MYTHOS_NECKLACE, ItemType.MYTHOS_CLOAK, ItemType.MYTHOS_BELT, ItemType.MYTHOS_BRACELET);

    private static final double SWORD_OF_REVELATIONS_DAMAGE = 1.5;
    private static final double SWORD_OF_REVELATIONS_MAGIC_FIND = 5;
    private static final double DAEDALUS_DAMAGE_PERCENT_PER_TIER = 1.5;
    private static final double DAEDALUS_MAGIC_FIND_PER_TIER = 0.3;
    private static final double DAEDALUS_DAMAGE_PER_TAMING_LEVEL = 5;
    private static final double CROWN_OF_GREED_MAGIC_FIND = 10;
    private static final double CROWN_OF_GREED_DAMAGE_TAKEN = 1.25;
    private static final double CROWN_OF_AVARICE_DAMAGE_TAKEN = 1.5;
    private static final double GOLDEN_FLEECE_MAGIC_FIND = 5;
    private static final double GOLDEN_FLEECE_TRACKING = 15;
    private static final double MANTICORE_CLAW_COPY = 0.10;

    private MythologicalGear() {
    }

    public static boolean isMythosMight(ItemType type) {
        return CHALLENGER.contains(type) || MYTHOS_ARMOR.contains(type) || MYTHOS_EQUIPMENT.contains(type);
    }

    /**
     * Stats an item grants on top of its own: Mythos' Might doubles Challenger's and Mythos gear in
     * the Hub during the Ritual, the Manticore Claw and Daedalus Blade copy the active pet.
     */
    public static ItemStatistics extraItemStatistics(SkyBlockItem item, @Nullable SkyBlockPlayer wearer) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        if (type == null) return ItemStatistics.empty();
        if (isMythosMight(type)) {
            return GriffinBurrowService.isRitualActive() ? item.getAttributeHandler().getStatistics() : ItemStatistics.empty();
        }
        if (wearer == null) return ItemStatistics.empty();
        SkyBlockItem pet = wearer.getPetData().getEnabledPet();
        SkyBlockEnchantment chimera = item.getAttributeHandler().getEnchantment(EnchantmentType.CHIMERA);
        ItemStatistics copied = chimera == null || pet == null ? ItemStatistics.empty()
                : ItemStatistics.multiply(PetItems.statistics(pet), chimera.level() * EnchantmentChimera.COPY_PER_LEVEL);
        return ItemStatistics.add(copied, petCopy(type, pet, wearer));
    }

    private static ItemStatistics petCopy(ItemType type, @Nullable SkyBlockItem pet, SkyBlockPlayer wearer) {
        if (type == ItemType.MANTICORE_CLAW) {
            return pet == null ? ItemStatistics.empty()
                    : PetItems.combatStatistics(PetItems.statistics(pet), MANTICORE_CLAW_COPY);
        }
        if (type == ItemType.DAEDALUS_AXE) {
            ItemStatistics taming = ItemStatistics.builder().withBase(ItemStatistic.DAMAGE,
                    DAEDALUS_DAMAGE_PER_TAMING_LEVEL * wearer.getSkills().getCurrentLevel(SkillCategories.TAMING)).build();
            return pet == null ? taming : ItemStatistics.add(taming, PetItems.combatStatistics(PetItems.statistics(pet), 1));
        }
        return ItemStatistics.empty();
    }

    public static ItemStatistics against(SkyBlockPlayer player, @Nullable LivingEntity enemy) {
        if (!(enemy instanceof MythologicalCreatureMob) || player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        double magicFind = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("R63"));
        magicFind += GriffinPet.kingOfKingsMagicFind(player);

        ItemType helmet = type(new SkyBlockItem(player.getHelmet()));
        if (helmet == ItemType.CROWN_OF_GREED) magicFind += CROWN_OF_GREED_MAGIC_FIND;
        if (helmet == ItemType.CROWN_OF_AVARICE) magicFind += CrownOfAvarice.magicFind(new SkyBlockItem(player.getHelmet()));

        if (equipment(player).stream().anyMatch(item -> type(item) == ItemType.GOLDEN_FLEECE)) {
            magicFind += GOLDEN_FLEECE_MAGIC_FIND;
        }
        magicFind += familiarityMagicFind(player);

        ItemType weapon = type(new SkyBlockItem(player.getItemInMainHand()));
        if (weapon == ItemType.SWORD_OF_REVELATIONS) magicFind += SWORD_OF_REVELATIONS_MAGIC_FIND;
        if (weapon == ItemType.DAEDALUS_AXE) magicFind += DAEDALUS_MAGIC_FIND_PER_TIER * MythologicalBestiary.totalTiers(player);

        if (magicFind == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.MAGIC_FIND, magicFind).build();
    }

    public static double outgoingDamageMultiplier(SkyBlockPlayer player) {
        double multiplier = 1 + AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("U60")) / 100D;
        ItemType weapon = type(new SkyBlockItem(player.getItemInMainHand()));
        if (weapon == ItemType.SWORD_OF_REVELATIONS) multiplier *= SWORD_OF_REVELATIONS_DAMAGE;
        if (weapon == ItemType.DAEDALUS_AXE) {
            multiplier *= 1 + DAEDALUS_DAMAGE_PERCENT_PER_TIER * MythologicalBestiary.totalTiers(player) / 100D;
        }
        SkyBlockItem helmet = new SkyBlockItem(player.getHelmet());
        if (type(helmet) == ItemType.CROWN_OF_AVARICE) multiplier *= CrownOfAvarice.damageMultiplier(helmet);
        return multiplier;
    }

    public static double incomingDamageMultiplier(SkyBlockPlayer player) {
        double multiplier = 1 - unearthedReduction(unearthedPieces(player)) / 100D;
        ItemType helmet = type(new SkyBlockItem(player.getHelmet()));
        if (helmet == ItemType.CROWN_OF_GREED) multiplier *= CROWN_OF_GREED_DAMAGE_TAKEN;
        if (helmet == ItemType.CROWN_OF_AVARICE) multiplier *= CROWN_OF_AVARICE_DAMAGE_TAKEN;
        return multiplier;
    }

    public static double burrowTracking(SkyBlockPlayer player) {
        double tracking = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("E39"));
        if (equipment(player).stream().anyMatch(item -> type(item) == ItemType.GOLDEN_FLEECE)) {
            tracking += GOLDEN_FLEECE_TRACKING;
        }
        return tracking;
    }

    public static double mobCoinMultiplier(SkyBlockPlayer player) {
        return 1 + greedPercent(greedPieces(player)) / 100D;
    }

    public static int unearthedPieces(SkyBlockPlayer player) {
        return (int) worn(player).stream().map(MythologicalGear::type).filter(Objects::nonNull)
                .filter(type -> CHALLENGER.contains(type) || MYTHOS_ARMOR.contains(type) || MYTHOS_EQUIPMENT.contains(type))
                .count();
    }

    public static int familiarityPieces(SkyBlockPlayer player) {
        return (int) armor(player).stream().map(MythologicalGear::type).filter(Objects::nonNull).filter(MYTHOS_ARMOR::contains).count();
    }

    public static int greedPieces(SkyBlockPlayer player) {
        return (int) equipment(player).stream().map(MythologicalGear::type).filter(Objects::nonNull).filter(MYTHOS_EQUIPMENT::contains).count();
    }

    public static double unearthedReduction(int pieces) {
        return pieces < 2 ? 0 : 10 + 2.5 * (pieces - 2);
    }

    public static double familiarityMagicFind(int pieces) {
        return pieces < 2 ? 0 : 10 + 5 * (pieces - 2);
    }

    public static double greedPercent(int pieces) {
        return pieces < 2 ? 0 : 10 + 5 * (pieces - 2);
    }

    private static double familiarityMagicFind(SkyBlockPlayer player) {
        return familiarityMagicFind(familiarityPieces(player));
    }

    private static List<SkyBlockItem> worn(SkyBlockPlayer player) {
        List<SkyBlockItem> worn = new ArrayList<>(armor(player));
        worn.addAll(equipment(player));
        return worn;
    }

    private static List<SkyBlockItem> armor(SkyBlockPlayer player) {
        return List.of(new SkyBlockItem(player.getHelmet()), new SkyBlockItem(player.getChestplate()),
                new SkyBlockItem(player.getLeggings()), new SkyBlockItem(player.getBoots()));
    }

    private static List<SkyBlockItem> equipment(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return List.of();
        SkyBlockInventory inventory = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue();
        List<SkyBlockItem> equipment = new ArrayList<>();
        for (var serialized : List.of(inventory.getNecklace(), inventory.getCloak(), inventory.getBelt(), inventory.getGloves())) {
            if (serialized != null) equipment.add(new SkyBlockItem(serialized));
        }
        return equipment;
    }

    private static @Nullable ItemType type(SkyBlockItem item) {
        return item.isNA() || item.isAir() ? null : item.getAttributeHandler().getPotentialType();
    }
}
