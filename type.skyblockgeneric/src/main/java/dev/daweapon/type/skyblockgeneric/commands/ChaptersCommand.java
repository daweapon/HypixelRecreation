package dev.daweapon.type.skyblockgeneric.commands;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterIsland;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.gui.inventories.chapters.GUIChapters;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Opens the Chapters of the island you are on",
    usage = "/chapters",
    permission = Rank.DEFAULT,
    labels = "chapters",
    allowsConsole = false)
public class ChaptersCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            if (!HotfService.isUnlocked(player)) {
                player.sendMessage("<c>Talk to Hina in Tangleburg's Path on Galatea to unlock your Chapters!");
                return;
            }
            boolean torrhus = HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_TORRHUS_CANYON;
            ChapterIsland island = torrhus ? ChapterIsland.TORRHUS_CANYON : ChapterIsland.MOONGLADE_MARSH;
            if (!ChapterService.isOpen(player, island)) {
                player.sendMessage("<c>Talk to Helia in Torrhus Canyon to unlock your Torrhus Chapters!");
                return;
            }
            player.openView(new GUIChapters(island));
        });
    }
}
