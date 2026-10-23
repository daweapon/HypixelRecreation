package dev.daweapon.type.skyblockgeneric.gui.inventories.autopet;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAutopets;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.GUIPets;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetOptions;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetRule;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetService;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;

public class GUIAutopets extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Autopets", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointAutopets.AutopetData data = AutopetService.data(player);
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(48, ItemStacks.item(Material.ARROW, 1, "<a>Go Back\n<7>To Pets"), (_, c) -> c.player().openView(new GUIPets()));
        layout.slot(4, ItemStacks.item(Material.BOOK, 1, "<6>Autopets\n<7>Equip a pet automatically when something\n<7>happens, like gaining skill XP.\n\n<7>Rule slots: <a>" + data.unlockedSlots() + "<7>/<a>" + DatapointAutopets.MAX_RULES
                + "\n<8>Consume an Autopet Rules 2-Pack for 2 more."));

        for (int slot = 0; slot < SLOTS.length; slot++) {
            int index = slot;
            if (slot >= data.unlockedSlots()) {
                layout.slot(SLOTS[slot], ItemStacks.item(Material.RED_STAINED_GLASS_PANE, 1,
                        "<c>Locked Rule Slot\n<7>Consume an <5>Autopet Rules 2-Pack\n<7>to unlock this slot."));
                continue;
            }
            AutopetRule rule = data.rule(slot);
            if (rule == null) {
                layout.slot(SLOTS[slot], ItemStacks.item(Material.LIME_STAINED_GLASS_PANE, 1,
                        "<a>Create Rule #" + (slot + 1) + "\n\n<e>Click to create!"),
                        (_, c) -> c.player().openView(new GUIAutopetRule(index)));
                continue;
            }
            layout.slot(SLOTS[slot], ItemStacks.item(rule.trigger().icon(), 1, describe(rule, index, player)), (click, c) -> {
                if (click.click() instanceof Click.RightShift) {
                    AutopetService.data((SkyBlockPlayer) c.player()).setRule(index, null);
                    AutopetService.save((SkyBlockPlayer) c.player(), AutopetService.data((SkyBlockPlayer) c.player()));
                    c.player().openView(new GUIAutopets());
                    return;
                }
                c.player().openView(new GUIAutopetRule(index, rule));
            });
        }
    }

    private static String describe(AutopetRule rule, int index, SkyBlockPlayer player) {
        StringBuilder text = new StringBuilder("<a>Rule #" + (index + 1) + "\n<7>When: <f>" + rule.trigger().label());
        if (rule.param() != null) text.append(" <e>").append(AutopetOptions.label(rule.trigger().param(), rule.param(), player));
        text.append("\n<7>Equip: <f>").append(StringUtility.toNormalCase(rule.petType())).append(" <7>(").append(StringUtility.toNormalCase(rule.petRarity())).append(")");
        if (!rule.exceptions().isEmpty()) {
            text.append("\n<7>Except:");
            rule.exceptions().forEach(condition -> text.append("\n<c> - ").append(condition.exception().label())
                    .append(condition.param() == null ? "" : " " + AutopetOptions.label(condition.exception().param(), condition.param(), player)));
        }
        return text.append("\n\n<e>Click to edit!\n<e>Shift right-click to delete!").toString();
    }
}
