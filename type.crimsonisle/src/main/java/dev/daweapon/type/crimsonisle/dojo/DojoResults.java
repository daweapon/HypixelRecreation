package dev.daweapon.type.crimsonisle.dojo;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoGrade;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoProgress;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;

/** The end-of-test summary and Master Tao's verdict. */
public final class DojoResults {
    private static final String RULE = "<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

    private DojoResults() {
    }

    public static void announce(SkyBlockPlayer player, DojoTest test, int score, DojoGrade grade, long time,
                                boolean cancelled, boolean newBest, boolean nextWasUnlocked) {
        if (cancelled) player.sendMessage("<c>You cancelled the challenge, so it counts as a fail.");
        player.sendMessage(RULE);
        player.sendMessage("<6><l>  " + test.fullName().toUpperCase() + " <7>finished!");
        player.sendMessage("<7>  Your Rank: " + grade.coloured() + " <8>(" + StringUtility.commaify(score) + ")");
        player.sendMessage("<7>  Time: <b>" + DojoFormat.time(time));
        if (newBest && score > 0) player.sendMessage("<6><l>  NEW PERSONAL BEST!");
        player.sendMessage("<7>  Dojo Points: <6>" + StringUtility.commaify(DojoProgress.points(player)));
        player.sendMessage(RULE);

        player.showTitle(Title.title(Text.of(grade.coloured()).asComponent(),
                Text.of("<7>" + StringUtility.commaify(score) + " points").asComponent(),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(500))));
        player.playSound(Sound.sound(Key.key(grade.isPassing() ? "entity.player.levelup" : "entity.villager.no"),
                Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());

        ScheduleUtility.delay(() -> {
            if (!player.isOnline()) return;
            MasterTao.say(player, grade.getTaoResponse());
            int next = test.ordinal() + 1;
            if (!nextWasUnlocked && next < DojoTest.values().length && DojoProgress.isUnlocked(player, DojoTest.values()[next])) {
                player.sendMessage("<a>You unlocked the <9>" + DojoTest.values()[next].fullName() + "<a>!");
            }
        }, 20);
    }
}
