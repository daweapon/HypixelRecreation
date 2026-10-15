package dev.daweapon.type.crimsonisle.dojo;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoBelt;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoProgress;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointInventory;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.crafting.UpgradeCarryOver;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockInventory;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Master Tao's belt rewards. Since 0.19.10 he upgrades the belt you bring, so the previous belt has
 * to be in your inventory or belt slot; it keeps its enchantments and reforge.
 */
public final class DojoBeltRewards {
    public static final String GRAND_MASTER = "skyblock.dojo_grand_master";

    private DojoBeltRewards() {
    }

    /** First talk: the White Belt gift. */
    public static void giftWhiteBelt(SkyBlockPlayer player) {
        player.addAndUpdateItem(new SkyBlockItem(DojoBelt.WHITE.getItemType()));
        DojoProgress.setHighestBelt(player, DojoBelt.WHITE);
    }

    public static boolean claim(SkyBlockPlayer player, DojoBelt belt) {
        if (DojoProgress.hasClaimed(player, belt)) return false;
        DojoBelt previous = belt.previous();
        if (DojoProgress.points(player) < belt.getRequiredPoints()) {
            player.sendMessage("<c>You need " + StringUtility.commaify(belt.getRequiredPoints()) + " Dojo Points to claim these rewards!");
            return false;
        }
        if (previous != null && !DojoProgress.hasClaimed(player, previous)) {
            player.sendMessage("<c>You must claim the " + previous.getDisplayName() + " Belt rewards first!");
            return false;
        }
        if (previous != null && !upgrade(player, previous, belt)) {
            MasterTao.say(player, "Bring me your " + previous.coloured() + " Belt<f> and I will upgrade it for you!");
            return false;
        }

        DojoProgress.setHighestBelt(player, belt);
        var skills = player.getSkills();
        skills.setRaw(player, SkillCategories.COMBAT, skills.getRaw(SkillCategories.COMBAT) + belt.getCombatXP());
        player.sendMessage("<6><l>BELT UPGRADE! " + belt.coloured() + " Belt");
        player.sendMessage("  <8>+<3>" + StringUtility.commaify(belt.getCombatXP()) + " <7>Combat Experience");
        player.sendMessage("  <8>+<b>" + belt.getSkyBlockXP() + " SkyBlock XP");
        if (belt == DojoBelt.BLACK) player.getAchievementHandler().completeAchievement(GRAND_MASTER);
        return true;
    }

    private static boolean upgrade(SkyBlockPlayer player, DojoBelt from, DojoBelt to) {
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (!isBelt(stack, from.getItemType())) continue;
            SkyBlockItem upgraded = upgraded(new SkyBlockItem(stack), from, to);
            player.getInventory().setItemStack(slot, PlayerItemUpdater.playerUpdate(player, upgraded.getItemStack()).build());
            return true;
        }
        DatapointInventory datapoint = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class);
        SkyBlockInventory inventory = datapoint.getValue();
        SkyBlockItem equipped = inventory.getBelt() == null ? null : new SkyBlockItem(inventory.getBelt());
        if (equipped == null || equipped.isNA() || equipped.getAttributeHandler().getPotentialType() != from.getItemType()) {
            return false;
        }
        inventory.setBelt(upgraded(equipped, from, to).toUnderstandable());
        datapoint.setValue(inventory);
        return true;
    }

    private static SkyBlockItem upgraded(SkyBlockItem old, DojoBelt from, DojoBelt to) {
        return UpgradeCarryOver.apply(new SkyBlockItem(to.getItemType()), from.getItemType(), new SkyBlockItem[]{old});
    }

    private static boolean isBelt(ItemStack stack, ItemType type) {
        if (stack.isAir() || stack.material() != Material.PLAYER_HEAD || !SkyBlockItem.isSkyBlockItem(stack)) return false;
        return new SkyBlockItem(stack).getAttributeHandler().getPotentialType() == type;
    }
}
