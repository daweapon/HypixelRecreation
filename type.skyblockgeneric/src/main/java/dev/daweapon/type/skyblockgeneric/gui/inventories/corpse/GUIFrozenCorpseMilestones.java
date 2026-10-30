package dev.daweapon.type.skyblockgeneric.gui.inventories.corpse;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseMilestone;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseMilestones;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseProgress;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseRngMeter;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class GUIFrozenCorpseMilestones extends StatelessView {
    private static final int FIRST_MILESTONE_SLOT = 19;
    private static final int BAR_LENGTH = 24;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Frozen Corpse Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(4, (_, _) -> ItemStacks.item(Material.MAP, Text.of("<b>Frozen Corpse Milestones"), List.of(
                Text.of("<7>Loot different types of <b>Frozen"),
                Text.of("<b>Corpses <7>while in the <b>Glacite"),
                Text.of("<b>Mineshafts <7>to progress through the"),
                Text.of("<7>milestones!"),
                Text.empty(),
                Text.of("<7>Upon reaching a new milestone,"),
                Text.of("<7>unlock exclusive <6>rewards<7>!"))));
        List<CorpseMilestone> milestones = CorpseMilestones.all();
        for (int i = 0; i < milestones.size(); i++) {
            CorpseMilestone milestone = milestones.get(i);
            layout.slot(FIRST_MILESTONE_SLOT + i, (_, c) -> milestoneItem((SkyBlockPlayer) c.player(), milestone));
        }
        layout.slot(50, (_, c) -> rngMeterItem((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUIFrozenCorpseRngMeter()));
    }

    private ItemStack.Builder milestoneItem(SkyBlockPlayer player, CorpseMilestone milestone) {
        int claimed = CorpseMilestones.claimed(player);
        boolean done = milestone.tier() <= claimed;
        boolean next = milestone.tier() == claimed + 1;
        Material pane = done ? Material.LIME_STAINED_GLASS_PANE : next ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String colour = done ? "<a>" : next ? "<e>" : "<c>";
        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        for (CorpseType type : CorpseType.values()) {
            int required = milestone.required(type);
            if (required <= 0) continue;
            int looted = Math.min(required, CorpseProgress.lootedCount(player, type));
            lore.add(Text.of(type.coloured() + " <7>Progress: <e>" + (looted * 100 / required) + "<6>%"));
            lore.add(bar(looted, required));
            lore.add(Text.empty());
        }
        lore.add(Text.of("<7>Rewards:"));
        if (milestone.rewardItem() != null) lore.add(Text.of("  {}", new SkyBlockItem(milestone.rewardItem()).getDisplayNameText()));
        if (milestone.bonusCorpse() > 0) {
            lore.add(Text.of("  <8>+<f>Gain a <a>+{}% <f>chance to spawn <a>+1", (int) milestone.bonusCorpse()));
            lore.add(Text.of("  <b>Frozen Corpse <f>when entering a"));
            lore.add(Text.of("  <b>Glacite Mineshaft <f>you summoned."));
        }
        if (milestone.bonusRoll() > 0) {
            lore.add(Text.of("  <8>+<f>Gain a <a>+{}% <f>chance to get <a>+1 <f>item", (int) milestone.bonusRoll()));
            lore.add(Text.of("  <f>when looting a <b>Frozen Corpse<f>."));
        }
        lore.add(Text.of("  <8>+<b>{} Glacite Powder", String.format("%,d", milestone.powder())));
        lore.add(Text.of("  <8>+<9>{} <b>Frozen Corpse RNG Meter <9>XP", String.format("%,d", milestone.rngXp())));
        lore.add(Text.of("  <8>+<b>{} SkyBlock XP", milestone.skyBlockXp()));
        if (!CorpseProgress.data(player).isMetWolf()) {
            lore.add(Text.empty());
            lore.add(Text.of("<c>Talk to Grandpa Wolf to start claiming!"));
        }
        return ItemStacks.item(pane, Text.of(colour + "Milestone " + StringUtility.getAsRomanNumeral(milestone.tier()) + " Rewards"), lore);
    }

    private ItemStack.Builder rngMeterItem(SkyBlockPlayer player) {
        var selected = CorpseRngMeter.selected(player);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>Frozen Corpses"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Your <b>Frozen Corpse RNG Meter <7>fills"));
        lore.add(Text.of("<7>with varying amounts of <9>XP <7>every"));
        lore.add(Text.of("<7>time you loot a <b>Frozen Corpse <7>while"));
        lore.add(Text.of("<7>in the <b>Glacite Mineshafts<7>!"));
        lore.add(Text.empty());
        lore.add(Text.of("<9>Lapis <b>Corpse<f>: <9>500 XP"));
        lore.add(Text.of("<6>Umber<f>/<7>Tungsten <b>Corpse<f>: <9>2,500 XP"));
        lore.add(Text.of("<f>Vanguard <b>Corpse<f>: <9>25,000 XP"));
        lore.add(Text.empty());
        lore.add(selected == null ? Text.of("<c>You don't have an RNG drop selected. Choose one!") : Text.of("<7>Selected: <f>{}", selected.name()));
        lore.add(Text.of("<7>Stored Frozen Corpse XP: <9>{}", String.format("%,d", CorpseRngMeter.stored(player))));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to view!"));
        return ItemStacks.item(Material.IRON_HELMET, Text.of("<d>RNG Meter"), lore);
    }

    private static Text bar(int value, int total) {
        int filled = value * BAR_LENGTH / total;
        return Text.of("<a><m>{}<f><m>{} <e>{}<6>/<e>{}", " ".repeat(filled), " ".repeat(BAR_LENGTH - filled), value, total);
    }
}
