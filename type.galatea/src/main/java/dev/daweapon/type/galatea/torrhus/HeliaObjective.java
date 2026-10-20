package dev.daweapon.type.galatea.torrhus;

import dev.daweapon.type.skyblockgeneric.chapters.ChapterIsland;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.QuestObjective;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;

final class HeliaObjective {
    static final Pos HELIA = new Pos(-690.5, 93, 155.5);

    private HeliaObjective() {
    }

    static @Nullable QuestObjective of(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return null;
        if (ChapterTriggers.done(player, ChapterIsland.TORRHUS_CANYON.gateTrigger())) return null;
        return QuestObjective.at("Talk to Helia", HELIA);
    }
}
