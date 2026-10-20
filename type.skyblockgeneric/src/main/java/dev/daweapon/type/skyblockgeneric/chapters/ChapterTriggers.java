package dev.daweapon.type.skyblockgeneric.chapters;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ChapterTriggers {
    private ChapterTriggers() {
    }

    public static void fire(SkyBlockPlayer player, String trigger) {
        add(player, trigger, 1);
    }

    public static void add(SkyBlockPlayer player, String trigger, long amount) {
        ChapterService.add(player, trigger, amount);
    }

    public static void atLeast(SkyBlockPlayer player, String trigger, long value) {
        ChapterService.atLeast(player, trigger, value);
    }

    public static boolean done(SkyBlockPlayer player, String trigger) {
        return ChapterService.done(player, trigger);
    }
}
