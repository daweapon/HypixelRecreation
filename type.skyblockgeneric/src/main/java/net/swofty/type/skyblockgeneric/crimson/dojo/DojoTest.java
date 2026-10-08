package net.swofty.type.skyblockgeneric.crimson.dojo;

import lombok.Getter;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Master Tao's seven tests, in the order they unlock. A time limit of zero means the test runs until you fail. */
@Getter
public enum DojoTest {
    FORCE("Force", Material.STICK, 120, List.of(
            "<6>1. <7>Knock mobs into the lava.",
            "<6>2. <7>Some mobs give more points than others.",
            "<6>3. <7>Avoid negative point mobs!")),
    STAMINA("Stamina", Material.RABBIT_FOOT, 0, List.of(
            "<6>1. <7>Jump through the open holes in the moving",
            "<7>walls.",
            "<6>2. <7>Earn points by staying on the platform.",
            "<6>3. <7>Don't get pushed into the lava!")),
    MASTERY("Mastery", Material.BOW, 120, List.of(
            "<6>1. <7>Shoot the targets before they disappear.",
            "<6>2. <a>Green<7> targets are worth <a>8 <7>points.",
            "<6>3. <e>Yellow <7>targets are worth <a>16 <7>points.",
            "<6>4. <c>Red <7>targets are worth <a>32 <7>points.")),
    DISCIPLINE("Discipline", Material.DIAMOND_SWORD, 120, List.of(
            "<6>1. <7>Hit the mob with the correct sword.",
            "<6>2. <7>Every 30 seconds, gain a new type of sword.",
            "<6>3. <7>Don't hit the mob with the wrong sword.")),
    SWIFTNESS("Swiftness", Material.LEAD, 0, List.of(
            "<6>1. <7>Temporary blocks will spawn to stand on.",
            "<6>2. <7>If the block is red it'll disappear soon!",
            "<6>3. <7>Don't stand around and fall into the lava.")),
    CONTROL("Control", Material.ENDER_EYE, 0, List.of(
            "<6>1. <7>Stare directly at a moving skeleton.",
            "<6>2. <7>More accurate staring gives more points",
            "<7>and time in the arena.",
            "<6>3. <7>Don't look away for too long or fall in the",
            "<7>lava!")),
    TENACITY("Tenacity", Material.FIRE_CHARGE, 0, List.of(
            "<6>1. <7>A ghast shoots fireballs at you.",
            "<6>2. <7>Earn points by avoiding fireballs over time.",
            "<6>3. <7>Don't trip and fall into lava!")),
    ;

    private final String displayName;
    private final Material icon;
    private final int timeLimitSeconds;
    private final List<String> rules;

    DojoTest(String displayName, Material icon, int timeLimitSeconds, List<String> rules) {
        this.displayName = displayName;
        this.icon = icon;
        this.timeLimitSeconds = timeLimitSeconds;
        this.rules = rules;
    }

    public String fullName() {
        return "Test of " + displayName;
    }

    public boolean isTimed() {
        return timeLimitSeconds > 0;
    }

    public @Nullable DojoTest previous() {
        return ordinal() == 0 ? null : values()[ordinal() - 1];
    }

    public static @Nullable DojoTest byName(String name) {
        for (DojoTest test : values()) {
            if (test.name().equalsIgnoreCase(name) || test.displayName.equalsIgnoreCase(name)) return test;
        }
        return null;
    }
}
