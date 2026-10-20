package dev.daweapon.type.skyblockgeneric.gui.inventories.chapters;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.questlog.GUIMissionLog;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

public class GUIQuestsAndChapters extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Quests & Chapters", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(4, ItemStacks.item(Material.WRITABLE_BOOK, """
                <a>Quests & Chapters
                <7>Each island has its own series of
                <b>Chapters <7>for you to complete!

                <7>Complete tasks within a Chapter to
                <7>earn small <6>rewards<7>, or complete
                <7>entire Chapters to earn big ones!

                <7>Some islands also have <a>Quests <7>for
                <7>you to complete! Some items can only
                <7>be obtained through Quests."""));
        layout.slot(20, ItemStacks.item(Material.BOOK, """
                <b>Chapters
                <8>/chapters

                <7>View your progression through the
                <b>Chapters <7>of each island!

                <e>Click to view!"""), (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            if (!HotfService.isUnlocked(player)) {
                player.sendMessage("<c>Talk to Hina in Tangleburg's Path on Galatea to unlock your Chapters!");
                return;
            }
            c.push(new GUIChapters());
        });
        layout.slot(24, ItemStacks.item(Material.WRITABLE_BOOK, """
                <a>Quest Log
                <8>/quests

                <7>View your active quests, progress,
                <7>and rewards.

                <e>Click to view!"""), (_, c) -> c.push(new GUIMissionLog()));
        Components.backOrClose(layout, 40, ctx);
    }
}
