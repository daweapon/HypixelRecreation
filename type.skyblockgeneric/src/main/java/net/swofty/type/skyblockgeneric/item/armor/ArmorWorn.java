package net.swofty.type.skyblockgeneric.item.armor;

import net.minestom.server.item.ItemStack;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public final class ArmorWorn {
    private ArmorWorn() {
    }

    public static @Nullable String idOf(ItemStack stack) {
        if (stack == null || stack.isAir()) return null;
        return new SkyBlockItem(stack).getAttributeHandler().getTypeAsString();
    }

    public static List<String> pieceIds(SkyBlockPlayer player) {
        return java.util.Arrays.asList(idOf(player.getHelmet()), idOf(player.getChestplate()),
                idOf(player.getLeggings()), idOf(player.getBoots()));
    }

    public static boolean helmetIs(SkyBlockPlayer player, String id) {
        return id.equals(idOf(player.getHelmet()));
    }

    public static int count(SkyBlockPlayer player, Set<String> ids) {
        int count = 0;
        for (String id : pieceIds(player)) if (id != null && ids.contains(id)) count++;
        return count;
    }

    public static int countPrefix(SkyBlockPlayer player, String prefix) {
        int count = 0;
        for (String id : pieceIds(player)) if (id != null && id.startsWith(prefix + "_")) count++;
        return count;
    }

    public static boolean fullSet(SkyBlockPlayer player, String prefix) {
        return idOf(player.getHelmet()) != null && (prefix + "_HELMET").equals(idOf(player.getHelmet()))
                && (prefix + "_CHESTPLATE").equals(idOf(player.getChestplate()))
                && (prefix + "_LEGGINGS").equals(idOf(player.getLeggings()))
                && (prefix + "_BOOTS").equals(idOf(player.getBoots()));
    }
}
