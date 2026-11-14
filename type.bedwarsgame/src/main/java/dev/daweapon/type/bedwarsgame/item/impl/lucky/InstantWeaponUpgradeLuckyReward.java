package dev.daweapon.type.bedwarsgame.item.impl.lucky;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.bedwarsgame.shop.impl.ReplaceAdderItem;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.data.datapoints.DatapointBedWarsHotbar;

import java.util.List;

// Lucky Block reward that instantly upgrades the opener's best sword to the next tier.
public class InstantWeaponUpgradeLuckyReward extends LuckyReward {
    public InstantWeaponUpgradeLuckyReward() {
        super("Instant Weapon Upgrade");
    }

    @Override
    public void apply(BedWarsPlayer player, Pos openedAt) {
        Material bestSword = bestSword(player);
        if (bestSword == Material.DIAMOND_SWORD) {
            player.sendMessage("<c>You already have the best sword.");
            return;
        }
        Material next;
        if (bestSword == Material.IRON_SWORD) {
            next = Material.DIAMOND_SWORD;
        } else if (bestSword == Material.STONE_SWORD) {
            next = Material.IRON_SWORD;
        } else {
            next = Material.STONE_SWORD;
        }
        new ReplaceAdderItem("lucky_weapon_upgrade", "Weapon Upgrade", "", 0, null, next, bestSword,
            DatapointBedWarsHotbar.HotbarItemType.MELEE).onPurchase(player);
        player.sendMessage("<a>Your weapon was upgraded.");
    }

    private Material bestSword(BedWarsPlayer player) {
        List<Material> order = List.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD);
        Material best = Material.WOODEN_SWORD;
        for (ItemStack stack : player.getInventory().getItemStacks()) {
            if (order.contains(stack.material()) && order.indexOf(stack.material()) > order.indexOf(best)) {
                best = stack.material();
            }
        }
        return best;
    }
}
