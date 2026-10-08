package net.swofty.type.skyblockgeneric.chickenrace;

import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

/**
 * Gulliver's four Chicken Race rewards, with the wiki's time limits. The cluckpoints are the course's
 * green time hoops (and the finish), so the race countdown always runs to the next one's limit.
 */
@Getter
public enum Cluckpoint {
    ONE(33, ItemType.RED_GIFT, "<c>Red Gift"),
    TWO(52, ItemType.GIFT_COMPASS, "<a>Gift Compass"),
    THREE(68, ItemType.FROZEN_CHICKEN, "<9>Frozen Chicken"),
    FOUR(104, ItemType.JINGLE_BELLS, "<9>Jingle Bells");

    private final int secondsToBeat;
    private final ItemType reward;
    private final String rewardMarkup;

    Cluckpoint(int secondsToBeat, ItemType reward, String rewardMarkup) {
        this.secondsToBeat = secondsToBeat;
        this.reward = reward;
        this.rewardMarkup = rewardMarkup;
    }

    public int number() {
        return ordinal() + 1;
    }

    /** Time a passed cluckpoint adds to the countdown: the gap to the next cluckpoint's limit. */
    public int bonusSeconds() {
        Cluckpoint next = next(number());
        return next == null ? 0 : next.secondsToBeat - secondsToBeat;
    }

    public static Cluckpoint of(int number) {
        return values()[number - 1];
    }

    public static @Nullable Cluckpoint next(int claimed) {
        return claimed >= values().length ? null : values()[claimed];
    }

    /** Countdown shown during a run that has already passed {@code passed} cluckpoints. */
    public static long remainingMillis(int passed, long elapsedMillis) {
        return of(Math.min(passed + 1, values().length)).secondsToBeat * 1000L - elapsedMillis;
    }

    public enum Outcome {
        /** First arrival in time: the race ends and the reward is granted. */
        CLAIM,
        /** First arrival, but slower than the time limit: the race ends empty-handed. */
        TOO_SLOW,
        /** Already claimed: it only adds time and the race carries on. */
        PASS,
        /** Cluckpoint 1 is as far as anyone gets before talking to Frosty. */
        NEEDS_FROSTY
    }

    public static Outcome reach(Cluckpoint reached, int claimed, long elapsedMillis, boolean spokenToFrosty) {
        if (reached.number() <= claimed) {
            return reached == ONE && !spokenToFrosty ? Outcome.NEEDS_FROSTY : Outcome.PASS;
        }
        return elapsedMillis < reached.secondsToBeat * 1000L ? Outcome.CLAIM : Outcome.TOO_SLOW;
    }
}
