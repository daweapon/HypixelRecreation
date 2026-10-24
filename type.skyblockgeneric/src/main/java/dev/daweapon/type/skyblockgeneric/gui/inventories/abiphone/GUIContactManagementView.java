package dev.daweapon.type.skyblockgeneric.gui.inventories.abiphone;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneItemSettings;
import java.util.List;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;


// GUI for managing a single Abiphone contact, offering removal and navigation back to the contact list
public final class GUIContactManagementView implements View<GUIContactManagementView.State> {

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.translatable("gui_abiphone.management.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(4, (s, c) -> ItemStacks.lore(
                ItemStacks.name(s.npc().getIcon(), "<f>{}", s.npc().getName()),
                "<7>{}", s.npc().getDescription()
        ));

        if (AbiphoneItemSettings.supportsDoNotDisturb(state.abiphone())) {
            layout.slot(29, (s, c) -> doNotDisturbButton(AbiphoneItemSettings.isDoNotDisturb(s.abiphone(), s.npc())),
                    (click, viewCtx) -> {
                        AbiphoneItemSettings.toggleDoNotDisturb(state.abiphone(), state.npc());
                        viewCtx.session(Object.class).updateUnchecked(o -> o);
                    });
        }

        layout.slot(31, (s, c) -> ItemStacks.item(
                Material.FEATHER,
                1,
                Text.key("gui_abiphone.management.remove_contact"),
                Text.keyLines("gui_abiphone.management.remove_contact.lore")
        ), (click, viewCtx) -> {
            state.abiphone().getAttributeHandler().removeAbiphoneNPC(state.npc());
            viewCtx.player().sendMessage(Text.key("gui_abiphone.management.removed_message", state.npc().getName()));

            if (!viewCtx.pop()) {
                viewCtx.player().closeInventory();
            }
        });

        if (!Components.back(layout, 48, ctx)) {
            layout.slot(48, (s, c) -> Components.BACK_BUTTON, (click, viewCtx) -> {
                viewCtx.push(new AbiphoneView(), new AbiphoneView.State(state.abiphone()));
            });
        }

        Components.close(layout, 49);
    }

    private static ItemStack.Builder doNotDisturbButton(boolean enabled) {
        return ItemStacks.lore(ItemStacks.name(ItemStack.builder(enabled ? Material.RED_DYE : Material.LIME_DYE), "<c>Do Not Disturb"), List.of(
                Text.of("<7>Silences this contact so they cannot"),
                Text.of("<7>call you."),
                Text.empty(),
                Text.of("<7>Status: {}", enabled ? "<c>Enabled" : "<a>Disabled"),
                Text.empty(),
                Text.of("<e>Click to toggle!")));
    }

    public record State(SkyBlockItem abiphone, AbiphoneNPC npc) {}
}

