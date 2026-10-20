package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelCause;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.levels.causes.StarlynMilestoneCause;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class StarlynMilestones {
    public static final int SKYBLOCK_XP_PER_MILESTONE = 5;
    public static final int FORAGING_FORTUNE_PER_MILESTONE = 2;
    public static final int EMBLEM_MILESTONE = 5;

    private StarlynMilestones() {
    }

    public static int current(SkyBlockPlayer player, StarlynSister sister) {
        return StarlynContests.data(player, sister).getMilestoneGranted();
    }

    public static void advance(SkyBlockPlayer player, StarlynSister sister) {
        StarlynProfile profile = StarlynContests.profile(player);
        StarlynProfile.SisterData data = profile.sister(sister);
        StarlynConfig config = StarlynConfig.get(sister);
        int reached = config.milestoneFor(data.getCouponsEarned());
        while (data.getMilestoneGranted() < reached) {
            data.setMilestoneGranted(data.getMilestoneGranted() + 1);
            grant(player, sister, config.milestone(data.getMilestoneGranted()));
        }
        StarlynContests.save(player, profile);
        sync(player);
        ChapterTriggers.atLeast(player, sister.trigger("milestone"), data.getMilestoneGranted());
    }

    public static void sync(SkyBlockPlayer player) {
        int gained = 0;
        for (StarlynSister sister : StarlynSister.values()) gained += current(player, sister) - 1;
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.STARLYN_SISTER_LEVELS,
                (double) gained * SKYBLOCK_XP_PER_MILESTONE);
    }

    public static void maxOut(SkyBlockPlayer player) {
        for (StarlynSister sister : StarlynSister.values()) {
            StarlynConfig config = StarlynConfig.get(sister);
            StarlynProfile profile = StarlynContests.profile(player);
            StarlynProfile.SisterData data = profile.sister(sister);
            data.setMet(true);
            data.setCouponsEarned(Math.max(data.getCouponsEarned(), config.milestone(config.maxMilestone()).threshold()));
            StarlynContests.save(player, profile);
            advance(player, sister);
        }
    }

    private static void grant(SkyBlockPlayer player, StarlynSister sister, StarlynConfig.Milestone milestone) {
        String roman = StringUtility.getAsRomanNumeral(milestone.number());
        player.sendMessage("<6><l>MILESTONE! <r><7>You reached <e>" + sister.milestoneName() + " " + roman + "<7>!");
        if (milestone.foragingXp() > 0) {
            var skills = player.getSkills();
            skills.setRaw(player, SkillCategories.FORAGING, skills.getRaw(SkillCategories.FORAGING) + milestone.foragingXp());
            player.sendMessage("<8>  +<3>" + StringUtility.commaify(milestone.foragingXp()) + " Foraging Experience");
        }
        if (milestone.essence() > 0) {
            Essences.add(player, EssenceType.FOREST, milestone.essence());
            player.sendMessage("<8>  +<f>" + StringUtility.commaify(milestone.essence()) + "x <d>Forest Essence");
        }
        player.sendMessage("<8>  +<a>" + FORAGING_FORTUNE_PER_MILESTONE + " <6>Foraging Fortune");
        player.sendMessage("<8>  +<b>" + SKYBLOCK_XP_PER_MILESTONE + " SkyBlock XP");
        if (milestone.whisperBuffPercent() > 0) {
            player.sendMessage("<9>" + sister.getCurrency().getDisplayName() + " from " + sister.contestName()
                    + "s are now buffed by " + milestone.whisperBuffPercent() + "%!");
        }
        if (sister == StarlynSister.AGATHA && milestone.number() == EMBLEM_MILESTONE) grantEmblem(player);
    }

    private static void grantEmblem(SkyBlockPlayer player) {
        StarlynMilestoneCause cause = SkyBlockLevelCause.getStarlynMilestoneCause(StarlynSister.AGATHA.key(), EMBLEM_MILESTONE);
        if (cause == null) return;
        player.getSkyBlockExperience().addExperience(cause);
        player.sendMessage("<8>  +<a>Agatha's Jewel Emblem <7>ꖘ");
    }
}
