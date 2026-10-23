package dev.daweapon.type.skyblockgeneric.gui.inventories.autopet;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.Comparator;
import java.util.List;

class AutopetPetChoiceView extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    private final AutopetDraft draft;
    private final int page;

    AutopetPetChoiceView(AutopetDraft draft, int page) {
        this.draft = draft;
        this.page = page;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Select Pet to Equip", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(48, ItemStacks.item(Material.ARROW, 1, "<a>Go Back"), (_, c) -> c.player().openView(new GUIAutopetRule(draft)));
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        List<SkyBlockItem> pets = player.getPetData().getPetsMap().keySet().stream()
                .sorted(Comparator.comparingInt((SkyBlockItem pet) -> pet.getAttributeHandler().getRarity().ordinal()).reversed()
                        .thenComparing(Comparator.comparingInt(PetLevels::level).reversed()))
                .toList();
        int start = page * SLOTS.length;
        for (int i = 0; i < SLOTS.length && start + i < pets.size(); i++) {
            SkyBlockItem pet = pets.get(start + i);
            layout.slot(SLOTS[i], (_, c) -> PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(), pet.getItemStack()),
                    (_, c) -> {
                        draft.setPetType(pet.getAttributeHandler().getPotentialType().name());
                        draft.setPetRarity(pet.getAttributeHandler().getRarity().name());
                        c.player().openView(new GUIAutopetRule(draft));
                    });
        }
        if (page > 0) layout.slot(45, ItemStacks.item(Material.ARROW, 1, "<a>Previous Page"), (_, c) -> c.player().openView(new AutopetPetChoiceView(draft, page - 1)));
        if (start + SLOTS.length < pets.size()) layout.slot(53, ItemStacks.item(Material.ARROW, 1, "<a>Next Page"), (_, c) -> c.player().openView(new AutopetPetChoiceView(draft, page + 1)));
    }
}
