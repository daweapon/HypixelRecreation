package dev.daweapon.type.skyblockgeneric.dungeons.gui;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestRun;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestService;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonRewardChest;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The reward chest pack models and the Contents/Cost lore Hypixel shows on a chest and its open button. */
public final class DungeonChestIcons {
    private DungeonChestIcons() {
    }

    public static String model(DungeonChestTier tier) {
        return "hypixel_skyblock:item/island_relevant/dungeons/reward_chest_" + tier.name().toLowerCase(Locale.ROOT);
    }

    public static ItemStack.Builder chest(DungeonChestTier tier, Text name, List<Text> lore) {
        return ItemStacks.item(Material.CHEST, name, lore).set(DataComponents.ITEM_MODEL, model(tier));
    }

    public static ItemStack modelStack(DungeonChestTier tier) {
        return ItemStack.builder(Material.CHEST).set(DataComponents.ITEM_MODEL, model(tier)).build();
    }

    public static List<Text> contentsAndCost(DungeonChestRun run, DungeonRewardChest chest) {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Contents"));
        chest.rewards().forEach(reward -> lore.add(reward.name()));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Cost"));
        lore.add(Text.of(DungeonChestService.costLine(chest.cost())));
        if (!chest.opened() && run.nextNeedsKey()) lore.add(Text.of("<9>Dungeon Chest Key"));
        lore.add(Text.empty());
        return lore;
    }

    public static Text status(DungeonChestRun run, DungeonRewardChest chest, String clickLine) {
        if (chest.opened()) return Text.of("<c>Already opened!");
        if (!run.canOpenMore()) return Text.of("<c>Can't open another chest!");
        return Text.of(clickLine);
    }
}
