package dev.daweapon.type.skyblockgeneric.trapper;

import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hiker.HikerQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class FarmingIslandsMaxing {
    private static final List<String> TRIGGERS = List.of("shepherd.talked", "shepherd.sheared", "npc.talbot",
            "abiphone.trevor.given", "abiphone.talbot.given", "abiphone.tony.given");

    private FarmingIslandsMaxing() {
    }

    public static void maxOut(SkyBlockPlayer player) {
        for (String trigger : TRIGGERS) {
            if (!ChapterTriggers.done(player, trigger)) ChapterTriggers.fire(player, trigger);
        }
        player.getHuntingData().setCounter("hiker.state", HikerQuest.REWARDED);
        dev.daweapon.type.skyblockgeneric.farming.lab.LabQuest.completeInstantly(player);
    }
}
