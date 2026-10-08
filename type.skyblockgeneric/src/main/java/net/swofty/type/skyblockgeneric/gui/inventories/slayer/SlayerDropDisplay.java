package net.swofty.type.skyblockgeneric.gui.inventories.slayer;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.slayer.SlayerDrop;
import net.swofty.type.skyblockgeneric.slayer.SlayerDropTable;
import net.swofty.type.skyblockgeneric.slayer.SlayerTier;
import net.swofty.type.skyblockgeneric.slayer.SlayerType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

final class SlayerDropDisplay {
    private SlayerDropDisplay() {
    }

    static ItemStack.Builder icon(SkyBlockPlayer player, SlayerDrop drop, List<Text> lore) {
        ItemStack.Builder builder = drop.item() == null
            ? ItemStacks.item(Material.GRAY_DYE, Text.empty(), List.of())
            : PlayerItemUpdater.playerUpdate(player, SlayerDropTable.create(drop, 1).getItemStackBuilder().build());
        ItemStacks.name(builder, SlayerDropTable.displayName(drop));
        return ItemStacks.lore(builder, lore);
    }

    static List<Text> oddsLines(SlayerDrop drop) {
        List<Text> lore = new ArrayList<>();
        for (SlayerTier tier : SlayerTier.values()) {
            SlayerDrop.TierOdds odds = drop.odds(tier);
            if (odds == null) continue;
            lore.add(Text.of("<7>Tier {}: <f>{}x {}", tier.numeral(), odds.amountText(), chanceText(drop, odds)));
        }
        return lore;
    }

    static Text requirement(SlayerType type, SlayerDrop drop, int level) {
        if (drop.requiredLevel() <= 0) return null;
        return level >= drop.requiredLevel()
            ? Text.of("<7>Requires: <a>{} Slayer {}", type.categoryName(), drop.requiredLevel())
            : Text.of("<4>☠ <c>Requires <5>{} Slayer {}<c>.", type.categoryName(), drop.requiredLevel());
    }

    private static Text chanceText(SlayerDrop drop, SlayerDrop.TierOdds odds) {
        return switch (drop.pool()) {
            case TOKEN -> Text.of("<8>(<a>Guaranteed<8>)");
            case LOOT_SHARE -> Text.of("<8>(<7>Loot Share only<8>)");
            default -> Text.of("<8>(<b>{}<8>)", format(odds.chancePercent()));
        };
    }

    static String format(double percent) {
        if (percent >= 0.01) return String.format("%.2f%%", percent);
        return "1/" + String.format("%,d", Math.round(100D / percent));
    }
}
