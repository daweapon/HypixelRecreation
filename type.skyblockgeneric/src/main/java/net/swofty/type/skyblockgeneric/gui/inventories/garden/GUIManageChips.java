package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.chip.ChipData;
import net.swofty.type.skyblockgeneric.garden.chip.ChipService;
import net.swofty.type.skyblockgeneric.garden.chip.GardenChip;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
        Components.backOrClose(layout, 48, ctx);
        Components.close(layout, 49);
        layout.slot(4, (_, c) -> ItemStacks.item(Material.PLAYER_HEAD, Text.of("<a>Garden Chips"), List.of(
                Text.of("<7>Redeem chips to unlock them and"),
                Text.of("<7>increase their rarity, then level them"),
                Text.of("<7>up with <2>Sowdust<7>."),
                Text.empty(),
                Text.of("<7>Sowdust: <2>{}", StringUtility.commaify(Math.floor(Gardens.profile((SkyBlockPlayer) c.player()).getSowdust()))))));
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

    static ItemStack.Builder icon(GardenChip chip) {
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
            lore.add(Text.of("<c>LOCKED"));
            return ItemStacks.text(icon(chip), Text.of("<7>" + chip.getDisplayName() + " Chip"), lore);
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
            lore.add(Text.of("<a>Max level reached!"));
        }
        return ItemStacks.text(icon(chip), Text.of(GardenChip.RARITY_COLOURS[rarity] + chip.getDisplayName() + " Chip"), lore);
    }
}
