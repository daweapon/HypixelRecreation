package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public final class TreeState {
    public static final int CAPACITY = 5;
    public static final int GUARANTEED_AFTER_DAYS = 42;
    public static final int ROLLS_AFTER_DAYS = 30;
    public static final double ROLL_PERCENT = 10;
    public static final double PURPLE_PERCENT = 15;
    private static final int MAX_CATCH_UP_DAYS = 400;

    public interface Dice {
        boolean generates(double percent);

        boolean purple(double percent);
    }

    private int green;
    private int purple;
    private long lastGeneratedDay;
    private long checkedDay;

    private TreeState(long day) {
        this.lastGeneratedDay = day;
        this.checkedDay = day;
    }

    public static TreeState fresh(long day) {
        return new TreeState(day);
    }

    public static TreeState parse(@Nullable String raw, long today) {
        TreeState state = new TreeState(today);
        if (raw == null || raw.isBlank()) return state;
        JSONObject tree = new JSONObject(raw).optJSONObject("tree");
        if (tree == null) return state;
        state.green = tree.optInt("green");
        state.purple = tree.optInt("purple");
        state.lastGeneratedDay = tree.optLong("last", today);
        state.checkedDay = tree.optLong("checked", today);
        return state;
    }

    public String serialize() {
        return new JSONObject().put("tree", new JSONObject()
                .put("green", green).put("purple", purple)
                .put("last", lastGeneratedDay).put("checked", checkedDay)).toString();
    }

    public int green() {
        return green;
    }

    public int purple() {
        return purple;
    }

    public int held() {
        return green + purple;
    }

    public boolean checkedOn(long day) {
        return checkedDay >= day;
    }

    public void take() {
        green = 0;
        purple = 0;
    }

    public int advance(long today, Dice dice) {
        if (today < checkedDay) {
            checkedDay = today;
            lastGeneratedDay = Math.min(lastGeneratedDay, today);
            return 0;
        }
        long from = Math.max(checkedDay + 1, today - MAX_CATCH_UP_DAYS + 1);
        int generated = 0;
        for (long day = from; day <= today; day++) {
            if (held() >= CAPACITY) break;
            long since = day - lastGeneratedDay;
            boolean fruit = since >= GUARANTEED_AFTER_DAYS || (since >= ROLLS_AFTER_DAYS && dice.generates(ROLL_PERCENT));
            if (!fruit) continue;
            lastGeneratedDay = day;
            if (dice.purple(PURPLE_PERCENT)) purple++;
            else green++;
            generated++;
        }
        checkedDay = today;
        return generated;
    }
}
