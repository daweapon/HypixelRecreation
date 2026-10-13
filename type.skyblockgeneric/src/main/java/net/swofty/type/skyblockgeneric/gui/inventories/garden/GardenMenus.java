package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.GardenConfig;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class GardenMenus {
    static final int BAR_WIDTH = 20;

    private GardenMenus() {
    }

    static Text blank() {
        return Text.literal(" ");
    }

    static ItemStack.Builder item(Material material, Text name, List<Text> lore) {
        return ItemStacks.item(material, name, List.copyOf(lore));
    }

    static List<Text> lines(String prefix, int count) {
        List<Text> lines = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            lines.add(Text.key(prefix + i));
        }
        return lines;
    }

    static void open(ViewContext ctx, View<?> view) {
        ctx.push(view);
    }

    static void notAdded(ViewContext ctx, String label) {
        ctx.player().sendMessage(Text.key("gui_garden_desk.not_added", label));
    }

    static Material icon(GardenCrop crop) {
        return switch (crop) {
            case WHEAT -> Material.WHEAT;
            case CARROT -> Material.CARROT;
            case POTATO -> Material.POTATO;
            case PUMPKIN -> Material.PUMPKIN;
            case SUGAR_CANE -> Material.SUGAR_CANE;
            case MELON -> Material.MELON_SLICE;
            case CACTUS -> Material.CACTUS;
            case COCOA_BEANS -> Material.COCOA_BEANS;
            case MUSHROOM -> Material.RED_MUSHROOM;
            case NETHER_WART -> Material.NETHER_WART;
            case SUNFLOWER -> Material.SUNFLOWER;
            case MOONFLOWER -> Material.BLUE_ORCHID;
            case WILD_ROSE -> Material.ROSE_BUSH;
        };
    }

    static int unlockLevel(GardenCrop crop) {
        return GardenConfig.get().levels().stream()
                .filter(level -> level.crops().contains(crop))
                .mapToInt(GardenConfig.Level::level)
                .findFirst()
                .orElse(1);
    }

    static String percent(long current, long target) {
        if (target <= 0 || current <= 0) {
            return "0";
        }
        double value = Math.min(100D, Math.floor(current * 1000D / target) / 10D);
        String text = String.format(Locale.ROOT, "%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    static int filled(long current, long target, int width) {
        if (target <= 0 || current <= 0) {
            return 0;
        }
        return (int) Math.min(width, current * width / target);
    }

    static String spaces(int count) {
        return " ".repeat(Math.max(0, count));
    }

    static String number(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }
}
