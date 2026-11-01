package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.chip.ChipData;
import dev.daweapon.type.skyblockgeneric.garden.chip.ChipService;
import dev.daweapon.type.skyblockgeneric.garden.chip.GardenChip;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIManageChips extends StatelessView {
    private static final int[] SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Manage Chips"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(4, (_, c) -> ItemStacks.item(Material.PLAYER_HEAD, Text.of("<a>Manage Chips"), List.of(
                Text.of("<7>Upgrade your <9>Chips <7>to gain powerful"),
                Text.of("<7>buffs!"),
                Text.empty(),
                Text.of("<2>✿ Sowdust"),
                Text.of("<7>Sowdust is dropped from harvesting"),
                Text.of("<7>crops in <a>The Garden <7>and is used to"),
                Text.of("<7>upgrade the Chips you've unlocked!"),
                Text.empty(),
                Text.of("<7>Sowdust: <2>{}", StringUtility.commaify(Math.floor(Gardens.profile((SkyBlockPlayer) c.player()).getSowdust()))))));
        layout.slot(50, (_, _) -> ItemStacks.item(Material.REDSTONE_TORCH, Text.of("<a>Chip Information"), List.of(
                Text.of("<7>Chips can be upgraded using <2>Sowdust<7>."),
                Text.of("<7>To increase a Chip's <9>maximum level<7>,"),
                Text.of("<7>you'll need to upgrade its rarity by"),
                Text.of("<a>using Chip items<7>."),
                Text.empty(),
                Text.of("<7>▪ <9><bold>RARE <7>Chips max at <a>Level 10"),
                Text.of("<7>▪ <5><bold>EPIC <7>Chips max at <a>Level 15"),
                Text.of("<7>▪ <6><bold>LEGENDARY <7>Chips max at <a>Level 20"),
                Text.empty(),
                Text.of("<7>Increasing a Chip's rarity also"),
                Text.of("<7>increases the scaling of its perks!"))));
        layout.slot(53, (_, _) -> ItemStacks.item(Material.BARRIER, Text.of("<c>Reset Farming Chips"), List.of(
                        Text.of("<7>Reset the levels of all of your"),
                        Text.of("<7>Chips and reimburse the <2>Sowdust"),
                        Text.of("<7>you spent on them."),
                        Text.empty(),
                        Text.of("<e>Click to reset!"))),
                (_, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    long refund = ChipService.reset(player);
                    player.sendMessage(Text.of("<a>Your Chips were reset and you were reimbursed <2>{} Sowdust<a>!", StringUtility.commaify(refund)));
                    c.session(DefaultState.class).refresh();
                });
        GardenChip[] chips = GardenChip.values();
        for (int i = 0; i < chips.length; i++) {
            GardenChip chip = chips[i];
            layout.slot(SLOTS[i], (_, c) -> item((SkyBlockPlayer) c.player(), chip), (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                int attempts = click.click() instanceof Click.LeftShift || click.click() instanceof Click.RightShift ? 10 : 1;
                int upgraded = 0;
                ChipService.UpgradeResult result = ChipService.UpgradeResult.UPGRADED;
                for (int n = 0; n < attempts; n++) {
                    result = ChipService.upgrade(player, chip);
                    if (result != ChipService.UpgradeResult.UPGRADED) break;
                    upgraded++;
                }
                if (upgraded > 0) {
                    player.sendMessage(Text.of("<a>Your {} Chip <a>is now level {}<a>!", chip.getDisplayName(), ChipService.data(player).level(chip)));
                } else {
                    switch (result) {
                        case LOCKED -> player.sendMessage(Text.of("<c>You haven't unlocked this chip yet!"));
                        case MAX_FOR_RARITY -> player.sendMessage(Text.of("<c>Redeem more chips to raise the maximum level!"));
                        case NOT_ENOUGH_SOWDUST -> player.sendMessage(Text.of("<c>You need more Sowdust!"));
                        default -> {
                        }
                    }
                }
                c.session(DefaultState.class).refresh();
            });
        }
    }

    public static ItemStack.Builder icon(GardenChip chip) {
        ItemType type = ItemType.get(chip.getItemId());
        return type == null ? ItemStack.builder(chip.getIcon()) : new SkyBlockItem(type).getDisplayItem();
    }

    private ItemStack.Builder item(SkyBlockPlayer player, GardenChip chip) {
        ChipData data = ChipService.data(player);
        List<Text> lore = new ArrayList<>();
        if (!data.unlocked(chip)) {
            lore.add(Text.of("<7>Level <a>0<7>/<a>{}", GardenChip.MAX_LEVEL[0]));
            lore.add(Text.empty());
            lore.add(Text.of("<6>Ability: {}", chip.getDisplayName()));
            lore.addAll(Text.of("<7><wrap:30>" + chip.getDescription().replace("<green>", "<a>").replace("<gray>", "<7>") + "</wrap>").lines());
            lore.add(Text.empty());
            lore.add(Text.of("<7>" + source(chip)));
            lore.add(Text.empty());
            lore.add(Text.of("<c><bold>LOCKED"));
            return ItemStacks.text(icon(chip), Text.of("<c>" + chip.getDisplayName() + " Chip"), lore);
        }
        int rarity = data.rarity(chip);
        int level = data.level(chip);
        int max = ChipService.maxLevel(data, chip);
        lore.add(Text.of("<7>Level <a>{}<7>/<a>{}", level, max));
        lore.add(Text.empty());
        lore.add(Text.of("<6>Ability: {}", chip.getDisplayName()));
        lore.add(Text.of("<7>Current: <a>{}", ChipService.describe(data, chip)));
        if (!chip.isActive()) lore.add(Text.of("<c>Not active yet!"));
        lore.add(Text.empty());
        if (level < max) {
            lore.add(Text.of("<7>Next level costs <2>{} Sowdust", StringUtility.commaify(ChipService.costToReach(level + 1))));
            lore.add(Text.of("<e>Click to level up!"));
            lore.add(Text.of("<e>Shift Click to level up 10 times!"));
        } else if (rarity < 2) {
            int needed = GardenChip.CONSUMED_FOR_RARITY[rarity + 1] - data.consumed(chip);
            lore.add(Text.of("<7>Redeem <a>{} <7>more chips to upgrade to {}<7>!", needed, GardenChip.RARITY_COLOURS[rarity + 1] + GardenChip.RARITY_NAMES[rarity + 1]));
        } else {
            lore.add(Text.of("<a><bold>MAXED!"));
            lore.add(Text.empty());
            lore.add(Text.of(GardenChip.RARITY_COLOURS[rarity] + "<bold>" + GardenChip.RARITY_NAMES[rarity] + " GARDEN CHIP"));
        }
        return ItemStacks.text(icon(chip), Text.of(GardenChip.RARITY_COLOURS[rarity] + chip.getDisplayName() + " Chip"), lore);
    }

    private static String source(GardenChip chip) {
        return switch (chip) {
            case VERMIN_VAPORIZER -> "Rarely dropped by the <2>Dragonfly Pest<7>.";
            case SYNTHESIS, EVERGREEN -> "Rarely drops from the <a>Greenhouse<7>.";
            case SOWLEDGE, CROPSHOT -> "Unlocked from <a>SkyMart<7>.";
            case MECHAMIND, OVERDRIVE -> "Purchased from <b>Anita's shop<7>.";
            case HYPERCHARGE, QUICKDRAW -> "Rarely gets rewarded from completing <b>Visitor Offers<7>.";
            case RAREFINDER -> "Rarely drops from farming <e>Crops<7>.";
        };
    }
}
