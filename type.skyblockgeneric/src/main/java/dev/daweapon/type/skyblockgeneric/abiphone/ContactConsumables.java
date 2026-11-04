package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonAbiphone;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

public final class ContactConsumables {
    private static final Map<String, String> CONTACT_BY_ITEM = Map.of(
            "TEDS_CONTACT_BAKED_IN_BREAD", "feast_chef_ted",
            "SCOTTS_CONTACT_BAKED_IN_BREAD", "feast_baker_scott",
            "SIRIUS_PERSONAL_PHONE_NUMBER", "sirius");

    private ContactConsumables() {
    }

    public static void registerInteractables() {
        CONTACT_BY_ITEM.forEach((item, contact) -> InteractableRegistry.register(item, InteractableItemConfig.builder()
                .rightClickHandler((player, held) -> consume(player, held, contact)).build()));
    }

    private static void consume(SkyBlockPlayer player, SkyBlockItem held, String contactId) {
        AbiphoneNPC npc = AbiphoneRegistry.getFromId(contactId);
        if (!(npc instanceof ConfiguredContact contact)) return;
        int slot = CrimsonAbiphone.abiphoneSlot(player);
        if (slot < 0) {
            player.sendMessage("<c>You need an Abiphone in your inventory to add this contact!");
            return;
        }
        SkyBlockItem phone = new SkyBlockItem(player.getInventory().getItemStack(slot));
        if (phone.getAttributeHandler().hasAbiphoneNPC(npc)) {
            player.sendMessage("<7>[<b>✆<7>] This NPC is already in your contacts list!");
            return;
        }
        if (phone.getAttributeHandler().getAbiphoneNPCs().size() >= phone.getComponent(AbiphoneComponent.class).contactLimit(player, phone)) {
            player.sendMessage("<7>[<b>✆<7>] <c>Your Abiphone has no free contact slots! Use an <9>Abiphone Contacts Trio <c>to add more.");
            return;
        }
        TimedConsumables.consumeHeld(player, held);
        contact.grantDirectly(player, slot);
    }
}
