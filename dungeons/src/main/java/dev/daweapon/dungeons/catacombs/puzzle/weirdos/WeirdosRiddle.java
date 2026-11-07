package dev.daweapon.dungeons.catacombs.puzzle.weirdos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;
import java.util.random.RandomGenerator;

/**
 * The riddle dealt to one Three Weirdos room: a name for the weirdo at each of the room's three spots, the line each
 * one says, and the spot whose chest holds the reward. The case's roles are shuffled onto the spots.
 */
public record WeirdosRiddle(List<String> names, List<String> lines, int rewardedSpot) {
    public WeirdosRiddle {
        names = List.copyOf(names);
        lines = List.copyOf(lines);
    }

    public static WeirdosRiddle deal(List<WeirdosCase> cases, List<String> namePool, RandomGenerator random,
                                     UnaryOperator<String> nameStyle) {
        if (cases.isEmpty() || namePool.size() < WeirdosCase.WEIRDOS) {
            throw new IllegalArgumentException("Three Weirdos needs at least one case and three names");
        }
        WeirdosCase dealt = cases.get(random.nextInt(cases.size()));
        List<String> pool = new ArrayList<>(namePool);
        Collections.shuffle(pool, Random.from(random));
        List<String> roleNames = pool.subList(0, WeirdosCase.WEIRDOS);
        List<Integer> roleAtSpot = new ArrayList<>(List.of(0, 1, 2));
        Collections.shuffle(roleAtSpot, Random.from(random));
        List<String> names = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        int rewardedSpot = -1;
        for (int spot = 0; spot < WeirdosCase.WEIRDOS; spot++) {
            int role = roleAtSpot.get(spot);
            names.add(roleNames.get(role));
            lines.add(resolve(dealt.lines().get(role), roleNames, nameStyle));
            if (role == dealt.rewarded()) rewardedSpot = spot;
        }
        return new WeirdosRiddle(names, lines, rewardedSpot);
    }

    private static String resolve(String line, List<String> roleNames, UnaryOperator<String> nameStyle) {
        String resolved = line;
        for (int role = 0; role < roleNames.size(); role++) {
            resolved = resolved.replace("{" + (role + 1) + "}", nameStyle.apply(roleNames.get(role)));
        }
        return resolved;
    }

    public boolean rewarded(int spot) {
        return spot == rewardedSpot;
    }

    public String rewardedName() {
        return names.get(rewardedSpot);
    }
}
