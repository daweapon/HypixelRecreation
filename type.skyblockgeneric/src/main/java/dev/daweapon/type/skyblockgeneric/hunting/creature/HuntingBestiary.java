package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryEntry;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.CataloguedBestiaryAreas;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Bestiary families of the Galatea and Torrhus creatures: one entry per creature, or per wiki family (Tiki, Chill). */
public final class HuntingBestiary {
    private HuntingBestiary() {
    }

    public static BestiaryEntry[] section(String section) {
        Map<String, List<CreatureSpec>> groups = new LinkedHashMap<>();
        for (CreatureSpec spec : CreatureCatalog.all()) {
            if (!spec.bestiary().section().equals(section)) continue;
            String key = spec.bestiary().family() != null ? "family:" + spec.bestiary().family() : "id:" + spec.id();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(spec);
        }
        List<BestiaryEntry> entries = new ArrayList<>();
        for (List<CreatureSpec> members : groups.values()) {
            CreatureSpec lead = members.stream().filter(CreatureSpec::hasOwnBestiary).findFirst().orElse(members.getFirst());
            String name = lead.bestiary().family() != null ? lead.bestiary().family() : lead.name();
            GUIMaterial icon = HuntingCreatureMob.iconOf(lead);
            List<BestiaryMob> mobs = members.stream().map(spec -> (BestiaryMob) HuntingCreatureMob.create(spec)).toList();
            entries.add(new CataloguedBestiaryAreas.CataloguedFamily("<a>" + name, lead.bestiary().lore(), icon, mobs));
        }
        entries.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return entries.toArray(BestiaryEntry[]::new);
    }
}
