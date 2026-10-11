package net.swofty.type.skyblockgeneric.gui.inventories.centauri;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeGemData;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeHotPotatoBookData;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentConflicts;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.enchantment.UltimateEnchantChoice;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import net.swofty.type.skyblockgeneric.gems.Gemstone;
import net.swofty.type.skyblockgeneric.item.ItemAttributeHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.EnchantableComponent;
import net.swofty.type.skyblockgeneric.item.components.GemstoneComponent;
import net.swofty.type.skyblockgeneric.item.components.HotPotatoableComponent;
import net.swofty.type.skyblockgeneric.item.components.NecronsBladeScrollComponent;
import net.swofty.type.skyblockgeneric.mythological.gear.CrownOfAvarice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Maxes out an item's enchantments, hot potato books, gemstones, Necron's Blade scrolls and
// recombobulation for testing, and fills a Crown of Avarice to its 1B cap
public final class CentauriItemMaximizer {
    private static final List<String> NECRONS_BLADE_SCROLLS = List.of("IMPLOSION", "SHADOW_WARP", "WITHER_SHIELD");

    private CentauriItemMaximizer() {}

    public static int maximize(SkyBlockPlayer player, SkyBlockItem item) {
        int applied = 0;
        applied += maximizeEnchantments(player, item);
        applied += maximizePotatoBooks(item);
        applied += maximizeGemstones(item);
        applied += maximizeScrolls(item);
        applied += maximizeCrownOfAvarice(item);

        ItemAttributeHandler handler = item.getAttributeHandler();
        if (handler.getRarity().isCanReforge() && !handler.isRecombobulated()) {
            handler.setRecombobulated(true);
            applied++;
        }
        return applied;
    }

    private static int maximizeCrownOfAvarice(SkyBlockItem item) {
        if (item.getAttributeHandler().getPotentialType() != ItemType.CROWN_OF_AVARICE) return 0;
        if (item.getAttributeHandler().getCoinsConsumed() >= CrownOfAvarice.CAPACITY) return 0;
        item.getAttributeHandler().setCoinsConsumed(CrownOfAvarice.CAPACITY);
        return 1;
    }

    private static int maximizeEnchantments(SkyBlockPlayer player, SkyBlockItem item) {
        if (!item.hasComponent(EnchantableComponent.class)) return 0;
        var itemGroups = item.getComponent(EnchantableComponent.class).groupsFor(item);
        int applied = 0;

        applied += applyUltimate(player, item, itemGroups);

        for (EnchantmentType type : EnchantmentType.values()) {
            if (type == EnchantmentType.KNOCKBACK) continue;
            if (EnchantmentConflicts.blockedBy(item, type)) continue;
            if (type.getEnch().getGroups().stream().noneMatch(itemGroups::contains)) continue;
            int maximumLevel = type.getEnch().getLevelsToApply(player).maximumLevel();
            var current = item.getAttributeHandler().getEnchantment(type);
            if (current != null && current.level() >= maximumLevel) continue;

            item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(type, maximumLevel));
            applied++;
        }
        return applied;
    }

    private static int applyUltimate(SkyBlockPlayer player, SkyBlockItem item, List<EnchantItemGroups> itemGroups) {
        ItemType itemType = item.getAttributeHandler().getPotentialType();
        EnchantmentType ultimate = UltimateEnchantChoice.forItem(itemType == null ? null : itemType.name(), itemGroups);
        if (ultimate == null) return 0;
        int maximumLevel = ultimate.getEnch().getLevelsToApply(player).maximumLevel();
        var current = item.getAttributeHandler().getEnchantment(ultimate);
        if (current != null && current.level() >= maximumLevel) return 0;
        EnchantmentConflicts.applyTo(item, ultimate);
        item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(ultimate, maximumLevel));
        return 1;
    }

    private static int maximizePotatoBooks(SkyBlockItem item) {
        if (!item.hasComponent(HotPotatoableComponent.class)) return 0;
        HotPotatoableComponent component = item.getComponent(HotPotatoableComponent.class);
        ItemAttributeHotPotatoBookData.HotPotatoBookData data = item.getAttributeHandler().getHotPotatoBookData();
        int applied = 0;

        for (var entry : component.getApplicableItems().entrySet()) {
            int missing = entry.getValue() - data.getAmount(entry.getKey());
            if (missing <= 0) continue;
            data.addAmount(entry.getKey(), missing);
            applied += missing;
        }
        data.setPotatoType(component.getPotatoType());
        item.getAttributeHandler().setHotPotatoBookData(data);
        return applied;
    }

    private static int maximizeScrolls(SkyBlockItem item) {
        if (!item.hasComponent(NecronsBladeScrollComponent.class)) return 0;
        int applied = 0;
        for (String scroll : NECRONS_BLADE_SCROLLS) {
            if (item.getAttributeHandler().getAppliedScrolls().contains(scroll)) continue;
            item.getAttributeHandler().addAppliedScroll(scroll);
            applied++;
        }
        return applied;
    }

    private static int maximizeGemstones(SkyBlockItem item) {
        if (!item.hasComponent(GemstoneComponent.class)) return 0;
        GemstoneComponent component = item.getComponent(GemstoneComponent.class);
        ItemAttributeGemData.GemData data = item.getAttributeHandler().getGemData();
        int applied = 0;

        for (int index = 0; index < component.getSlots().size(); index++) {
            Gemstone gemstone = component.getSlots().get(index).slot().getValidGemstones().getFirst();
            ItemType perfectGem = gemstone.getItem().getLast();
            ItemAttributeGemData.GemData.GemSlots current = data.getGem(index);
            if (current == null) {
                data.putGem(new ItemAttributeGemData.GemData.GemSlots(index, perfectGem, true));
                applied++;
            } else {
                if (!current.isUnlocked()) { current.setUnlocked(true); applied++; }
                if (current.getFilledWith() != perfectGem) { current.setFilledWith(perfectGem); applied++; }
            }
        }
        item.getAttributeHandler().setGemData(data);
        return applied;
    }
}
