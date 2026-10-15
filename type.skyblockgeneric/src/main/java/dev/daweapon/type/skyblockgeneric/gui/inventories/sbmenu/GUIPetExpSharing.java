package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetData;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetExperience;

import java.util.ArrayList;
import java.util.List;

// Hypixel's Exp Sharing menu: three slots whose pets earn a share of the summoned pet's Pet Exp
public class GUIPetExpSharing extends StatelessView {
    private static final int[] STATUS_SLOTS = {21, 22, 23};
    private static final int[] PET_SLOTS = {30, 31, 32};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.pets.exp_sharing.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<Text> lore = new ArrayList<>(Text.keyLines("gui_sbmenu.pets.exp_sharing.info.lore"));
            lore.add(Text.key("gui_sbmenu.pets.exp_sharing.rate", formatRate(PetExperience.shareRate(player))));
            return ItemStacks.item(Material.YELLOW_DYE, 1, Text.key("gui_sbmenu.pets.exp_sharing"), lore);
        });

        for (int i = 0; i < DatapointPetData.EXP_SHARE_SLOTS; i++) {
            int slot = i;
            layout.slot(STATUS_SLOTS[i], (s, c) -> statusPane((SkyBlockPlayer) c.player(), slot));
            layout.slot(PET_SLOTS[i], (s, c) -> petSlot((SkyBlockPlayer) c.player(), slot), (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                SkyBlockItem current = player.getPetData().getExpSharePet(slot);
                if (current != null) {
                    player.getPetData().setExpSharePet(slot, null);
                    player.sendMessage(Text.key("gui_sbmenu.pets.exp_sharing.msg.removed", current.getDisplayName(), slot + 1));
                    c.session(DefaultState.class).refresh();
                    return;
                }
                if (!PetExperience.isSlotActive(slot)) {
                    player.sendMessage(Text.key("gui_sbmenu.pets.exp_sharing.msg.disabled"));
                    return;
                }
                c.push(new GUIPetExpShareSelect(slot), GUIPetExpShareSelect.createInitialState(player));
            });
        }
    }

    private static ItemStack.Builder statusPane(SkyBlockPlayer player, int slot) {
        DatapointPetData.UserPetData petData = player.getPetData();
        List<Text> lore = new ArrayList<>(Text.keyLines("gui_sbmenu.pets.exp_sharing.slot.lore"));
        Material material;
        Text title;
        if (!PetExperience.isSlotActive(slot)) {
            material = Material.RED_STAINED_GLASS_PANE;
            title = Text.key("gui_sbmenu.pets.exp_sharing.slot.inactive_title", slot + 1);
            lore.add(Text.key("gui_sbmenu.pets.exp_sharing.slot.disabled"));
            lore.add(Text.key("gui_sbmenu.pets.exp_sharing.slot.diana"));
        } else if (petData.getExpSharePet(slot) != null && petData.getEnabledPet() != null) {
            material = Material.LIME_STAINED_GLASS_PANE;
            title = Text.key("gui_sbmenu.pets.exp_sharing.slot.active_title", slot + 1);
            lore.add(Text.key("gui_sbmenu.pets.exp_sharing.slot.active"));
        } else {
            material = Material.RED_STAINED_GLASS_PANE;
            title = Text.key("gui_sbmenu.pets.exp_sharing.slot.inactive_title", slot + 1);
            lore.add(Text.key("gui_sbmenu.pets.exp_sharing.slot.inactive"));
        }
        return ItemStacks.item(material, 1, title, lore);
    }

    private static ItemStack.Builder petSlot(SkyBlockPlayer player, int slot) {
        SkyBlockItem pet = player.getPetData().getExpSharePet(slot);
        if (pet == null) {
            return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1,
                    Text.key("gui_sbmenu.pets.exp_sharing.empty"),
                    Text.keyLines("gui_sbmenu.pets.exp_sharing.empty.lore"));
        }
        ItemStack.Builder item = new NonPlayerItemUpdater(pet).getUpdatedItem();
        List<Text> lore = ItemStacks.loreText(item);
        lore.add(Text.literal(" "));
        lore.add(Text.key("gui_sbmenu.pets.exp_sharing.click_to_remove"));
        return ItemStacks.lore(item, lore);
    }

    private static String formatRate(double rate) {
        return String.format("%.1f%%", rate * 100);
    }
}
