package dev.daweapon.type.skyblockgeneric.gui.inventories.enchantment;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentConflicts;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEntry;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.EnchantmentCatalogue;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Builds the text the Enchantments Guide shows for an enchantment: effect, sources, applicable items, conflicts.
final class EnchantmentGuideLore {
    private EnchantmentGuideLore() {}

    static String title(EnchantmentType type, int level) {
        String name = type.getName() + " " + StringUtility.getAsRomanNumeral(level);
        return type.getEnch().isUltimate() ? "<d><l>" + name : "<9>" + name;
    }

    static int maximumLevel(EnchantmentType type, SkyBlockPlayer player) {
        return Math.max(type.getEnch().getLevelsToApply(player).maximumLevel(), type.getEnch().getEndcapLevel());
    }

    static List<Text> description(EnchantmentType type, int level, SkyBlockPlayer player) {
        return Text.of("<7><wrap:30>{}</wrap>", Text.parseLenient(type.getDescription(level, player))).lines();
    }

    static List<Text> sources(EnchantmentType type, SkyBlockPlayer player) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<6>Source:"));
        CatalogueEntry entry = EnchantmentCatalogue.find(type.name());
        Set<String> seen = new LinkedHashSet<>();
        type.getEnch().getSources(player).forEach(source -> seen.add(source.toString().trim()));
        if (entry != null) entry.sources().forEach(source -> seen.add(source.trim()));
        if (seen.isEmpty()) seen.add("<7>Unknown");
        seen.forEach(line -> lines.add(Text.parseLenient(line)));
        return lines;
    }

    static List<Text> appliedTo(EnchantmentType type) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<6>Applied To:"));
        CatalogueEntry entry = EnchantmentCatalogue.find(type.name());
        List<String> labels = entry != null && !entry.appliedTo().isEmpty() ? entry.appliedTo() : type.getEnch().getGroups().stream()
                .map(EnchantItemGroups::getDisplayName).distinct().collect(Collectors.toList());
        labels.forEach(label -> lines.add(Text.parseLenient("<7>- <f>" + label)));
        return lines;
    }

    static List<Text> conflicts(EnchantmentType type) {
        List<Text> lines = new ArrayList<>();
        var conflicts = EnchantmentConflicts.of(type).stream().sorted(Comparator.comparing(EnchantmentType::getName)).toList();
        if (conflicts.isEmpty()) return lines;
        lines.add(Text.of("<6>Conflicts:"));
        conflicts.forEach(other -> lines.add(Text.parseLenient("<7>- <c>" + other.getName())));
        return lines;
    }

    static int minimumLevel(EnchantmentType type, SkyBlockPlayer player) {
        return type.getEnch().getLevelsToApply(player).minimumLevel();
    }

    static String levelRange(EnchantmentType type, SkyBlockPlayer player) {
        int min = minimumLevel(type, player);
        int max = maximumLevel(type, player);
        if (min == max) return StringUtility.getAsRomanNumeral(max);
        return StringUtility.getAsRomanNumeral(min) + "-" + StringUtility.getAsRomanNumeral(max);
    }
}
