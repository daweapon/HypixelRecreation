package dev.daweapon.type.generic.quest;

import lombok.Builder;
import lombok.Getter;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.achievement.AchievementCategory;

@Getter
@Builder
public class QuestDefinition {
    private final String id;
    private final String name;
    private final String description;
    private final QuestType type;
    private final AchievementCategory category;
    private final String trigger;
    private final int goal;
    private final QuestReward reward;
    private final String headTexture;

    public Text formatProgress(int current) {
        return Text.of("<b>(<6>{}<b>/<6>{}<b>)", current, goal);
    }
}
