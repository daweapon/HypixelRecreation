package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;

import java.util.List;

public interface BestiaryEntry {
    String getName();
    String getDescription();
    GUIMaterial getGuiMaterial();
    List<BestiaryMob> getMobs();
}

