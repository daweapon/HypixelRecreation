package net.swofty.type.skyblockgeneric.minion.inferno;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.minion.IslandMinionData;
import net.swofty.type.skyblockgeneric.minion.MinionRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Right-clicking an Inferno Minion with Capsaicin Eyedrops raises its Hypergolic unique-item
 * chances by 30% for 24 hours.
 */
public final class CapsaicinEyedrops {
    public static final long DURATION_MS = 24L * 60 * 60 * 1000;

    private CapsaicinEyedrops() {
    }

    public static boolean isActive(IslandMinionData.IslandMinion minion) {
        return minion.getCapsaicinExpiry() > System.currentTimeMillis();
    }

    public static boolean isHeld(SkyBlockPlayer player) {
        return new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType()
                == ItemType.CAPSAICIN_EYEDROPS_NO_CHARGES;
    }

    public static void apply(SkyBlockPlayer player, IslandMinionData.IslandMinion minion) {
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        if (minion.getMinion() != MinionRegistry.INFERNO) {
            player.sendMessage("<c>Capsaicin Eyedrops can only be used on Inferno Minions!");
            return;
        }
        if (isActive(minion)) {
            long remaining = minion.getCapsaicinExpiry() - System.currentTimeMillis();
            player.sendMessage("<c>This minion's eyes are already burning! <7>(" + StringUtility.formatTimeLeft(remaining) + " left)");
            return;
        }

        minion.setCapsaicinExpiry(System.currentTimeMillis() + DURATION_MS);
        SkyBlockItem remaining = held.clone();
        remaining.setAmount(held.getAmount() - 1);
        player.setItemInHand(remaining.getAmount() > 0 ? remaining : null);
        player.sendMessage("<a>You applied Capsaicin Eyedrops to your Inferno Minion! Its unique drop chances are boosted by <a>30% <a>for <a>24 hours<a>.");
    }
}
