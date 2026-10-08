package net.swofty.type.skyblockgeneric.mythological.gear;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import net.swofty.type.skyblockgeneric.mythological.MythologicalBestiary;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Item behaviour the Mythological Ritual adds that lives outside the gear stats: eating Brain Food,
 * the Mythological Dye and Fateful Stinger in the Anvil, and the live lore on Challenger's/Mythos
 * gear, the Crown of Avarice and the Daedalus Blade.
 */
public final class MythologicalItemHandlers {
    public static final int BRAIN_FOOD_LIMIT = 5;
    private static final String MYTHOLOGICAL_DYE_COLOR = "#6F6F0C";
    private static final int FATEFUL_STINGER_FROM = 6;
    private static final int FATEFUL_STINGER_TO = 7;
    private static final Set<Material> LEATHER_ARMOR = Set.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);

    private MythologicalItemHandlers() {
    }

    public static void registerLore() {
        LoreRegistry.register("MYTHOLOGICAL_GEAR", new LoreConfig(MythologicalItemHandlers::gearLore, null));
        LoreRegistry.register("CROWN_OF_AVARICE", new LoreConfig(MythologicalItemHandlers::crownOfAvariceLore, null));
        LoreRegistry.register("DAEDALUS_BLADE", new LoreConfig(MythologicalItemHandlers::daedalusLore, null));
    }

    public static void registerInteractables() {
        InteractableRegistry.register("BRAIN_FOOD", InteractableItemConfig.builder()
                .rightClickHandler(MythologicalItemHandlers::eatBrainFood).build());
    }

    public static void registerAnvilCombines() {
        AnvilCombineRegistry.register("DYE_MYTHOLOGICAL", new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> upgradeItem.getAttributeHandler().setDyeColor(MYTHOLOGICAL_DYE_COLOR),
                (player, upgradeItem, sacrificeItem) -> LEATHER_ARMOR.contains(upgradeItem.getMaterial())
                        && !MYTHOLOGICAL_DYE_COLOR.equals(upgradeItem.getAttributeHandler().getDyeColor()),
                (upgradeItem, sacrificeItem, player) -> 0,
                null));
        AnvilCombineRegistry.register("FATEFUL_STINGER", new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> upgradeItem.getAttributeHandler()
                        .addEnchantment(new SkyBlockEnchantment(EnchantmentType.VENOMOUS, FATEFUL_STINGER_TO)),
                (player, upgradeItem, sacrificeItem) -> {
                    SkyBlockEnchantment venomous = upgradeItem.getAttributeHandler().getEnchantment(EnchantmentType.VENOMOUS);
                    return venomous != null && venomous.level() == FATEFUL_STINGER_FROM;
                },
                (upgradeItem, sacrificeItem, player) -> 0,
                null));
    }

    public static int brainFoodEaten(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return 0;
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BRAIN_FOOD_CONSUMED, DatapointInteger.class).getValue();
    }

    public static void setBrainFoodEaten(SkyBlockPlayer player, int eaten) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BRAIN_FOOD_CONSUMED, DatapointInteger.class).setValue(eaten);
    }

    public static ItemStatistics permanentStatistics(SkyBlockPlayer player) {
        int eaten = brainFoodEaten(player);
        if (eaten == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.TRACKING, (double) eaten).build();
    }

    private static void eatBrainFood(SkyBlockPlayer player, SkyBlockItem item) {
        int eaten = brainFoodEaten(player);
        if (eaten >= BRAIN_FOOD_LIMIT) {
            player.sendMessage("<c>You have already consumed the maximum amount of Brain Food!");
            return;
        }
        setBrainFoodEaten(player, eaten + 1);
        SkyBlockItem remaining = item.clone();
        remaining.setAmount(item.getAmount() - 1);
        player.setItemInHand(remaining.getAmount() > 0 ? remaining : null);
        player.sendMessage("<a>You consumed a <5>Brain Food <a>and gained <d>+1 ❃ Tracking<a>! <7>(" + (eaten + 1) + "/" + BRAIN_FOOD_LIMIT + ")");
    }

    private static List<String> gearLore(SkyBlockItem item, @Nullable SkyBlockPlayer player) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        boolean equipment = MythologicalGear.MYTHOS_EQUIPMENT.contains(type)
                || (MythologicalGear.CHALLENGER.contains(type) && type.name().matches(".*_(NECKLACE|CLOAK|BELT|BRACELET)"));
        List<String> lore = new ArrayList<>();
        lore.add("<6>Ability: Mythos' Might");
        lore.add("<7>Grants <a>2x <7>this " + (equipment ? "equipment's" : "armor's") + " stats while in");
        lore.add("<a>The Hub <7>during Diana's <2>Mythological");
        lore.add("<2>Ritual<7>.");

        int unearthed = player == null ? 0 : MythologicalGear.unearthedPieces(player);
        lore.add("");
        lore.add(tier("Unearthed", unearthed, 8));
        lore.add("<7>Reduces damage taken from <2>✿");
        lore.add("<2>Mythological <7>mobs by <a>" + format(MythologicalGear.unearthedReduction(unearthed)) + "%<7>.");

        if (MythologicalGear.MYTHOS_ARMOR.contains(type)) {
            int pieces = player == null ? 0 : MythologicalGear.familiarityPieces(player);
            lore.add("");
            lore.add(tier("Familiarity", pieces, 4));
            lore.add("<7>Grants <b>+" + format(MythologicalGear.familiarityMagicFind(pieces)) + " ✯ Magic Find <7>on <2>✿");
            lore.add("<2>Mythological <7>mobs.");
        } else if (MythologicalGear.MYTHOS_EQUIPMENT.contains(type)) {
            int pieces = player == null ? 0 : MythologicalGear.greedPieces(player);
            lore.add("");
            lore.add(tier("Mythological Greed", pieces, 4));
            lore.add("<7>Grants <a>+" + format(MythologicalGear.greedPercent(pieces)) + "% <7>more <6>Coins <7>from killing <2>✿");
            lore.add("<2>Mythological <7>mobs.");
        }
        return lore;
    }

    private static List<String> crownOfAvariceLore(SkyBlockItem item, @Nullable SkyBlockPlayer player) {
        long consumed = item.getAttributeHandler().getCoinsConsumed();
        boolean full = consumed >= CrownOfAvarice.CAPACITY;
        List<String> lore = new ArrayList<>();
        lore.add("<6>Ability: Overindulgence");
        lore.add("<2>✿ Mythological <7>mobs deal <c>1.5x");
        lore.add("<7>damage.");
        lore.add("");
        if (full) {
            lore.add("<7>You gain <a>2x <6>Coins <7>from <e>Griffin");
            lore.add("<e>Burrows <7>and <2>✿ Mythological <7>mobs.");
        } else {
            lore.add("<7>You gain <a>5x <6>Coins <7>from <e>Griffin");
            lore.add("<e>Burrows <7>and <2>✿ Mythological <7>mobs,");
            lore.add("<7>but they are consumed by this helmet.");
            lore.add("<8>(Perk changes at 1B Coins consumed)");
        }
        lore.add("");
        lore.add("<7>Grants <b>+2.5 Magic Find <7>against <2>✿");
        lore.add("<2>Mythological <7>mobs and deal <c>+0.015x");
        lore.add("<c>Damage <7>for each digit of <6>Coins");
        lore.add("<7>consumed.");
        lore.add("");
        lore.add("<7>Coins Consumed: <6>" + String.format("%,d", consumed));
        lore.add("  <c>+" + format(CrownOfAvarice.damageMultiplier(item)) + "x Damage");
        lore.add("  <b>+" + format(CrownOfAvarice.magicFind(item)) + " Magic Find <2>✿");
        return lore;
    }

    private static List<String> daedalusLore(SkyBlockItem item, @Nullable SkyBlockPlayer player) {
        int tiers = player == null || player.getSkyblockDataHandler() == null ? 0 : MythologicalBestiary.totalTiers(player);
        return List.of(
                "<7>Gains <c>+4 Damage <7>per Taming level",
                "<7>and copies the base <c>Combat <7>stats of",
                "<7>your active pet.",
                "",
                "<7>Grants <c>+1% ❁ Damage <7>and <b>+0.2",
                "<b>✯ Magic Find <7>on <2>✿ Mythological <7>mobs",
                "<7>per <3>Bestiary <7>tier you have across",
                "<7>all of them.",
                "",
                "<2>✿ Mythological <7>Bestiary Tiers: <3>" + tiers,
                "  <c>+" + tiers + "% ❁ Damage",
                "  <b>+" + format(tiers * 0.2) + " ✯ Magic Find");
    }

    private static String tier(String name, int pieces, int of) {
        return "<8>Tiered Bonus: " + (pieces >= 2 ? "<6>" : "") + name + " (" + pieces + "/" + of + ")";
    }

    private static String format(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(Math.round(value * 1000) / 1000D);
    }
}
