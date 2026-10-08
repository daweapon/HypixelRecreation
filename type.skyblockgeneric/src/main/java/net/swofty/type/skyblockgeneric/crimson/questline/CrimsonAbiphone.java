package net.swofty.type.skyblockgeneric.crimson.questline;

import net.minestom.server.item.ItemStack;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneNPC;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AbiphoneComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** Quest-given Abiphone contacts, which go straight onto the first Abiphone in the player's inventory. */
public final class CrimsonAbiphone {
    private CrimsonAbiphone() {
    }

    public static int abiphoneSlot(SkyBlockPlayer player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
            if (new SkyBlockItem(stack).hasComponent(AbiphoneComponent.class)) return slot;
        }
        return -1;
    }

    public static boolean hasAbiphone(SkyBlockPlayer player) {
        return abiphoneSlot(player) >= 0;
    }

    public static boolean hasContact(SkyBlockPlayer player, String contactId) {
        int slot = abiphoneSlot(player);
        AbiphoneNPC contact = AbiphoneRegistry.getFromId(contactId);
        if (slot < 0 || contact == null) return false;
        return new SkyBlockItem(player.getInventory().getItemStack(slot)).getAttributeHandler().hasAbiphoneNPC(contact);
    }

    /** Adds the contact with the standard "has been added" line. Returns false without an Abiphone. */
    public static boolean grant(SkyBlockPlayer player, String contactId) {
        int slot = abiphoneSlot(player);
        AbiphoneNPC contact = AbiphoneRegistry.getFromId(contactId);
        if (slot < 0 || contact == null || hasContact(player, contactId)) return false;
        player.updateItemInSlot(slot, item -> item.getAttributeHandler().addAbiphoneNPC(contact));
        player.sendMessage("<b>✆ {} <f>has been added to your Abiphone's contacts!", contact.getName());
        return true;
    }
}
