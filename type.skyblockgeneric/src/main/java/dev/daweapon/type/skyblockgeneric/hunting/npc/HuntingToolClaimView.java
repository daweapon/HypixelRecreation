package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;

import java.util.List;
import java.util.function.Consumer;

public class HuntingToolClaimView extends StatelessView {
    private final String title;
    private final ItemType tool;
    private final boolean soulbound;
    private final Consumer<SkyBlockPlayer> onClaim;

    public HuntingToolClaimView(String title, ItemType tool, boolean soulbound, Consumer<SkyBlockPlayer> onClaim) {
        this.title = title;
        this.tool = tool;
        this.soulbound = soulbound;
        this.onClaim = onClaim;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(title, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        var icon = new SkyBlockItem(tool).getItemStackBuilder();
        List<Text> lore = ItemStacks.loreText(icon);
        lore.addAll(List.of(Text.empty(), Text.of("<e>Click to claim!")));
        layout.slot(22, ItemStacks.lore(icon, lore), (click, c) -> claim((SkyBlockPlayer) c.player()));
    }

    private void claim(SkyBlockPlayer player) {
        if (player.getAmountInInventory(tool) > 0) {
            player.sendMessage("<c>You already have one of these!");
            return;
        }
        SkyBlockItem item = new SkyBlockItem(tool);
        if (soulbound) item.getAttributeHandler().setSoulBound(false);
        player.addAndUpdateItem(item);
        player.playSuccessSound();
        player.closeInventory();
        onClaim.accept(player);
    }
}
