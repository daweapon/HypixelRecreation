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
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetOptions;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetService;
import dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetTrigger;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.Arrays;
import java.util.List;

public class GUIAutopetRule extends StatelessView {
    private final AutopetDraft draft;

    GUIAutopetRule(AutopetDraft draft) {
        this.draft = draft;
    }

    public GUIAutopetRule(int slot) {
        this(new AutopetDraft(slot));
    }

    GUIAutopetRule(int slot, dev.daweapon.type.skyblockgeneric.user.pets.autopet.AutopetRule rule) {
        this(AutopetDraft.from(slot, rule));
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Autopet Rule", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Components.fill(layout);
        Components.close(layout, 31);
        layout.slot(30, ItemStacks.item(Material.ARROW, 1, "<a>Go Back\n<7>To Autopets"), (_, c) -> c.player().openView(new GUIAutopets()));

        AutopetTrigger trigger = draft.getTrigger();
        layout.slot(10, ItemStacks.item(trigger == null ? Material.REDSTONE_TORCH : trigger.icon(), 1,
                "<c>When\n<7>Current: " + (trigger == null ? "<8>not set" : "<f>" + trigger.label()) + "\n\n<e>Click to choose!"),
                (_, c) -> chooseTrigger((SkyBlockPlayer) c.player()));

        if (draft.needsParam()) {
            String param = draft.getParam();
            layout.slot(11, ItemStacks.item(Material.NAME_TAG, 1,
                    "<c>Which one?\n<7>Current: " + (param == null ? "<8>not set" : "<f>" + AutopetOptions.label(trigger.param(), param, player)) + "\n\n<e>Click to choose!"),
                    (_, c) -> chooseParam((SkyBlockPlayer) c.player()));
        }

        String pet = draft.getPetType();
        layout.slot(13, ItemStacks.item(Material.BONE, 1,
                "<a>Equip\n<7>Current: " + (pet == null ? "<8>not set" : "<f>" + StringUtility.toNormalCase(pet) + " <7>(" + StringUtility.toNormalCase(draft.getPetRarity()) + ")") + "\n\n<e>Click to choose!"),
                (_, c) -> c.player().openView(new AutopetPetChoiceView(draft, 0)));

        layout.slot(15, ItemStacks.item(Material.BARRIER, 1,
                "<c>Exceptions\n" + exceptionLines() + "\n<e>Click to edit!"),
                (_, c) -> c.player().openView(new GUIAutopetExceptions(draft)));

        if (draft.isComplete()) {
            layout.slot(22, ItemStacks.item(Material.LIME_DYE, 1, "<a>Save Rule\n<7>Rule slot #" + (draft.getSlot() + 1) + "\n\n<e>Click to save!"), (_, c) -> save((SkyBlockPlayer) c.player()));
        } else {
            layout.slot(22, ItemStacks.item(Material.GRAY_DYE, 1, "<7>Save Rule\n<c>Choose a trigger and a pet first."));
        }
        layout.slot(25, ItemStacks.item(Material.LAVA_BUCKET, 1, "<c>Delete Rule\n\n<e>Click to delete!"), (_, c) -> delete((SkyBlockPlayer) c.player()));
    }

    private String exceptionLines() {
        if (draft.getExceptions().isEmpty()) return "<7>None";
        StringBuilder lines = new StringBuilder();
        draft.getExceptions().forEach(condition -> lines.append("<7>- ").append(condition.exception().label())
                .append(condition.param() == null ? "" : " <f>" + StringUtility.toNormalCase(condition.param())).append("\n"));
        return lines.toString();
    }

    private void chooseTrigger(SkyBlockPlayer player) {
        List<AutopetChoiceView.Choice> choices = Arrays.stream(AutopetTrigger.values())
                .map(trigger -> new AutopetChoiceView.Choice(trigger.name(), trigger.label(), trigger.icon(), trigger.unavailable())).toList();
        player.openView(new AutopetChoiceView("Select Trigger", choices, (p, id) -> {
            AutopetTrigger picked = AutopetTrigger.valueOf(id);
            if (picked != draft.getTrigger()) draft.setParam(null);
            draft.setTrigger(picked);
            p.openView(new GUIAutopetRule(draft));
        }, () -> player.openView(new GUIAutopetRule(draft)), 0));
    }

    private void chooseParam(SkyBlockPlayer player) {
        List<AutopetChoiceView.Choice> choices = AutopetOptions.of(draft.getTrigger().param(), player).stream()
                .map(option -> new AutopetChoiceView.Choice(option.id(), option.label(), option.icon(), null)).toList();
        player.openView(new AutopetChoiceView("Select Option", choices, (p, id) -> {
            draft.setParam(id);
            p.openView(new GUIAutopetRule(draft));
        }, () -> player.openView(new GUIAutopetRule(draft)), 0));
    }

    private void save(SkyBlockPlayer player) {
        DatapointAutopets.AutopetData data = AutopetService.data(player);
        data.setRule(draft.getSlot(), draft.build());
        AutopetService.save(player, data);
        player.sendMessage("<a>Saved Autopet rule #{}.", draft.getSlot() + 1);
        player.openView(new GUIAutopets());
    }

    private void delete(SkyBlockPlayer player) {
        DatapointAutopets.AutopetData data = AutopetService.data(player);
        data.setRule(draft.getSlot(), null);
        AutopetService.save(player, data);
        player.sendMessage("<c>Deleted Autopet rule #{}.", draft.getSlot() + 1);
        player.openView(new GUIAutopets());
    }
}
