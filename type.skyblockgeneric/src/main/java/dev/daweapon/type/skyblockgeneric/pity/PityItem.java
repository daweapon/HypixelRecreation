package dev.daweapon.type.skyblockgeneric.pity;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public record PityItem(int slot, String key, Material icon, String title, List<String> lore) {
    public List<String> loreFor(PityCategoryId category, SkyBlockPlayer player) {
        List<String> lines = new ArrayList<>(lore);
        lines.add("");
        lines.addAll(PityMeters.of(category, key).lines(player));
        return lines;
    }
}
