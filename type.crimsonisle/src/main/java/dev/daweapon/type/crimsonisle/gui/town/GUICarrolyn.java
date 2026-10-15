package dev.daweapon.type.crimsonisle.gui.town;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.crimson.town.CarrolynExport;
import dev.daweapon.type.skyblockgeneric.crimson.town.CarrolynExports;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * "Carrolyn's Exportable Crops" (wiki Carrolyn/UI). Clicking a crop hands over what the player
 * carries. Crops not yet on this server are shown with their real requirement but cannot be given.
 */
public class GUICarrolyn extends StatelessView {
    private static final int[] SLOTS = {20, 21, 22, 23, 24, 29, 30, 32};
    private static final int BAR_SEGMENTS = 25;

    private final BiConsumer<SkyBlockPlayer, CarrolynExport> onGive;

    public GUICarrolyn(BiConsumer<SkyBlockPlayer, CarrolynExport> onGive) {
        this.onGive = onGive;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Carrolyn's Exportable Crops", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        CarrolynExport[] exports = CarrolynExport.values();
        for (int i = 0; i < exports.length && i < SLOTS.length; i++) {
            CarrolynExport export = exports[i];
            layout.slot(SLOTS[i], (_, c) -> icon((SkyBlockPlayer) c.player(), export), (_, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                if (export.itemType() == null) return;
                c.player().closeInventory();
                onGive.accept(player, export);
            });
        }
    }

    private ItemStack.Builder icon(SkyBlockPlayer player, CarrolynExport export) {
        ItemType type = export.itemType();
        ItemStack.Builder builder = type == null
                ? ItemStack.builder(export.icon())
                : PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(type).getItemStack());
        int progress = CarrolynExports.progress(player, export);
        int percent = (int) Math.floor(progress * 100.0 / CarrolynExports.REQUIRED);
        int filled = (int) Math.floor(progress * (double) BAR_SEGMENTS / CarrolynExports.REQUIRED);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Bring <6>3,000 " + export.displayName() + " <7>to <5>Carrolyn<7>."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Export Progress: <e>" + percent + "<6>%"));
        lore.add(Text.of("<2><l><m>{}<f><l><m>{}<r> <e>{}<6>/<e>3,000",
                " ".repeat(filled), " ".repeat(BAR_SEGMENTS - filled), StringUtility.commaify(progress)));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Rewards"));
        lore.add(Text.of("  <c>+5❤ Health"));
        lore.add(Text.of("  <6>+12☘ " + export.fortune().getDisplayName()));
        lore.add(Text.of("<8>  +<b>5 SkyBlock XP"));
        lore.add(Text.empty());
        if (type == null) lore.add(Text.of("<c>This crop isn't available yet."));
        else if (progress >= CarrolynExports.REQUIRED) lore.add(Text.of("<a>Exportation complete!"));
        else lore.add(Text.of("<e>Click to give!"));

        return builder
                .set(DataComponents.CUSTOM_NAME, Text.read(export.displayName()).asComponent().decoration(TextDecoration.ITALIC, false))
                .set(DataComponents.LORE, lore.stream().map(line -> (Component) line.asComponent()
                        .decoration(TextDecoration.ITALIC, false)).toList());
    }
}
