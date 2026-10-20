package dev.daweapon.type.skyblockgeneric.chapters;

import java.nio.file.Path;

public enum ChapterIsland {
    MOONGLADE_MARSH("Moonglade Marsh", "Hina", "moonglade_marsh", 0, null),
    TORRHUS_CANYON("Torrhus Canyon", "Helia", "torrhus_canyon", 100, "npc.helia");

    private final String displayName;
    private final String guide;
    private final Path file;
    private final int keyOffset;
    private final String gateTrigger;

    ChapterIsland(String displayName, String guide, String fileName, int keyOffset, String gateTrigger) {
        this.displayName = displayName;
        this.guide = guide;
        this.file = Path.of("./configuration/skyblock/chapters/" + fileName + ".yml");
        this.keyOffset = keyOffset;
        this.gateTrigger = gateTrigger;
    }

    public String displayName() {
        return displayName;
    }

    public String guide() {
        return guide;
    }

    public Path file() {
        return file;
    }

    public int keyOffset() {
        return keyOffset;
    }

    public String gateTrigger() {
        return gateTrigger;
    }
}
