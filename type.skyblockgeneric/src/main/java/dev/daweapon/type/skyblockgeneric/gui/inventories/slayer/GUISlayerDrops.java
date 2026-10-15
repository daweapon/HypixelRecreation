package dev.daweapon.type.skyblockgeneric.gui.inventories.slayer;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerDefinition;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerDrop;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUISlayerDrops extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    private final SlayerMenuStyle style;

    public GUISlayerDrops(SlayerMenuStyle style) {
        this.style = style;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("{} Drops", InventoryType.CHEST_6_ROW, style.type().displayName());
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        SlayerDefinition definition = SlayerRegistry.get(style.type());
        if (definition == null) return;
        List<SlayerDrop> drops = definition.drops();
        for (int i = 0; i < drops.size() && i < SLOTS.length; i++) {
            SlayerDrop drop = drops.get(i);
            layout.slot(SLOTS[i], (s, c) -> dropItem((SkyBlockPlayer) c.player(), definition, drop));
        }
    }

    private ItemStack.Builder dropItem(SkyBlockPlayer player, SlayerDefinition definition, SlayerDrop drop) {
        int level = definition.levelForXp(player.getSlayerData().progress(style.type()).getXp());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>{}", poolName(drop.pool())));
        lore.add(Text.empty());
        lore.addAll(SlayerDropDisplay.oddsLines(drop));
        Text requirement = SlayerDropDisplay.requirement(style.type(), drop, level);
        if (requirement != null) {
            lore.add(Text.empty());
            lore.add(requirement);
        }
        if (drop.hasRngMeter()) {
            lore.add(Text.empty());
            lore.add(Text.of("<d>Can be targeted with the RNG Meter."));
        }
        if (!drop.available()) {
            lore.add(Text.empty());
            lore.add(Text.of("<c>{}", drop.unavailableReason() == null ? "Not available yet." : drop.unavailableReason()));
        }
        return SlayerDropDisplay.icon(player, drop, lore);
    }

    private static String poolName(SlayerDrop.Pool pool) {
        return switch (pool) {
            case TOKEN -> "Token Drop";
            case MAIN -> "Main Drop";
            case EXTRA -> "Extra Drop";
            case SEPARATE -> "Separate Drop";
            case LOOT_SHARE -> "Loot Share Drop";
        };
    }
}
