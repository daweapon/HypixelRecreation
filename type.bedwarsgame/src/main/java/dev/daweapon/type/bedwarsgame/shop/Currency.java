package dev.daweapon.type.bedwarsgame.shop;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

// The four Bedwars shop currencies (iron/gold/diamond/emerald) with their display material and color.
@Getter
public enum Currency {
    IRON("Iron", Material.IRON_INGOT, NamedTextColor.WHITE),
    GOLD("Gold", Material.GOLD_INGOT, NamedTextColor.GOLD),
    DIAMOND("Diamond", Material.DIAMOND, NamedTextColor.AQUA),
    EMERALD("Emerald", Material.EMERALD, NamedTextColor.DARK_GREEN);

    private final String name;
	private final Material material;
	private final TextColor color;

    Currency(String name, Material material, TextColor color) {
        this.name = name;
		this.material = material;
		this.color = color;
    }

    @Nullable
    public static Currency byMaterial(Material material) {
        for (Currency currency : values()) {
            if (currency.getMaterial() == material) {
                return currency;
            }
        }
        return null;
    }

}

