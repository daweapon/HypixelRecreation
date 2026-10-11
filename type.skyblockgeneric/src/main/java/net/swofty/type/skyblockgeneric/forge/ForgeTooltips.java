package net.swofty.type.skyblockgeneric.forge;

import net.kyori.adventure.text.format.NamedTextColor;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public final class ForgeTooltips {
    private ForgeTooltips() {
    }

    public static String colorTag(ItemType type) {
        var color = type.rarity.getColor();
        if (color == NamedTextColor.GREEN) return "<a>";
        if (color == NamedTextColor.BLUE) return "<9>";
        if (color == NamedTextColor.DARK_PURPLE) return "<5>";
        if (color == NamedTextColor.GOLD) return "<6>";
        if (color == NamedTextColor.LIGHT_PURPLE) return "<d>";
        if (color == NamedTextColor.AQUA) return "<b>";
        if (color == NamedTextColor.RED) return "<c>";
        if (color == NamedTextColor.DARK_RED) return "<4>";
        return "<f>";
    }

    public static String duration(long millis) {
        long seconds = Math.max(0, (millis + 999) / 1000);
        long days = seconds / 86_400;
        long hours = seconds % 86_400 / 3_600;
        long minutes = seconds % 3_600 / 60;
        long secs = seconds % 60;
        StringBuilder out = new StringBuilder();
        if (days > 0) out.append(days).append("d ");
        if (hours > 0) out.append(hours).append("h ");
        if (minutes > 0) out.append(minutes).append("m ");
        if (secs > 0 || out.isEmpty()) out.append(secs).append("s");
        return out.toString().trim();
    }

    public static String name(ForgeRecipe recipe) {
        ItemType output = recipe.output();
        if (output == null || recipe.isPet()) return "<6>" + recipe.name();
        return colorTag(output) + new SkyBlockItem(output).getDisplayName();
    }

    public static String ingredients(ForgeRecipe recipe) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, Integer> input : recipe.inputs().entrySet()) {
            ItemType type = ItemType.get(input.getKey());
            String name = type == null ? "<8>" + input.getKey().toLowerCase(Locale.ROOT).replace('_', ' ')
                    : colorTag(type) + new SkyBlockItem(type).getDisplayName();
            out.append(name);
            if (input.getValue() > 1) out.append(" <7>x").append(input.getValue());
            out.append('\n');
        }
        if (recipe.coins() > 0) out.append("<6>").append(NumberFormat.getIntegerInstance().format(recipe.coins())).append(" Coins\n");
        return out.toString();
    }

    public static ItemStack.Builder icon(ForgeRecipe recipe) {
        ItemType output = recipe.output();
        if (output == null) return ItemStacks.item(net.minestom.server.item.Material.BARRIER, "<c>" + recipe.name());
        return new NonPlayerItemUpdater(recipe.outputItem()).getUpdatedItem();
    }

    public static ItemStack.Builder recipe(SkyBlockPlayer player, ForgeRecipe recipe, String footer) {
        long duration = ForgeService.durationMillis(player, recipe);
        StringBuilder lore = new StringBuilder("\n<e>Items Required\n").append(ingredients(recipe))
                .append("\n<7>Duration: <b>").append(duration(duration));
        if (HotmService.data(player).getTier() < recipe.hotm()) {
            lore.append("\n\n<c>Requires Heart of the Mountain Tier ").append(recipe.hotm()).append('.');
        }
        if (recipe.hasRequirement()) lore.append("\n<c>Requires ").append(recipe.requirement()).append('.');
        if (!recipe.available()) lore.append("\n\n<8>Not available on this server yet.");
        if (footer != null) lore.append("\n\n").append(footer);
        return ItemStacks.appendLore(icon(recipe), lore.toString());
    }
}
