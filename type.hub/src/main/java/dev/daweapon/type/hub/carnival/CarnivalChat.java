package dev.daweapon.type.hub.carnival;

import dev.daweapon.commons.ChatUtility;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The end-of-game summary as Hypixel prints it: a green rule, then white-prefixed lines centred with spaces. */
public final class CarnivalChat {
    public static final String RULE = "<a><l>" + "▬".repeat(64);

    private CarnivalChat() {
    }

    private static final double CENTER_PX = 160.5;
    private static final int WRAP_PX = 300;
    private static final Map<String, Integer> CAPTURED = Map.of(
            "<6><l>Fruit Digging", 31, "<3><l>Catch a Fish", 31, "<c><l>Zombie Shootout", 29,
            "<f>Your Score", 33, "<f>Fruits Collected", 30, "<f>Fish Caught", 33, "<f>Zombies Slain", 32);

    public static String centered(String markup) {
        return "<f>" + " ".repeat(spacesFor(markup)) + markup;
    }

    /** Hypixel's own spacing for the fixed headings; an estimate within one space for everything else. */
    static int spacesFor(String markup) {
        Integer captured = CAPTURED.get(markup);
        if (captured != null) return captured;
        int spaceWidth = ChatUtility.FontInfo.SPACE.getLength() + 1;
        return (int) Math.max(0, Math.ceil((CENTER_PX - width(markup) / 2D) / spaceWidth));
    }

    static int width(String markup) {
        boolean bold = false;
        int width = 0;
        for (int i = 0; i < markup.length(); i++) {
            char c = markup.charAt(i);
            int close = c == '<' ? markup.indexOf('>', i) : -1;
            if (close > 0) {
                String tag = markup.substring(i + 1, close);
                if (tag.equals("l")) bold = true;
                else if (tag.length() == 1 || tag.equals("reset")) bold = false;
                i = close;
                continue;
            }
            ChatUtility.FontInfo font = ChatUtility.FontInfo.getDefaultFontInfo(c);
            width += (bold ? font.getBoldLength() : font.getLength()) + 1;
        }
        return width;
    }

    /** Splits a long list line where the chat would wrap it and centres each part, as the Fruits Collected list is. */
    public static List<String> centeredWrapped(List<String> items, String separator) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            String piece = items.get(i) + (i < items.size() - 1 ? separator : "");
            for (String word : piece.split("(?<= )")) {
                if (!line.isEmpty() && width(line + word.stripTrailing()) > WRAP_PX) {
                    lines.add(centered(line.toString().stripTrailing()));
                    line.setLength(0);
                }
                line.append(word);
            }
        }
        if (!line.isEmpty()) lines.add(centered(line.toString().stripTrailing()));
        return lines;
    }

    public static List<String> summary(CarnivalGame game, long score, List<String> body, int tokens) {
        List<String> lines = new ArrayList<>();
        lines.add(RULE);
        lines.add(centered(game.getColor() + "<l>" + game.getDisplayName()));
        lines.add("");
        lines.add(centered("<f>Your Score"));
        lines.add(centered("<a>" + StringUtility.commaify(score)));
        lines.add("");
        lines.addAll(body);
        lines.add("");
        lines.add(centered("<f>You earned <e>" + StringUtility.commaify(tokens) + " Carnival Tokens<7>!"));
        lines.add(RULE);
        return lines;
    }

    public static String timeLeft(int seconds) {
        String colour = seconds > 60 ? "<a>" : seconds > 20 ? "<e>" : "<c>";
        String time = seconds >= 60 ? seconds / 60 + "m " + seconds % 60 + "s" : seconds + "s";
        return "<f>Time Left: <7>" + colour + time;
    }
}
