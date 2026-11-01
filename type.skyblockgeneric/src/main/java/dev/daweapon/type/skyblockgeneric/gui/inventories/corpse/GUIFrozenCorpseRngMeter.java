package dev.daweapon.type.skyblockgeneric.gui.inventories.corpse;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseLootTable;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseRngMeter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class GUIFrozenCorpseRngMeter extends StatelessView {
    private static final int FIRST_ROW = 1;
    private static final int COLUMNS = 7;
    private static final int BAR_LENGTH = 20;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Frozen Corpses RNG Meter", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
        layout.slot(4, (_, c) -> summary((SkyBlockPlayer) c.player()));
        List<CorpseLootTable.MeterDrop> drops = CorpseLootTable.get().meterDrops();
        for (int i = 0; i < drops.size(); i++) {
            CorpseLootTable.MeterDrop drop = drops.get(i);
            int slot = (FIRST_ROW + i / COLUMNS) * 9 + 1 + i % COLUMNS;
            layout.slot(slot, (_, c) -> dropItem((SkyBlockPlayer) c.player(), drop), (_, c) -> select((SkyBlockPlayer) c.player(), drop, c));
        }
        layout.slot(51, (_, c) -> ItemStacks.item(Material.REDSTONE_BLOCK, Text.of("<d>Reset RNG Drop"), List.of(
                Text.of("<7>Resets the selected drop for this"),
                Text.of("<d>RNG Meter<7>. You still earn progress"),
                Text.of("<7>without a drop selected."),
                Text.empty(),
                Text.of("<e>Click to reset!"))), (_, c) -> {
            CorpseRngMeter.select((SkyBlockPlayer) c.player(), null);
            c.session(DefaultState.class).refresh();
        });
    }

    private void select(SkyBlockPlayer player, CorpseLootTable.MeterDrop drop, ViewContext ctx) {
        CorpseLootTable.MeterDrop selected = CorpseRngMeter.selected(player);
        if (selected != null && selected.id().equals(drop.id())) {
            CorpseRngMeter.select(player, null);
            player.sendMessage(Text.of("<e>Deselected <f>{}<e>.", drop.name()));
        } else {
            CorpseRngMeter.select(player, drop);
            player.sendMessage(Text.of("<a>Selected <f>{} <a>as your Frozen Corpse RNG Meter drop!", drop.name()));
        }
        ctx.session(DefaultState.class).refresh();
    }

    private ItemStack.Builder dropItem(SkyBlockPlayer player, CorpseLootTable.MeterDrop drop) {
        CorpseLootTable.MeterDrop selected = CorpseRngMeter.selected(player);
        boolean isSelected = selected != null && selected.id().equals(drop.id());
        long stored = isSelected ? CorpseRngMeter.stored(player) : 0;
        List<Text> lore = new ArrayList<>();
        lore.add(Text.empty());
        lore.add(Text.of("<7>Frozen Corpse XP: <9>{}<5>/<9>{}", String.format("%,d", stored), String.format("%,d", drop.requiredXp())));
        if (isSelected) lore.add(bar(CorpseRngMeter.percent(stored, drop.requiredXp())));
        lore.add(Text.empty());
        lore.add(isSelected ? Text.of("<a>SELECTED") : Text.of("<e>Click to select!"));
        SkyBlockItem item = new SkyBlockItem(drop.item(), 1);
        if (drop.book() != null) item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(drop.book(), drop.bookLevel()));
        ItemStack.Builder icon = PlayerItemUpdater.playerUpdate(player, item.getItemStackBuilder().build());
        ItemStacks.name(icon, Text.of("<5>{}", drop.name()));
        ItemStacks.lore(icon, lore);
        return isSelected ? ItemStacks.enchanted(icon) : icon;
    }

    private ItemStack.Builder summary(SkyBlockPlayer player) {
        CorpseLootTable.MeterDrop selected = CorpseRngMeter.selected(player);
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
        lore.add(Text.of("<7>Your selected <d>RNG Drop <7>will be"));
        lore.add(Text.of("<7>guaranteed to appear in a viable"));
        lore.add(Text.of("<b>Frozen Corpse <7>once your RNG Meter"));
        lore.add(Text.of("<7>is full!"));
        lore.add(Text.empty());
        lore.add(selected == null ? Text.of("<c>You don't have an RNG drop selected.") : Text.of("<7>Selected: <f>{}", selected.name()));
        lore.add(Text.of("<7>Stored Frozen Corpse XP: <9>{}", String.format("%,d", CorpseRngMeter.stored(player))));
        return ItemStacks.item(Material.IRON_HELMET, Text.of("<d>RNG Meter"), lore);
    }

    private static Text bar(double percent) {
        int filled = (int) Math.round(percent / 100D * BAR_LENGTH);
        return Text.of("<d>{}<f>{} <d>{}%", "─".repeat(filled), "─".repeat(BAR_LENGTH - filled), String.format("%.1f", percent));
    }
}
