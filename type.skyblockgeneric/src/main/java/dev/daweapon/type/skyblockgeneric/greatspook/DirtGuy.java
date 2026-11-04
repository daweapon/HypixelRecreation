package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.reforge.Reforge;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public final class DirtGuy {
    public static final String SPEAKER = "<e>[NPC] <f>Dirt Guy<f>: ";
    private static final String DIRTY_REFORGE = "Dirty";

    private DirtGuy() {
    }

    public static void interact(SkyBlockPlayer player) {
        player.playSound(Sound.sound(Key.key("block.gravel.place"), Sound.Source.NEUTRAL, 1f, 1f));
        ItemStack held = player.getItemInMainHand();
        if (held.isAir()) {
            say(player, "Me like dirt.");
            return;
        }
        if (held.material() == Material.DIRT) {
            say(player, "Dirt!");
            return;
        }
        if (!SkyBlockItem.isSkyBlockItem(held)) {
            say(player, "Me like dirt.");
            return;
        }
        SkyBlockItem item = new SkyBlockItem(held);
        if (item.getAttributeHandler().getPotentialType() == ItemType.INFINIDIRT_WAND) {
            say(player, "DIRT!");
            if (!GreatSpookEvent.isActive()) return;
            player.setItemInMainHand(ItemStack.AIR);
            player.addAndUpdateItem(new SkyBlockItem(ItemType.PRECIOUS));
            say(player, "PRECIOUS!");
            player.sendMessage("<a>You were given a <9>Precious <a>in exchange for your <a>InfiniDirt™ Wand<a>!");
            return;
        }
        Reforge reforge = item.getAttributeHandler().getReforge();
        if (reforge != null && DIRTY_REFORGE.equalsIgnoreCase(reforge.getName())) {
            say(player, "Dirty!");
            return;
        }
        say(player, "Me like dirt.");
    }

    private static void say(SkyBlockPlayer player, String line) {
        player.sendMessage(SPEAKER + line);
    }
}
