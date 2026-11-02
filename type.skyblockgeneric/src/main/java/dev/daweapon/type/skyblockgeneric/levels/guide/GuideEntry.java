package dev.daweapon.type.skyblockgeneric.levels.guide;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public record GuideEntry(String label, String ref, GuideKind kind, GuideRule rule) {
    public boolean available() {
        return rule.available();
    }

    public boolean complete(SkyBlockPlayer player) {
        return SkyBlockGuide.complete(player, this);
    }
}
