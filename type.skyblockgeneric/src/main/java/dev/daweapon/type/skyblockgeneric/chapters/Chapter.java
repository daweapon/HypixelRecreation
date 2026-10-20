package dev.daweapon.type.skyblockgeneric.chapters;

import java.util.List;

public record Chapter(ChapterIsland island, int number, String description, List<ChapterTask> tasks, ChapterReward completion) {
    public static final String[] ROMAN = {"", "I", "II", "III", "IV", "V", "VI", "VII"};

    public String roman() {
        return ROMAN[number];
    }

    public int key() {
        return island.keyOffset() + number;
    }

    public List<ChapterTask> availableTasks() {
        return tasks.stream().filter(ChapterTask::available).toList();
    }
}
