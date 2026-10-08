package net.swofty.type.crimsonisle.gui.town;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonAbiphone;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonContact;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Ziri's menu: the Crimson Isle quest contacts (Elle, the Dean, Captain Ahone and Igrupan) the player
 * has earned but no longer has on their Abiphone, each restorable with a click.
 */
public class GUIZiri extends StatelessView {
    public static final List<String> RESTORABLE = List.of("elle", "dean", "captain_ahone", "igrupan");
    private static final int[] SLOTS = {20, 21, 22, 23, 24};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Ziri", InventoryType.CHEST_6_ROW);
    }

    public static List<CrimsonContact> restorable(SkyBlockPlayer player) {
        List<CrimsonContact> contacts = new ArrayList<>();
        if (!CrimsonAbiphone.hasAbiphone(player)) return contacts;
        for (String id : RESTORABLE) {
            if (AbiphoneRegistry.getFromId(id) instanceof CrimsonContact contact
                    && contact.isEarned(player) && !CrimsonAbiphone.hasContact(player, id)) {
                contacts.add(contact);
            }
        }
        return contacts;
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        List<CrimsonContact> contacts = restorable(player);
        if (contacts.isEmpty()) {
            layout.slot(22, ItemStacks.item(Material.EGG, """
                    <c>Nobody available!
                    <7>There is no contact which you
                    <7>can add back here."""));
            return;
        }
        int first = (SLOTS.length - contacts.size()) / 2;
        for (int i = 0; i < contacts.size(); i++) {
            CrimsonContact contact = contacts.get(i);
            layout.slot(SLOTS[first + i], (_, _) -> ItemStacks.lore(contact.getIcon(), """
                            <7>Add this contact back to your
                            <7>Abiphone.

                            <e>Click to add!""")
                            .customName(contact.getName().asComponent()),
                    (_, c) -> {
                        c.player().closeInventory();
                        CrimsonAbiphone.grant(player, contact.getId());
                    });
        }
    }
}
