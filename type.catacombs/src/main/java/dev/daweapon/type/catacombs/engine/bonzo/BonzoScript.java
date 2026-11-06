package dev.daweapon.type.catacombs.engine.bonzo;

import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Bonzo's fight on Floor I, one tick at a time, as the two captured runs played it (times from his spawn):
 * <ul>
 *     <li>three intro lines 4.1 s apart while he stands at his spawn and cannot be hurt;</li>
 *     <li>at 11.5 s he dashes away (an Ender teleport) and starts summoning Undead every 0.3-3 s, each one an orb thrown
 *     at his own spot or a player's that lands 2.25 s later;</li>
 *     <li>his first hurt says "Oh noes, you got me.. what ever will I do?!", then a random line every 4.1 s;</li>
 *     <li>about 10 s after he starts moving he teleports to the middle of the arena and fires a Wither Skull, then a
 *     second one 0.7 s later;</li>
 *     <li>at 0 health: "Oh I'm dead!", "Sike" 3.1 s later, his second life 1.1 s after that;</li>
 *     <li>at 0 again: the final line, the death sounds 0.35 s later, the score 2.25 s later, "But my masters are a lot
 *     stronger.." at 4.1 s, the first exit gate at 4.2 s, the second 2 s later and "Just you wait..." at 8.2 s.</li>
 * </ul>
 * Neither run lasted long enough in the second life to show Bonzo's Balloon or Balloon Barrage. A 2022 solo video
 * (FLQEVDif3wo) shows the barrage opening with "SHOOOOOOOOO(obfuscated) TIMEEEEEEE!", balloons hitting for about
 * 6,000, then "Weeeeeeeee"; the timings are estimates.
 */
public final class BonzoScript {
    public sealed interface Action {
    }

    public record Say(String line) implements Action {
    }

    public record StartMoving() implements Action {
    }

    public record SummonOrb() implements Action {
    }

    public record SkullVolley(boolean toCentre) implements Action {
    }

    public record Revive() implements Action {
    }

    public record Balloon() implements Action {
    }

    public record BalloonBarrage() implements Action {
    }

    public record DeathSounds() implements Action {
    }

    public record ScoreScreen() implements Action {
    }

    public record OpenGate(int gate) implements Action {
    }

    public enum Phase {INTRO, FIRST_LIFE, DOWNED, SECOND_LIFE, DEFEATED}

    public static final int LINE_GAP = 82;
    public static final int ORB_TICKS = 45;
    public static final double ORB_PEAK = 9.5;
    public static final int MOVE_TICK = 230;
    public static final int SUMMON_GAP_MIN = 5;
    public static final int SUMMON_GAP_MAX = 60;
    public static final int SKULL_AFTER_MOVING = 200;
    public static final int SKULL_GAP = 200;
    public static final int SECOND_SKULL = 14;
    public static final int SIKE_AFTER = 62;
    public static final int REVIVE_AFTER_SIKE = 22;
    public static final int BALLOON_FIRST = 40;
    public static final int BALLOON_GAP = 60;
    public static final int BARRAGE_FIRST = 160;
    public static final int BARRAGE_GAP = 300;
    public static final int SECOND_LIFE_LINE = 120;
    public static final int BARRAGE_LINE = 40;
    public static final int DEATH_SOUNDS = 7;
    public static final int SCORE = 45;
    public static final int MASTERS = 82;
    public static final int FIRST_GATE = 84;
    public static final int SECOND_GATE = 124;
    public static final int JUST_YOU_WAIT = 164;

    static final List<String> INTRO = List.of(
            "Gratz for making it this far, but I'm basically unbeatable.",
            "I don't even need to fight, this is the life!",
            "I can summon lots of undead! Check this out.");
    static final String FIRST_HURT = "Oh noes, you got me.. what ever will I do?!";
    static final List<String> FIRST_LIFE_LINES = List.of(
            "Stop.", "You're not funny.", "Wait..", "Cringe.", "Give me 1 sec..", "Lame.",
            "Leave me alone!", "Fight my Undeads instead.", "Why are you running?",
            "Cheater!", "Hacker!", "Not fair!", "OUCH!");
    static final String DEAD = "Oh I'm dead!";
    static final String SIKE = "Sike";
    static final List<String> SECOND_LIFE_LINES = List.of(
            "Hoho, looks like you killed me!", "You fools!", "You fell victim to one of the classic blunders!",
            "Check mate.", "I can revive myself and become much stronger!");
    static final List<String> BARRAGE_LINES = List.of(
            "All I have are positive thoughts.", "Do you want a balloon?", "Run, run, run, RUN!", "Weeeeeeeee");
    static final String SHOWTIME = "SHOOOOOOOOO<k>OOOOOOW</k> TIMEEEEEEE!";
    static final String FINAL = "Alright, maybe I'm just weak after all..";
    static final String MASTERS_LINE = "But my masters are a lot stronger..";
    static final String WAIT_LINE = "Just you wait...";

    private final RandomGenerator random;
    private Phase phase = Phase.INTRO;
    private long now;
    private long nextSummon = -1;
    private long nextSkull = -1;
    private long nextChatter = -1;
    private long downedAt = -1;
    private long revivedAt = -1;
    private long nextBalloon = -1;
    private long nextBarrage = -1;
    private long barrageLineAt = -1;
    private long defeatedAt = -1;
    private boolean hurt;

    public BonzoScript(RandomGenerator random) {
        this.random = random;
    }

    public List<Action> tick() {
        List<Action> actions = new ArrayList<>();
        now++;
        switch (phase) {
            case INTRO -> intro(actions);
            case FIRST_LIFE -> firstLife(actions);
            case DOWNED -> downed(actions);
            case SECOND_LIFE -> secondLife(actions);
            case DEFEATED -> defeated(actions);
        }
        return actions;
    }

    private void intro(List<Action> actions) {
        for (int index = 0; index < INTRO.size(); index++) {
            if (now == 1 + (long) index * LINE_GAP) actions.add(new Say(INTRO.get(index)));
        }
        if (now < MOVE_TICK) return;
        phase = Phase.FIRST_LIFE;
        actions.add(new StartMoving());
        actions.add(new SummonOrb());
        nextSummon = now + summonGap();
        nextSkull = now + SKULL_AFTER_MOVING;
    }

    private void firstLife(List<Action> actions) {
        if (now >= nextSummon) {
            actions.add(new SummonOrb());
            nextSummon = now + summonGap();
        }
        if (now == nextSkull) actions.add(new SkullVolley(true));
        if (now == nextSkull + SECOND_SKULL) {
            actions.add(new SkullVolley(false));
            nextSkull += SKULL_GAP;
        }
        if (hurt && now >= nextChatter) {
            actions.add(new Say(pick(FIRST_LIFE_LINES)));
            nextChatter = now + LINE_GAP;
        }
    }

    private void downed(List<Action> actions) {
        if (now == downedAt + SIKE_AFTER) actions.add(new Say(SIKE));
        if (now < downedAt + SIKE_AFTER + REVIVE_AFTER_SIKE) return;
        phase = Phase.SECOND_LIFE;
        revivedAt = now;
        nextBalloon = now + BALLOON_FIRST;
        nextBarrage = now + BARRAGE_FIRST;
        actions.add(new Revive());
    }

    private void secondLife(List<Action> actions) {
        if (now == revivedAt + SECOND_LIFE_LINE) actions.add(new Say(pick(SECOND_LIFE_LINES)));
        if (now == barrageLineAt) actions.add(new Say(pick(BARRAGE_LINES)));
        if (now >= nextBarrage) {
            actions.add(new Say(SHOWTIME));
            actions.add(new BalloonBarrage());
            barrageLineAt = now + BARRAGE_LINE;
            nextBarrage = now + BARRAGE_GAP;
            nextBalloon = Math.max(nextBalloon, now + BALLOON_GAP);
        } else if (now >= nextBalloon) {
            actions.add(new Balloon());
            nextBalloon = now + BALLOON_GAP;
        }
    }

    private void defeated(List<Action> actions) {
        long since = now - defeatedAt;
        if (since == DEATH_SOUNDS) actions.add(new DeathSounds());
        if (since == SCORE) actions.add(new ScoreScreen());
        if (since == MASTERS) actions.add(new Say(MASTERS_LINE));
        if (since == FIRST_GATE) actions.add(new OpenGate(1));
        if (since == SECOND_GATE) actions.add(new OpenGate(2));
        if (since == JUST_YOU_WAIT) actions.add(new Say(WAIT_LINE));
    }

    /** Bonzo took damage; the first time in his first life he complains about it. */
    public List<Action> hurt() {
        if (phase != Phase.FIRST_LIFE || hurt) return List.of();
        hurt = true;
        nextChatter = now + LINE_GAP;
        return List.of(new Say(FIRST_HURT));
    }

    /** Bonzo's health reached zero. */
    public List<Action> lifeLost() {
        if (phase == Phase.FIRST_LIFE) {
            phase = Phase.DOWNED;
            downedAt = now;
            return List.of(new Say(DEAD));
        }
        if (phase == Phase.SECOND_LIFE) {
            phase = Phase.DEFEATED;
            defeatedAt = now;
            return List.of(new Say(FINAL));
        }
        return List.of();
    }

    /** The orb's path: straight across from Bonzo to where the Undead will stand, arcing about 9.5 blocks up. */
    public static Pos orbPosition(Pos from, Pos to, double progress) {
        double t = Math.clamp(progress, 0, 1);
        double x = from.x() + (to.x() - from.x()) * t;
        double z = from.z() + (to.z() - from.z()) * t;
        double y = from.y() + (to.y() + 1 - from.y()) * t + 4 * ORB_PEAK * t * (1 - t);
        return new Pos(x, y, z);
    }

    private int summonGap() {
        return SUMMON_GAP_MIN + random.nextInt(SUMMON_GAP_MAX - SUMMON_GAP_MIN + 1);
    }

    private String pick(List<String> lines) {
        return lines.get(random.nextInt(lines.size()));
    }

    public Phase phase() {
        return phase;
    }

    public long now() {
        return now;
    }

    public boolean vulnerable() {
        return phase == Phase.FIRST_LIFE || phase == Phase.SECOND_LIFE;
    }

    public boolean finished() {
        return phase == Phase.DEFEATED && now - defeatedAt >= JUST_YOU_WAIT;
    }
}
