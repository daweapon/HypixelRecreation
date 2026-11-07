package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.dungeons.catacombs.puzzle.RoomBlock;
import dev.daweapon.dungeons.catacombs.puzzle.quiz.QuizRound;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Quiz: Oruo the Omniscient asks three SkyBlock questions (wiki answer list) when the party walks in. The question
 * and its three answers show in chat and over the statue and the three pedestals; pressing a pedestal's button answers.
 * Three right answers find the Blessing of Time, a wrong one fails the puzzle. The statue's lines follow a 2022 video
 * (A1Q08rRKRgk); the room itself is still the practice-map copy, not a capture.
 */
public final class QuizRoom extends PuzzleRoom {
    private static final String STATUE = "<c>[STATUE] Oruo the Omniscient<f>: ";
    private static final String[] LETTERS = {"ⓐ", "ⓑ", "ⓒ"};
    private static final int OPTION_TICKS = 10;
    private static final int NEXT_QUESTION_TICKS = 40;
    private static final double LINE_GAP = 0.3;
    private static final double PEDESTAL_LABEL_HEIGHT = 1.6;

    private final PuzzleCatalogue.QuizRoom data;
    private final Map<Long, Integer> buttons = new HashMap<>();
    private final List<Entity> labels = new ArrayList<>();
    private QuizRound round;
    private boolean asking;
    private boolean answerable;

    public QuizRoom(PuzzleContext context, PlacedRoom room, PuzzleCatalogue.QuizRoom data) {
        super(context, room, CatacombsPuzzle.QUIZ);
        this.data = data;
    }

    @Override
    public void start() {
        round = QuizRound.deal(data.questions(), data.asked(), ThreadLocalRandom.current());
        for (int option = 0; option < data.pedestals().size(); option++) {
            RoomBlock pedestal = data.pedestals().get(option).block();
            int[][] sides = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] side : sides) {
                int[] at = frame.block(new RoomBlock(pedestal.x() + side[0], pedestal.y(), pedestal.z() + side[1]));
                buttons.put(RoomFrame.key(at), option);
            }
        }
    }

    @Override
    public synchronized void moved(SkyBlockPlayer player, Pos to, boolean onGround) {
        if (asking || round == null) return;
        asking = true;
        ask();
    }

    private void ask() {
        QuizRound.Asked asked = round.current();
        answerable = false;
        clearLabels();
        String header = "<6><l>Question #" + round.number();
        context.host().broadcast(header);
        context.host().broadcast("<6>" + asked.question());
        Pos top = frame.spot(data.questionSpot());
        label(header, top);
        label("<6>" + asked.question(), top.sub(0, LINE_GAP, 0));
        for (int option = 0; option < asked.options().size(); option++) {
            String line = "<6>" + LETTERS[option] + " <a>" + asked.options().get(option);
            Pos above = frame.centre(data.pedestals().get(option).block()).add(0, PEDESTAL_LABEL_HEIGHT, 0);
            boolean last = option == asked.options().size() - 1;
            later(OPTION_TICKS * (option + 1), () -> showOption(line, above, last));
        }
    }

    private synchronized void showOption(String line, Pos above, boolean last) {
        if (over()) return;
        context.host().broadcast(" " + line);
        label(line, above);
        if (last) answerable = true;
    }

    private void label(String markup, Pos at) {
        Entity stand = new Entity(EntityType.ARMOR_STAND);
        stand.editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setMarker(true);
            meta.setInvisible(true);
            meta.setCustomName(Text.parse(markup).asComponent());
            meta.setCustomNameVisible(true);
        });
        stand.setNoGravity(true);
        stand.setInstance(instance(), at);
        labels.add(stand);
    }

    private void clearLabels() {
        labels.forEach(Entity::remove);
        labels.clear();
    }

    @Override
    public synchronized boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        Integer option = buttons.get(RoomFrame.key(x, y, z));
        if (option == null) return false;
        if (answerable && !over()) answer(player, option);
        return true;
    }

    private void answer(SkyBlockPlayer player, int option) {
        int number = round.number();
        switch (round.answer(option)) {
            case CORRECT -> {
                select("question_" + number);
                statue(player, "{} <a>answered <6>Question #" + number + " <a>correctly!");
                int left = round.left();
                context.host().broadcast(STATUE + left + (left == 1 ? " question" : " questions")
                        + " left... Then you will have proven your worth to me!");
                answerable = false;
                later(NEXT_QUESTION_TICKS, this::nextQuestion);
            }
            case FINAL -> {
                select("question_" + number);
                statue(player, "{} <a>answered the final question correctly!");
                context.host().broadcast(STATUE + "I bestow upon you all the power of a hundred years!");
                clearLabels();
                completeRoom();
                context.blessings().grant(BlessingType.TIME, PuzzleChest.BLESSING_LEVEL, player);
            }
            case WRONG -> {
                clearLabels();
                statue(player, "{} <c>chose the wrong answer! I shall never forget this moment of misrememberance.");
                fail(player, null);
            }
            case IGNORED -> {
            }
        }
    }

    private synchronized void nextQuestion() {
        if (!over()) ask();
    }

    private void statue(SkyBlockPlayer player, String line) {
        context.host().broadcast(Text.of(STATUE + line, player.getColouredName()));
    }

    @Override
    public synchronized void solve(SkyBlockPlayer staff) {
        if (round == null) return;
        asking = true;
        while (!over() && !round.done() && !round.failed()) answer(staff, round.current().correct());
    }

    @Override
    public List<String> info() {
        if (round == null) return List.of();
        List<String> lines = new ArrayList<>();
        for (QuizRound.Asked asked : round.asked()) {
            lines.add("<7>" + asked.question() + " <a>" + LETTERS[asked.correct()] + " " + asked.options().get(asked.correct()));
        }
        return lines;
    }

    @Override
    public void dispose() {
        clearLabels();
    }
}
