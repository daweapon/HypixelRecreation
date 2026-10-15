package dev.daweapon.type.bedwarsgame.shop;

import net.minestom.server.item.Material;
import dev.daweapon.commons.bedwars.BedWarsGameType;

import java.util.function.Function;

// A single tier of an upgradeable shop item's price/currency/display material.
public record UpgradeableItemTier(String name, Function<BedWarsGameType, Integer> price, Currency currency, Material material) {
}

