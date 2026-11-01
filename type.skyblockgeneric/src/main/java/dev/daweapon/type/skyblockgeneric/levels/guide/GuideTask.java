package dev.daweapon.type.skyblockgeneric.levels.guide;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public record GuideTask(String name, String task, String iconFile, GuideKind kind, String description, int xp,
                        List<GuideEntry> entries) {
    private static final String ACCESSORY_BAG_HEAD = "1a11a7f11bcd5784903c5201d08261c4df8379109d6e611c1cd3ededf031afed";

    public long completed(SkyBlockPlayer player) {
        return entries.stream().filter(entry -> entry.complete(player)).count();
    }

    public double progress(SkyBlockPlayer player) {
        return entries.isEmpty() ? 0 : completed(player) / (double) entries.size();
    }

    public long unavailable() {
        return entries.stream().filter(entry -> !entry.available()).count();
    }

    public ItemStack.Builder icon() {
        if (kind == GuideKind.ACCESSORY) return ItemStacks.head(ACCESSORY_BAG_HEAD, Text.empty(), List.of());
        Material material = Material.fromKey("minecraft:" + iconFile.toLowerCase().replace(' ', '_').replace("'", ""));
        return ItemStack.builder(material != null ? material : kind.fallbackIcon());
    }
}
