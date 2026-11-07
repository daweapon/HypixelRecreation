package dev.daweapon.dungeons.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.puzzle.quiz.QuizRound;
import dev.daweapon.dungeons.catacombs.puzzle.quiz.QuizRound.Answer;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizTest {
    private static PuzzleCatalogue.QuizRoom room() {
        return PuzzleCatalogueFixture.load().quiz("quiz").orElseThrow();
    }

    @Test
    void theBankHoldsTheWikiQuestionsWithTheirAnswers() {
        PuzzleCatalogue.QuizRoom room = room();
        assertTrue(room.questions().size() >= 25);
        assertEquals(3, room.asked());
        assertEquals(List.of("a", "b", "c"), room.pedestals().stream().map(PuzzleCatalogue.QuizPedestal::option).toList());
        QuizRound.Question jerry = room.questions().stream().filter(question -> question.question().contains("Jerry's Workshop"))
                .findFirst().orElseThrow();
        assertEquals("5 Fairy Souls", jerry.answer());
        room.questions().forEach(question -> assertFalse(question.wrong().contains(question.answer()), question.question()));
    }

    @Test
    void eachRoundAsksThreeDifferentQuestionsWithTheAnswerAmongThreeOptions() {
        Random random = new Random(9);
        for (int round = 0; round < 200; round++) {
            QuizRound quiz = QuizRound.deal(room().questions(), 3, random);
            Set<String> questions = new HashSet<>();
            for (QuizRound.Asked asked : quiz.asked()) {
                assertTrue(questions.add(asked.question()));
                assertEquals(3, asked.options().size());
                assertEquals(3, Set.copyOf(asked.options()).size());
                QuizRound.Question source = room().questions().stream().filter(each -> each.question().equals(asked.question())).findFirst().orElseThrow();
                assertEquals(source.answer(), asked.options().get(asked.correct()));
            }
        }
    }

    @Test
    void threeRightAnswersFinishTheQuizAndAWrongOneFailsIt() {
        QuizRound quiz = QuizRound.deal(room().questions(), 3, new Random(1));
        assertEquals(1, quiz.number());
        assertEquals(Answer.CORRECT, quiz.answer(quiz.current().correct()));
        assertEquals(2, quiz.number());
        assertEquals(Answer.CORRECT, quiz.answer(quiz.current().correct()));
        assertEquals(Answer.FINAL, quiz.answer(quiz.current().correct()));
        assertTrue(quiz.done());

        QuizRound wrong = QuizRound.deal(room().questions(), 3, new Random(2));
        assertEquals(Answer.WRONG, wrong.answer((wrong.current().correct() + 1) % 3));
        assertTrue(wrong.failed());
        assertEquals(Answer.IGNORED, wrong.answer(wrong.current().correct()));
    }
}
