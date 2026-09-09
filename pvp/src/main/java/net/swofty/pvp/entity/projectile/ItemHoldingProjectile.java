package net.swofty.pvp.entity.projectile;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

// Marks a projectile entity as carrying a displayed/held ItemStack (e.g. snowball, egg, potion, ender pearl).
public interface ItemHoldingProjectile {
	void setItem(@NotNull ItemStack item);
}
