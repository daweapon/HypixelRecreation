package dev.daweapon.type.skyblockgeneric.enchantment.catalogue;

import dev.daweapon.type.skyblockgeneric.enchantment.abstr.Ench;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

// An enchantment whose levels, text, cost and item groups come from the catalogue. Subclasses that wire an
// effect override isEffectActive; the rest are catalogued only and say so in their description.
public class CatalogueEnchantment implements Ench {
    private final CatalogueEntry entry;

    protected CatalogueEnchantment(String key) {
        this.entry = EnchantmentCatalogue.require(key);
    }

    public static CatalogueEnchantment of(String key) {
        return new CatalogueEnchantment(key);
    }

    public static CatalogueEnchantment active(String key) {
        return new CatalogueEnchantment(key) {
            @Override
            public boolean isEffectActive() {
                return true;
            }
        };
    }

    public CatalogueEntry entry() {
        return entry;
    }

    protected @Nullable String limitation() {
        return null;
    }

    @Override
    public String getDescription(int level) {
        String text = entry.description(level);
        if (!isEffectActive()) return text + "\n<8>(Not active yet)";
        String limitation = limitation();
        return limitation == null ? text : text + "\n<8>(" + limitation + ")";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>();
        for (int level = entry.minLevel(); level <= entry.maxLevel(); level++) levels.put(level, entry.cost(level));
        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return entry.groups();
    }

    @Override
    public boolean isUltimate() {
        return entry.ultimate();
    }

    @Override
    public boolean isEffectActive() {
        return false;
    }
}
