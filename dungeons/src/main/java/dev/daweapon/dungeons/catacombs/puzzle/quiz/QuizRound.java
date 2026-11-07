package dev.daweapon.dungeons.catacombs.puzzle.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * One Quiz room's three questions from Oruo the Omniscient. Each question shows the right answer among two wrong
 * ones in a random order; a right answer moves on to the next question, a wrong one fails the puzzle.
 */
public final class QuizRound {
    public static final int OPTIONS = 3;

    public record Question(String question, String answer, List<String> wrong) {
        public Question {
            wrong = List.copyOf(wrong);
            if (wrong.size() < OPTIONS - 1) throw new IllegalArgumentException("A quiz question needs at least two wrong answers");
        }
    }

    public record Asked(String question, List<String> options, int correct) {
        public Asked {
            options = List.copyOf(options);
        }
    }

    public enum Answer {
        CORRECT,
        FINAL,
        WRONG,
        IGNORED
    }

    private final List<Asked> asked;
    private int current;
    private boolean failed;

    private QuizRound(List<Asked> asked) {
        this.asked = List.copyOf(asked);
    }

    public static QuizRound deal(List<Question> bank, int questions, RandomGenerator random) {
        if (bank.size() < questions) throw new IllegalArgumentException("Not enough quiz questions");
        Random shuffler = Random.from(random);
        List<Question> pool = new ArrayList<>(bank);
        Collections.shuffle(pool, shuffler);
        List<Asked> asked = new ArrayList<>();
        for (Question question : pool) {
            if (asked.size() == questions) break;
            if (asked.stream().anyMatch(each -> each.question().equals(question.question()))) continue;
            List<String> wrong = new ArrayList<>(question.wrong());
            Collections.shuffle(wrong, shuffler);
            List<String> options = new ArrayList<>(wrong.subList(0, OPTIONS - 1));
            options.add(question.answer());
            Collections.shuffle(options, shuffler);
            asked.add(new Asked(question.question(), options, options.indexOf(question.answer())));
        }
        return new QuizRound(asked);
    }

    public Answer answer(int option) {
        if (failed || done()) return Answer.IGNORED;
        if (option != asked.get(current).correct()) {
            failed = true;
            return Answer.WRONG;
        }
        current++;
        return done() ? Answer.FINAL : Answer.CORRECT;
    }

    public Asked current() {
        return asked.get(Math.min(current, asked.size() - 1));
    }

    /** The number of the question being asked, from 1. */
    public int number() {
        return Math.min(current, asked.size() - 1) + 1;
    }

    public int left() {
        return asked.size() - current;
    }

    public boolean done() {
        return current == asked.size();
    }

    public boolean failed() {
        return failed;
    }

    public List<Asked> asked() {
        return asked;
    }
}
