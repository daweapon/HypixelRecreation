package dev.daweapon.type.skyblockgeneric.entity.mob.island.dummy;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

public class TrainingDummyView extends StatelessView {
    private static final int[] FORM_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29};
    private static final int CLOSE_SLOT = 39;
    private static final int PICKUP_SLOT = 41;

    private final SkyBlockIsland island;
    private final TrainingDummyMob mob;

    public TrainingDummyView(SkyBlockIsland island, TrainingDummyMob mob) {
        this.island = island;
        this.mob = mob;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Training Dummy Settings", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        TrainingDummyForm[] forms = TrainingDummyForm.values();
        for (int i = 0; i < forms.length; i++) {
            TrainingDummyForm form = forms[i];
            boolean current = form == mob.placed().form();
            layout.slot(FORM_SLOTS[i], (_, _) -> ItemStacks.item(form.icon(), """
                    <a>%s
                    <7>Mob Types:
                    %s

                    %s""".formatted(form.label(), mobTypeLines(form), current ? "<a>SELECTED!" : "<e>Click to change!")),
                    (_, c) -> {
                        c.player().closeInventory();
                        if (!current && !mob.isRemoved()) TrainingDummies.changeForm(island, mob, form);
                    });
        }
        layout.slot(PICKUP_SLOT, (_, _) -> ItemStacks.item(Material.BEDROCK, """
                <a>Pickup Training Dummy
                <e>Click to pickup!"""), (_, c) -> {
            c.player().closeInventory();
            if (!mob.isRemoved()) TrainingDummies.pickUp(island, mob, (SkyBlockPlayer) c.player());
        });
        Components.close(layout, CLOSE_SLOT);
    }

    private static String mobTypeLines(TrainingDummyForm form) {
        return form.mobTypes().stream()
                .map(type -> type.getColor() + type.getSymbol() + " " + type.getDisplayName())
                .reduce((a, b) -> a + "\n" + b).orElse("");
    }
}
