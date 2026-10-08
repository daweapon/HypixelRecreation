package net.swofty.type.skyblockgeneric.user;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;

/** A quest step shown under "Objective" on the sidebar, with the place it leads to when it has one. */
public record QuestObjective(String text, @Nullable Pos target) {
    public static QuestObjective at(String text, @Nullable Pos target) {
        return new QuestObjective(text, target);
    }

    public static QuestObjective anywhere(String text) {
        return new QuestObjective(text, null);
    }
}
