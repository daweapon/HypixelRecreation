package dev.daweapon.type.skyblockgeneric.crimson.questline;

/** One stage of a Crimson Isle main questline. Stages are saved by ordinal, so only ever append. */
public interface QuestlineStep {
    String objective();

    int ordinal();

    String name();
}
