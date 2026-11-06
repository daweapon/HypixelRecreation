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
import java.util.Map;

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

    /** The chest's icon in a run's chest list at Croesus: Hypixel's captured heads, our pack model for the others. */
    public static ItemStack.Builder listIcon(DungeonChestTier tier, List<Text> lore) {
        Text name = Text.of(tier.colour() + tier.displayName());
        String head = HEADS.get(tier);
        return head == null ? chest(tier, name, lore) : ItemStacks.head(head, name, lore);
    }

    private static final Map<DungeonChestTier, String> HEADS = Map.of(
            DungeonChestTier.WOOD, "a4bc6b5800e57d8c23966a50e9b55aad8d0829099e987c96a01ee5be18dc82d0",
            DungeonChestTier.GOLD, "861bad6393a08e258268ee84f641e4da612603fec35d9438a6bacc5c660cca43",
            DungeonChestTier.DIAMOND, "f7bf02390d3f3f4cce4bfedc3c190484138a3174d8541a8fd912ebb2147fcc0e",
            DungeonChestTier.EMERALD, "aed20faddd3f9632f742f64350c38ef76c5e1b49164fce0d23a9a64b4e122540");

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
