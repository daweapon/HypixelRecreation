package dev.daweapon.type.skyblockgeneric.greatspook;

import java.util.random.RandomGenerator;

public record MathProblem(String text, int answer) {
    public static MathProblem random(RandomGenerator random) {
        int a = 6 + random.nextInt(15);
        int b = 3 + random.nextInt(10);
        return new MathProblem(a + " * " + b, a * b);
    }

    public boolean accepts(String message) {
        try {
            return Integer.parseInt(message.trim()) == answer;
        } catch (NumberFormatException invalid) {
            return false;
        }
    }
}
