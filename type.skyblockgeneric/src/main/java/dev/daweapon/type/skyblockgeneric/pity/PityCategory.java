package dev.daweapon.type.skyblockgeneric.pity;

import net.minestom.server.item.Material;

import java.util.List;

public record PityCategory(PityCategoryId id, String menuTitle, int slot, Material icon, String title,
                           List<String> lore, List<PityItem> items) {
}
