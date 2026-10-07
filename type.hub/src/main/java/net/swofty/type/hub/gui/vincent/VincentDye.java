package net.swofty.type.hub.gui.vincent;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.hub.util.UnreleasedContent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record VincentDye(String id, String name, char colour, @Nullable String hex, String kind, boolean droppable, List<String> source) {
    private static final Map<Character, Material> MATERIALS = Map.ofEntries(
            Map.entry('0', Material.BLACK_DYE), Map.entry('1', Material.BLUE_DYE), Map.entry('2', Material.GREEN_DYE),
            Map.entry('3', Material.CYAN_DYE), Map.entry('4', Material.RED_DYE), Map.entry('5', Material.PURPLE_DYE),
            Map.entry('6', Material.ORANGE_DYE), Map.entry('7', Material.LIGHT_GRAY_DYE), Map.entry('8', Material.GRAY_DYE),
            Map.entry('9', Material.BLUE_DYE), Map.entry('a', Material.LIME_DYE), Map.entry('b', Material.LIGHT_BLUE_DYE),
            Map.entry('c', Material.RED_DYE), Map.entry('d', Material.MAGENTA_DYE), Map.entry('e', Material.YELLOW_DYE),
            Map.entry('f', Material.WHITE_DYE));

    public ItemStack.Builder display(@Nullable Text boostLine) {
        List<Text> lore = new ArrayList<>();
        lore.add(switch (kind) {
            case "ANIMATED" -> Text.of("<8>Animated Dye");
            case "PARTICLE" -> Text.of("<8>Particle Dye");
            default -> Text.of("<8>Hex #{}", hex);
        });
        lore.add(Text.empty());
        source.forEach(line -> lore.add(Text.of(line)));
        if (boostLine != null) {
            lore.add(Text.empty());
            lore.addAll(boostLine.lines());
        }
        lore.addAll(UnreleasedContent.DYES.loreLines());
        return ItemStacks.item(MATERIALS.getOrDefault(colour, Material.WHITE_DYE), Text.of("<" + colour + ">{}", name), lore);
    }
}
