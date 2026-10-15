package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.skyblockgeneric.item.ItemAttributeHandler;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;

public final class AbiphoneContactsTrio {
    public static final String HANDLER_ID = "ABIPHONE_CONTACTS_TRIO";
    public static final int SLOTS_PER_USE = 3;

    private AbiphoneContactsTrio() {
    }

    public static void registerAnvilCombine() {
        AnvilCombineRegistry.register(HANDLER_ID, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> {
                    ItemAttributeHandler handler = upgradeItem.getAttributeHandler();
                    handler.setAbiphoneExtraContacts(handler.getAbiphoneExtraContacts() + SLOTS_PER_USE);
                },
                (player, upgradeItem, sacrificeItem) -> upgradeItem.hasComponent(AbiphoneComponent.class)
                        && upgradeItem.getAttributeHandler().getAbiphoneExtraContacts() + SLOTS_PER_USE <= AbiphoneComponent.MAX_CONTACT_ADDONS,
                (upgradeItem, sacrificeItem, player) -> 0,
                (player, upgradeItem, sacrificeItem) -> {
                    player.sendMessage("<a>You've elevated your Abiphone contacts limit by <b>+" + SLOTS_PER_USE + "<a>!");
                    return true;
                }));
    }
}
