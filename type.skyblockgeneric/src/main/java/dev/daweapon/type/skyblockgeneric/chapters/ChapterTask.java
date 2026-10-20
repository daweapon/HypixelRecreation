package dev.daweapon.type.skyblockgeneric.chapters;

public record ChapterTask(String id, String title, String description, ChapterReward reward, String trigger,
                          ChapterProbe probe, long amount, boolean available, String unavailable) {

    public String plainTitle() {
        return ChapterText.strip(title.replace("<base>", ""));
    }

    public String coloredTitle(String base) {
        return title.replace("<base>", base);
    }
}
