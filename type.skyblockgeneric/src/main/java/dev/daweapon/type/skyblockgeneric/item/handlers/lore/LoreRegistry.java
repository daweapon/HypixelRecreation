package dev.daweapon.type.skyblockgeneric.item.handlers.lore;

import dev.daweapon.type.skyblockgeneric.carnival.PartyEquipment;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.KuudraTeethPlaqueLore;
import dev.daweapon.type.skyblockgeneric.gifts.BottleOfJyrre;
import dev.daweapon.type.skyblockgeneric.gifts.SnowCannon;
import dev.daweapon.type.skyblockgeneric.gifts.SnowSuit;
import dev.daweapon.type.skyblockgeneric.mythological.gear.MythologicalItemHandlers;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.PotatoType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.item.ItemLore;
import dev.daweapon.type.skyblockgeneric.spray.SprayCanHandlers;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LoreRegistry {
    private static final Map<String, LoreConfig> REGISTERED_HANDLERS = new HashMap<>();

    static {
        MythologicalItemHandlers.registerLore();
        BottleOfJyrre.registerLore();
        SnowCannon.registerLore();
        SnowSuit.registerLore();
        PartyEquipment.registerLore();
        dev.daweapon.type.skyblockgeneric.foraging.FrenzyAxes.registerLore();
        dev.daweapon.type.skyblockgeneric.technoblade.BloodGodCrest.registerLore();
        KuudraTeethPlaqueLore.register();
        dev.daweapon.type.skyblockgeneric.crystalhollows.PrehistoricEggs.registerLore();
        dev.daweapon.type.skyblockgeneric.garden.tool.FarmingTools.registerLore();
        dev.daweapon.type.skyblockgeneric.garden.pest.PestItems.registerLore();
        dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseItems.registerLore();
        register("ENCHANTED_BOOK", new LoreConfig(
                (item, player) -> {
                    List<SkyBlockEnchantment> enchantments = item.getAttributeHandler().getEnchantments().toList();
                    ArrayList<String> lore = new ArrayList<>();

                    enchantments.forEach(enchantment -> {
                        lore.add(Text.of(enchantment.type().getEnch().isUltimate() ? "<d><l>{} {:roman}" : "<9>{} {:roman}",
                                enchantment.type().getName(),
                                enchantment.level()).serialize());
                        Text.of("<7><wrap:30>{}</wrap>",
                                        Text.parseLenient(enchantment.type().getDescription(enchantment.level(), player)))
                                .lines().forEach(line -> lore.add(line.serialize()));
                    });

                    lore.add(" ");
                    lore.add("<7>Apply Cost: <3>" + enchantments.stream()
                            .mapToInt(enchant -> enchant.type().getApplyCost(enchant.level(), player))
                            .sum() + " Exp Levels");
                    lore.add(" ");

                    Set<String> sourceTypes = enchantments.stream()
                            .flatMap(enchantment -> enchantment.type().getEnch().getGroups().stream())
                            .map(EnchantItemGroups::getDisplayName)
                            .collect(Collectors.toSet());

                    lore.add("<7>Applicable on: <9>" + String.join("<7>, <9>", sourceTypes));
                    lore.add("<7>Use this on an item in an Anvil to");
                    lore.add("<7>apply it!");

                    return lore;
                }, null));
        register("SKYBLOCK_MENU_LORE", new LoreConfig((item, player) -> Arrays.asList(
                "<7>View all of your SkyBlock progress,",
                "<7>including your Skills, Collections,",
                "<7>Recipes, and more!",
                "<e> ",
                "<e>Click to open!"
        ), (item, player) -> "<a>SkyBlock Menu <7>(Click)"));
        register(SprayCanHandlers.HANDLER_ID, SprayCanHandlers.lore());
        register("HOT_POTATO_BOOK", new LoreConfig((item, player) -> PotatoType.allLores(), null));
        register("MIDAS_SWORD", new LoreConfig((item, player) -> {
            List<String> lore = new ArrayList<>();
            long pricePaid = item.getAttributeHandler().getDarkAuctionPrice();
            int greedBonus = calculateGreedBonus(pricePaid);

            lore.add(Text.of("<7>Price paid: <6>{:,} Coins", pricePaid).serialize());
            lore.add("<c>❁ Strength <7>bonus: <c>+" + greedBonus);
            lore.add("<c>❁ Damage <7>bonus: <c>+" + greedBonus);

            return lore;
        }, null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
    }

    private static int calculateGreedBonus(long price) {
        if (price >= 50_000_000L) return 120;
        if (price >= 25_000_000L) {
            int bonus = 95 + (int)((price - 25_000_000L) / 1_000_000L);
            return Math.min(120, bonus);
        }
        if (price >= 7_500_000L) {
            int bonus = 60 + (int)((price - 7_500_000L) / 500_000L);
            return Math.min(95, bonus);
        }
        if (price >= 2_500_000L) {
            int bonus = 35 + (int)((price - 2_500_000L) / 200_000L);
            return Math.min(60, bonus);
        }
        if (price >= 1_000_000L) {
            int bonus = 20 + (int)((price - 1_000_000L) / 100_000L);
            return Math.min(35, bonus);
        }
        return (int) Math.min(20, price / 50_000L);
    }

    public static void register(String id, LoreConfig handler) {
        REGISTERED_HANDLERS.put(id, handler);
    }

    public static LoreConfig getHandler(String id) {
        return REGISTERED_HANDLERS.get(id);
    }
}
