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
        return output == null ? "<7>" + recipe.name() : colorTag(output) + new SkyBlockItem(output).getDisplayName();
    }

    public static String ingredients(SkyBlockPlayer player, ForgeRecipe recipe) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, Integer> input : recipe.inputs().entrySet()) {
            ItemType type = ItemType.get(input.getKey());
            if (type == null) {
                out.append("<8>").append(input.getKey().toLowerCase(Locale.ROOT).replace('_', ' ')).append(" <8>x").append(input.getValue()).append('\n');
                continue;
            }
            int have = player.getAmountInInventory(type) + player.getSackItems().getAmount(type);
            out.append(colorTag(type)).append(new SkyBlockItem(type).getDisplayName()).append(" <8>x").append(input.getValue())
                    .append(have >= input.getValue() ? " <a>✔" : " <c>✘").append('\n');
        }
        if (recipe.coins() > 0) {
            boolean enough = player.getCoins() >= recipe.coins();
            out.append("<6>").append(NumberFormat.getIntegerInstance().format(recipe.coins())).append(" Coins").append(enough ? " <a>✔" : " <c>✘").append('\n');
        }
        return out.toString();
    }

    public static ItemStack.Builder icon(ForgeRecipe recipe) {
        ItemType output = recipe.output();
        if (output == null) return ItemStacks.item(net.minestom.server.item.Material.BARRIER, "<c>" + recipe.name());
        return new NonPlayerItemUpdater(new SkyBlockItem(output, recipe.amount())).getUpdatedItem();
    }

    public static ItemStack.Builder recipe(SkyBlockPlayer player, ForgeRecipe recipe, String footer) {
        long duration = ForgeService.durationMillis(player, recipe);
        StringBuilder lore = new StringBuilder("\n<7>Duration: <a>").append(duration(duration)).append("\n\n<7>Requires:\n")
                .append(ingredients(player, recipe));
        if (recipe.hasRequirement()) lore.append("<7>Requires: <c>").append(recipe.requirement()).append('\n');
        lore.append("<7>Heart of the Mountain: ").append(HotmService.data(player).getTier() >= recipe.hotm() ? "<a>" : "<c>")
                .append("Tier ").append(recipe.hotm()).append('\n');
        if (!recipe.available()) lore.append("\n<8>Not available on this server yet.");
        if (footer != null) lore.append("\n\n").append(footer);
        return ItemStacks.appendLore(icon(recipe), lore.toString());
    }
}
