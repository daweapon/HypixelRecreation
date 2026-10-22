package dev.daweapon.type.skyblockgeneric.hiker;

import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHunting;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

public final class HikerQuest {
    public static final long SKYBLOCK_DAY_MILLIS = SkyBlockCalendar.DAY * 50L;
    public static final long FEED_COOLDOWN_MILLIS = 36 * SKYBLOCK_DAY_MILLIS;
    public static final long STARVE_MILLIS = 144 * SKYBLOCK_DAY_MILLIS;
    public static final long RESTART_MILLIS = 5L * 24 * 60 * 60 * 1000;
    public static final int FEEDINGS_REQUIRED = 10;

    public static final int NOT_STARTED = 0;
    public static final int ACTIVE = 1;
    public static final int FINISHED = 2;
    public static final int REWARDED = 3;
    public static final int STARVED = 4;

    private static final String STATE = "hiker.state";
    private static final String FOOD = "hiker.food";
    private static final String FEEDINGS = "hiker.feedings";
    private static final String FED_AT = "hiker.fedAt";
    private static final String DIED_AT = "hiker.diedAt";

    public enum Food {
        PORKCHOP("COOKED_PORKCHOP", "grilled meat", 30),
        APPLE("APPLE", "red and crunchy", 20),
        BREAD("BREAD", "made of wheat", 25),
        BEEF("COOKED_BEEF", "from a cow", 30),
        MELON("MELON_SLICE", "red on the inside and green on the outside", 64),
        POTATO("BAKED_POTATO", "cooked potato", 30),
        CHICKEN("COOKED_CHICKEN", "meat from a fowl", 30);

        private final String itemId;
        private final String clue;
        private final int amount;

        Food(String itemId, String clue, int amount) {
            this.itemId = itemId;
            this.clue = clue;
            this.amount = amount;
        }

        public String itemId() {
            return itemId;
        }

        public String clue() {
            return clue;
        }

        public int amount() {
            return amount;
        }
    }

    public enum Feeding {
        NOTHING_HELD, WRONG_FOOD, WRONG_AMOUNT, TOO_SOON, FED, FINISHED
    }

    private HikerQuest() {
    }

    private static DatapointHunting.HuntingData data(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HUNTING, DatapointHunting.class).getValue();
    }

    private static void save(SkyBlockPlayer player) {
        var datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HUNTING, DatapointHunting.class);
        datapoint.setValue(datapoint.getValue());
    }

    public static int state(SkyBlockPlayer player) {
        refresh(player, System.currentTimeMillis());
        return (int) data(player).counter(STATE);
    }

    public static boolean isCompleted(SkyBlockPlayer player) {
        int state = state(player);
        return state == FINISHED || state == REWARDED;
    }

    public static void refresh(SkyBlockPlayer player, long now) {
        var data = data(player);
        if (data.counter(STATE) != ACTIVE || data.counter(FEEDINGS) == 0) return;
        long fedAt = data.counter(FED_AT);
        if (now - fedAt <= STARVE_MILLIS) return;
        data.setCounter(STATE, STARVED);
        data.setCounter(DIED_AT, fedAt + STARVE_MILLIS);
        save(player);
    }

    public static Food food(SkyBlockPlayer player) {
        Food[] foods = Food.values();
        return foods[(int) Math.floorMod(data(player).counter(FOOD), foods.length)];
    }

    public static int feedings(SkyBlockPlayer player) {
        return (int) data(player).counter(FEEDINGS);
    }

    public static long millisUntilFeedable(SkyBlockPlayer player, long now) {
        var data = data(player);
        if (data.counter(FEEDINGS) == 0) return 0;
        return Math.max(0, data.counter(FED_AT) + FEED_COOLDOWN_MILLIS - now);
    }

    public static boolean restartReady(SkyBlockPlayer player, long now) {
        var data = data(player);
        return data.counter(STATE) == STARVED && now - data.counter(DIED_AT) >= RESTART_MILLIS;
    }

    public static void start(SkyBlockPlayer player) {
        var data = data(player);
        data.setCounter(STATE, ACTIVE);
        data.setCounter(FEEDINGS, 0);
        data.setCounter(FED_AT, 0);
        data.setCounter(DIED_AT, 0);
        data.setCounter(FOOD, ThreadLocalRandom.current().nextInt(Food.values().length));
        save(player);
    }

    public static void restart(SkyBlockPlayer player) {
        var data = data(player);
        data.setCounter(STATE, NOT_STARTED);
        data.setCounter(FEEDINGS, 0);
        data.setCounter(FED_AT, 0);
        data.setCounter(DIED_AT, 0);
        save(player);
    }

    public static Feeding feed(SkyBlockPlayer player, String heldId, int heldAmount, long now) {
        if (millisUntilFeedable(player, now) > 0) return Feeding.TOO_SOON;
        if (heldId.isEmpty()) return Feeding.NOTHING_HELD;
        Food food = food(player);
        if (!food.itemId().equals(heldId)) return Feeding.WRONG_FOOD;
        if (heldAmount != food.amount()) return Feeding.WRONG_AMOUNT;
        var data = data(player);
        long feedings = data.counter(FEEDINGS) + 1;
        data.setCounter(FEEDINGS, feedings);
        data.setCounter(FED_AT, now);
        data.setCounter(FOOD, ThreadLocalRandom.current().nextInt(Food.values().length));
        ChapterTriggers.fire(player, "hiker.fed");
        if (feedings >= FEEDINGS_REQUIRED) {
            data.setCounter(STATE, FINISHED);
            save(player);
            ChapterTriggers.fire(player, "hiker.complete");
            return Feeding.FINISHED;
        }
        save(player);
        return Feeding.FED;
    }

    public static void markRewarded(SkyBlockPlayer player) {
        var data = data(player);
        data.setCounter(STATE, REWARDED);
        save(player);
    }
}
