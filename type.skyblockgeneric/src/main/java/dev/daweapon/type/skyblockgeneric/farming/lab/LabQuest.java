package dev.daweapon.type.skyblockgeneric.farming.lab;

import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class LabQuest {
    public static final String BETH = "BETH";
    public static final String OPENED = "quest.beth.lab.opened";
    public static final String LAB1_DONE = "quest.beth.lab1.done";
    public static final String PLUSHIE = "quest.beth.plushie";
    public static final String LAB2_STARTED = "quest.beth.lab2.started";
    public static final String LAB2_LEVER = "quest.beth.lab2.lever";
    public static final String LAB2_DONE = "quest.beth.lab2.done";
    public static final int NOTE_VISIT = 3;
    public static final int LAB1_VISIT = 4;
    public static final int LAB2_VISIT = 5;

    public enum Stage {
        VISITING, NOTE, OPENED, LAB1, LAB1_DONE, LAB2_PENDING, LAB2_STARTED, LAB2_LEVER, COMPLETE
    }

    private LabQuest() {
    }

    public static int acceptedVisits(SkyBlockPlayer player) {
        return Gardens.profile(player).visitors().offersAccepted(BETH);
    }

    public static Stage stage(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return Stage.VISITING;
        if (ChapterTriggers.done(player, LAB2_DONE)) return Stage.COMPLETE;
        int visits = acceptedVisits(player);
        if (ChapterTriggers.done(player, LAB2_LEVER)) return Stage.LAB2_LEVER;
        if (ChapterTriggers.done(player, LAB2_STARTED)) return Stage.LAB2_STARTED;
        if (ChapterTriggers.done(player, LAB1_DONE)) return visits >= LAB2_VISIT ? Stage.LAB2_PENDING : Stage.LAB1_DONE;
        if (ChapterTriggers.done(player, OPENED)) return visits >= LAB1_VISIT ? Stage.LAB1 : Stage.OPENED;
        return visits >= NOTE_VISIT ? Stage.NOTE : Stage.VISITING;
    }

    public static boolean canDig(SkyBlockPlayer player) {
        return acceptedVisits(player) >= NOTE_VISIT;
    }

    public static boolean canVisit(SkyBlockPlayer player, GardenProfile profile) {
        int visits = profile.visitors().offersAccepted(BETH);
        return switch (visits) {
            case NOTE_VISIT -> ChapterTriggers.done(player, OPENED);
            case LAB1_VISIT -> ChapterTriggers.done(player, LAB1_DONE) && !profile.greenhouse().plots().isEmpty();
            case LAB2_VISIT -> ChapterTriggers.done(player, LAB2_DONE);
            default -> true;
        };
    }

    public static boolean isComplete(SkyBlockPlayer player) {
        return ChapterTriggers.done(player, LAB2_DONE);
    }

    public static void completeInstantly(SkyBlockPlayer player) {
        for (String flag : new String[]{OPENED, LAB1_DONE, PLUSHIE, LAB2_STARTED, LAB2_LEVER, LAB2_DONE}) {
            if (!ChapterTriggers.done(player, flag)) ChapterTriggers.fire(player, flag);
        }
    }
}
