package dev.daweapon.type.skyblockgeneric.gui.inventories.autopet;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetException;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetOptions;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetParam;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetRule;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.Arrays;
import java.util.List;

class GUIAutopetExceptions extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14};

    private final AutopetDraft draft;

    GUIAutopetExceptions(AutopetDraft draft) {
        this.draft = draft;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Autopet Exceptions", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Components.fill(layout);
        Components.close(layout, 31);
        layout.slot(30, ItemStacks.item(Material.ARROW, 1, "<a>Go Back\n<7>To the rule"), (_, c) -> c.player().openView(new GUIAutopetRule(draft)));
        List<AutopetRule.Condition> exceptions = draft.getExceptions();
        for (int i = 0; i < exceptions.size(); i++) {
            AutopetRule.Condition condition = exceptions.get(i);
            String detail = condition.param() == null ? "" : "\n<7>" + AutopetOptions.label(condition.exception().param(), condition.param(), player);
            layout.slot(SLOTS[i], ItemStacks.item(condition.exception().icon(), 1, "<c>" + condition.exception().label() + detail + "\n\n<e>Click to remove!"),
                    (_, c) -> {
                        draft.getExceptions().remove(condition);
                        c.player().openView(new GUIAutopetExceptions(draft));
                    });
        }
        if (exceptions.size() < AutopetRule.MAX_EXCEPTIONS) {
            layout.slot(SLOTS[exceptions.size()], ItemStacks.item(Material.LIME_STAINED_GLASS_PANE, 1,
                    "<a>Add Exception\n<7>" + exceptions.size() + "/" + AutopetRule.MAX_EXCEPTIONS + " used\n\n<e>Click to add!"),
                    (_, c) -> chooseException((SkyBlockPlayer) c.player()));
        }
    }

    private void chooseException(SkyBlockPlayer player) {
        List<AutopetChoiceView.Choice> choices = Arrays.stream(AutopetException.values())
                .map(exception -> new AutopetChoiceView.Choice(exception.name(), exception.label(), exception.icon(), exception.unavailable())).toList();
        player.openView(new AutopetChoiceView("Select Exception", choices, (p, id) -> {
            AutopetException exception = AutopetException.valueOf(id);
            if (exception.param() == AutopetParam.NONE) {
                draft.getExceptions().add(new AutopetRule.Condition(exception, null));
                p.openView(new GUIAutopetExceptions(draft));
                return;
            }
            List<AutopetChoiceView.Choice> options = AutopetOptions.of(exception.param(), p).stream()
                    .map(option -> new AutopetChoiceView.Choice(option.id(), option.label(), option.icon(), null)).toList();
            p.openView(new AutopetChoiceView("Select " + StringUtility.toNormalCase(exception.param().name()), options, (picker, value) -> {
                draft.getExceptions().add(new AutopetRule.Condition(exception, value));
                picker.openView(new GUIAutopetExceptions(draft));
            }, () -> p.openView(new GUIAutopetExceptions(draft)), 0));
        }, () -> player.openView(new GUIAutopetExceptions(draft)), 0));
    }
}
