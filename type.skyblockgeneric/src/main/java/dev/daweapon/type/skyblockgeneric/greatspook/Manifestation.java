package dev.daweapon.type.skyblockgeneric.greatspook;

import java.util.List;

public enum Manifestation {
    DEADLINE("DEADLINE_100", "Deadline"),
    DARKNESS_SHADE("DARKNESS_SHADE_100", "Darkness Shade"),
    COMMITMENT_PHOBIA("COMMITMENT_PHOBIA", "Commitment Phobia"),
    MATH_TEACHER("MATH_TEACHER_100", "Math Teacher"),
    PUBLIC_SPEAKING_DEMON("PUBLIC_SPEAKING_DEMON_100", "Public Speaking Demon"),
    VEGAN_CRAWLER("VEGAN_CRAWLER_100", "Vegan Crawler");

    public static final List<Manifestation> ALL = List.of(values());

    private final String mobId;
    private final String displayName;

    Manifestation(String mobId, String displayName) {
        this.mobId = mobId;
        this.displayName = displayName;
    }

    public String mobId() {
        return mobId;
    }

    public String displayName() {
        return displayName;
    }
}
