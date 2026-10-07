package net.swofty.type.skyblockgeneric.carnival;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.carnival.CarnivalGameResult.Metric;
import org.jetbrains.annotations.Nullable;

/** Chantelle's fifteen yearly Carnival Goals, in her menu's order. */
@Getter
public enum CarnivalGoal {
    FRUIT_PLAY(CarnivalGame.FRUIT_DIGGING, Kind.PLAY, null, 3, 50, "Play <a>3 games <7>of <6>Fruit Digging<7>."),
    FRUIT_SCORE(CarnivalGame.FRUIT_DIGGING, Kind.SCORE, null, 3_000, 50, "Reach <a>3,000 score <7>in a single game\n<7>of <6>Fruit Digging<7>."),
    FRUIT_APPLES(CarnivalGame.FRUIT_DIGGING, Kind.METRIC, Metric.APPLES, 3, 100, "Dig up <a>3 Apples <7>in a single game of\n<6>Fruit Digging<7>."),
    FRUIT_UNIQUE(CarnivalGame.FRUIT_DIGGING, Kind.METRIC, Metric.UNIQUE_FRUITS, 5, 100, "Dig up <a>5 unique Fruits <7>in a single\n<7>game of <6>Fruit Digging<7>."),
    FRUIT_DRAGONFRUIT(CarnivalGame.FRUIT_DIGGING, Kind.METRIC, Metric.DRAGONFRUITS, 1, 100, "Dig up a <d>Dragonfruit<7>."),
    FISH_PLAY(CarnivalGame.CATCH_A_FISH, Kind.PLAY, null, 3, 50, "Play <a>3 games <7>of <3>Catch a Fish<7>."),
    FISH_SCORE(CarnivalGame.CATCH_A_FISH, Kind.SCORE, null, 3_000, 50, "Reach <a>3,000 score <7>in a single game\n<7>of <3>Catch a Fish<7>."),
    FISH_CATCH(CarnivalGame.CATCH_A_FISH, Kind.METRIC, Metric.FISH, 30, 100, "Catch <a>30 Fish <7>in a single game of\n<3>Catch a Fish<7>."),
    FISH_YELLOW(CarnivalGame.CATCH_A_FISH, Kind.METRIC, Metric.YELLOW_FISH, 5, 100, "Catch <a>5 Yellow Fish <7>in a single game\n<7>of <3>Catch a Fish<7>."),
    FISH_STREAK(CarnivalGame.CATCH_A_FISH, Kind.METRIC, Metric.BEST_STREAK, 5, 100, "Reach a <a>5 Catch Streak <7>in a single\n<7>game of <3>Catch a Fish<7>."),
    ZOMBIE_PLAY(CarnivalGame.ZOMBIE_SHOOTOUT, Kind.PLAY, null, 3, 50, "Play <a>3 games <7>of <c>Zombie Shootout<7>."),
    ZOMBIE_SCORE(CarnivalGame.ZOMBIE_SHOOTOUT, Kind.SCORE, null, 3_000, 50, "Reach <a>3,000 score <7>in a single game\n<7>of <c>Zombie Shootout<7>."),
    ZOMBIE_KILLS(CarnivalGame.ZOMBIE_SHOOTOUT, Kind.METRIC, Metric.ZOMBIES, 75, 100, "Shoot <a>75 Zombies <7>in a single game of\n<c>Zombie Shootout<7>."),
    ZOMBIE_DIAMOND(CarnivalGame.ZOMBIE_SHOOTOUT, Kind.METRIC, Metric.DIAMOND_ZOMBIES, 5, 100, "Shoot <a>5 Diamond Zombies <7>in a single\n<7>game of <c>Zombie Shootout<7>."),
    ZOMBIE_LAMPS(CarnivalGame.ZOMBIE_SHOOTOUT, Kind.METRIC, Metric.LAMPS, 5, 100, "Shoot <a>5 Redstone Lamps <7>in a single\n<7>game of <c>Zombie Shootout<7>.");

    public static final int TICKETS_PER_GOAL = 5;

    public enum Kind { PLAY, SCORE, METRIC }

    private final CarnivalGame game;
    private final Kind kind;
    private final @Nullable Metric metric;
    private final int target;
    private final int tokens;
    private final String description;

    CarnivalGoal(CarnivalGame game, Kind kind, @Nullable Metric metric, int target, int tokens, String description) {
        this.game = game;
        this.kind = kind;
        this.metric = metric;
        this.target = target;
        this.tokens = tokens;
        this.description = description;
    }

    public boolean isMetBy(CarnivalGameResult result, int gamesPlayedThisYear) {
        if (result.game() != game) return false;
        return switch (kind) {
            case PLAY -> gamesPlayedThisYear >= target;
            case SCORE -> result.score() >= target;
            case METRIC -> result.metric(metric) >= target;
        };
    }

    public String plainDescription() {
        return description.replaceAll("<[^>]+>", "").replace("\n", " ");
    }
}
