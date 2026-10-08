package net.swofty.type.skyblockgeneric.crimson.dojo;

import lombok.Getter;

/** The rank a single Dojo test run earns. Every test uses the same point thresholds. */
@Getter
public enum DojoGrade {
    F(0, "<c>", "You  were not good enough. Come back when you've trained more."),
    D(200, "<a>", "Hardly skilled, but I'll let it pass. Come talk to me for your next challenge, and do better in the future."),
    C(400, "<a>", "Rather average, I'd say. Speak to me again to try a new challenge, or improve on this one!"),
    B(600, "<9>", "That was pretty good! Please come by again for a new challenge."),
    A(800, "<d>", "Wow, that was really impressive. Come talk to me to view your next challenge."),
    S(1000, "<6>", "That was a remarkable performance! I look forward to giving you your next challenge."),
    ;

    private final int requiredScore;
    private final String colour;
    private final String taoResponse;

    DojoGrade(int requiredScore, String colour, String taoResponse) {
        this.requiredScore = requiredScore;
        this.colour = colour;
        this.taoResponse = taoResponse;
    }

    public String coloured() {
        return colour + name();
    }

    public boolean isPassing() {
        return this != F;
    }

    public static DojoGrade of(int score) {
        DojoGrade result = F;
        for (DojoGrade grade : values()) {
            if (score >= grade.requiredScore) result = grade;
        }
        return result;
    }
}
