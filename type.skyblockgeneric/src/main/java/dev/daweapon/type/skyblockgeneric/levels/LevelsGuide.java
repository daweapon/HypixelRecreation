package dev.daweapon.type.skyblockgeneric.levels;

import lombok.Getter;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.levels.guide.GuideTask;
import dev.daweapon.type.skyblockgeneric.levels.guide.SkyBlockGuide;

import java.util.List;

@Getter
public enum LevelsGuide {
    STARTER(Text.of("<8>New Player"), Text.of("<7>You are starting on your journey through SkyBlock. Complete these tasks to get acquainted with the game."),
            Material.LIME_STAINED_GLASS_PANE),
    AMATEUR(Text.of("<8>Very Early Game"), Text.of("<7>Looks like you're getting the hang of things! As you continue to play SkyBlock, complete these tasks to expand your understanding of the game."),
            Material.YELLOW_STAINED_GLASS_PANE),
    INTERMEDIATE(Text.of("<8>Early Game"), Text.of("<7>You have successfully navigated the base layer of SkyBlock, but there is still much more to discover. Prepare yourself for the adventures ahead by completing these tasks."),
            Material.ORANGE_STAINED_GLASS_PANE),
    SKILLED(Text.of("<8>Mid Game"), Text.of("<7>Since you've made it this far, it's clear that you know your way around SkyBlock. Keep pushing yourself to reach your full potential by completing these tasks."),
            Material.RED_STAINED_GLASS_PANE),
    EXPERT(Text.of("<8>Late Game"), Text.of("<7>You have made significant progress and should be proud of your accomplishments. Complete further tasks and touch some grass from time to time, eh?"),
            Material.PURPLE_STAINED_GLASS_PANE),
    PROFESSIONAL(Text.of("<8>Very Late Game"), Text.of("<7>You're better at this than most, but never forget your roots. Everyone was a Starter at one point."),
            Material.BLUE_STAINED_GLASS_PANE),
    MASTER(Text.of("<8>End Game"), Text.of("<7>Why are you still playing this game? To check these boxes, that's why!"),
            Material.BLACK_STAINED_GLASS_PANE);

    private final Text title;
    private final Text description;
    private final Material glassMaterial;

    LevelsGuide(Text title, Text description, Material glassMaterial) {
        this.title = title;
        this.description = description;
        this.glassMaterial = glassMaterial;
    }

    public List<GuideTask> tasks() {
        return SkyBlockGuide.tasks(this);
    }
}
