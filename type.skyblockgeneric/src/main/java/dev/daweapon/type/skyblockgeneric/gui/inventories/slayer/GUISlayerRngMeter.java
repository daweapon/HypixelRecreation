package dev.daweapon.type.skyblockgeneric.gui.inventories.slayer;

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
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerDefinition;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerDrop;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerDropTable;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRegistry;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRngMeter;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUISlayerRngMeter extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34};
    private static final int BAR_LENGTH = 20;

    private final SlayerMenuStyle style;

    public GUISlayerRngMeter(SlayerMenuStyle style) {
        this.style = style;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("{} RNG Meter", InventoryType.CHEST_6_ROW, style.type().displayName());
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        SlayerDefinition definition = SlayerRegistry.get(style.type());
        if (definition == null) return;
        List<SlayerDrop> drops = definition.drops().stream().filter(SlayerDrop::hasRngMeter).toList();
        for (int i = 0; i < drops.size() && i < SLOTS.length; i++) {
            SlayerDrop drop = drops.get(i);
            layout.slot(SLOTS[i],
                (s, c) -> dropItem((SkyBlockPlayer) c.player(), definition, drop),
                (click, c) -> select((SkyBlockPlayer) c.player(), definition, drop, c));
        }
        layout.slot(4, (s, c) -> summary((SkyBlockPlayer) c.player(), definition));
    }

    private void select(SkyBlockPlayer player, SlayerDefinition definition, SlayerDrop drop, ViewContext ctx) {
        DatapointSlayer.SlayerProgress progress = player.getSlayerData().progress(style.type());
        if (!SlayerRngMeter.unlocked(progress)) {
            player.sendMessage(Text.of("<c>Slay a Tier III {} to unlock the RNG Meter!", style.type().displayName()));
            return;
        }
        int level = definition.levelForXp(progress.getXp());
        if (!drop.available()) {
            player.sendMessage(Text.of("<c>{}", drop.unavailableReason() == null ? "This drop isn't available yet." : drop.unavailableReason()));
            return;
        }
        if (level < drop.requiredLevel()) {
            player.sendMessage(Text.of("<c>Requires {} Slayer {}!", style.type().categoryName(), drop.requiredLevel()));
            return;
        }
        if (drop.key().equals(progress.getRngItem())) {
            SlayerRngMeter.select(player, style.type(), null);
            player.sendMessage(Text.of("<e>Deselected {}<e>.", SlayerDropTable.displayName(drop)));
        } else {
            SlayerRngMeter.select(player, style.type(), drop);
            player.sendMessage(Text.of("<a>Selected {} <a>as your RNG Meter drop!", SlayerDropTable.displayName(drop)));
        }
        ctx.session(DefaultState.class).refresh();
    }

    private ItemStack.Builder dropItem(SkyBlockPlayer player, SlayerDefinition definition, SlayerDrop drop) {
        DatapointSlayer.SlayerProgress progress = player.getSlayerData().progress(style.type());
        int level = definition.levelForXp(progress.getXp());
        boolean selected = drop.key().equals(progress.getRngItem());
        List<Text> lore = new ArrayList<>(SlayerDropDisplay.oddsLines(drop));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Required: <d>{} {} Slayer XP", String.format("%,d", drop.rngMeterXp()), style.type().categoryName()));
        Text requirement = SlayerDropDisplay.requirement(style.type(), drop, level);
        if (requirement != null) lore.add(requirement);
        lore.add(Text.empty());
        if (!drop.available()) {
            lore.add(Text.of("<c>{}", drop.unavailableReason() == null ? "Not available yet." : drop.unavailableReason()));
        } else if (selected) {
            lore.add(Text.of("<7>Progress: <d>{}%", String.format("%.1f", SlayerRngMeter.percent(progress, drop))));
            lore.add(bar(SlayerRngMeter.percent(progress, drop)));
            lore.add(Text.empty());
            lore.add(Text.of("<a>SELECTED"));
            lore.add(Text.of("<e>Click to deselect!"));
        } else {
            lore.add(Text.of("<c>Selecting a drop resets the meter."));
            lore.add(Text.of("<e>Click to select!"));
        }
        ItemStack.Builder icon = SlayerDropDisplay.icon(player, drop, lore);
        return selected ? ItemStacks.enchanted(icon) : icon;
    }

    private ItemStack.Builder summary(SkyBlockPlayer player, SlayerDefinition definition) {
        DatapointSlayer.SlayerProgress progress = player.getSlayerData().progress(style.type());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Earn Slayer XP from Tier III+ kills"));
        lore.add(Text.of("<7>to fill the meter. The selected drop"));
        lore.add(Text.of("<7>grows up to <a>3x <7>as likely, and a full"));
        lore.add(Text.of("<7>meter guarantees it on your next kill."));
        lore.add(Text.empty());
        if (!SlayerRngMeter.unlocked(progress)) {
            lore.add(Text.of("<c>Slay a Tier III {} to unlock!", style.type().displayName()));
        } else {
            SlayerDrop selected = SlayerRngMeter.selected(definition, progress);
            if (selected == null) {
                lore.add(Text.of("<7>Selected Drop: <c>None"));
            } else {
                lore.add(Text.of("<7>Selected Drop: {}", SlayerDropTable.displayName(selected)));
                lore.add(Text.of("<7>Stored XP: <d>{}<5>/<d>{}", String.format("%,d", progress.getRngXp()),
                    String.format("%,d", selected.rngMeterXp())));
                lore.add(bar(SlayerRngMeter.percent(progress, selected)));
            }
        }
        return ItemStacks.item(Material.PAINTING, Text.of("<d>{} RNG Meter", style.type().displayName()), lore);
    }

    static Text bar(double percent) {
        int filled = (int) Math.round(percent / 100D * BAR_LENGTH);
        return Text.of("<d>{}<f>{} <d>{}%", "─".repeat(filled), "─".repeat(BAR_LENGTH - filled),
            String.format("%.1f", percent));
    }
}
