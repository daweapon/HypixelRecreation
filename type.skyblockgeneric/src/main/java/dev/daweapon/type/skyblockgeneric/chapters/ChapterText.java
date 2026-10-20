package dev.daweapon.type.skyblockgeneric.chapters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChapterText {
    private static final Pattern TAG = Pattern.compile("<[^>]*>");
    private static final Pattern COLOUR = Pattern.compile("<[0-9a-f]>");
    private static final int LINE_WIDTH = 34;

    private ChapterText() {
    }

    public static String strip(String markup) {
        return TAG.matcher(markup).replaceAll("");
    }

    public static List<String> wrap(String markup) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        int visible = 0;
        String colour = "<7>";
        for (String word : markup.split(" ")) {
            int width = strip(word).length();
            if (visible > 0 && visible + 1 + width > LINE_WIDTH) {
                lines.add(line.toString());
                line = new StringBuilder("<7>").append(colour.equals("<7>") ? "" : colour);
                visible = 0;
            }
            if (visible > 0) {
                line.append(' ');
                visible++;
            }
            line.append(word);
            visible += width;
            Matcher matcher = COLOUR.matcher(word);
            while (matcher.find()) colour = matcher.group();
        }
        lines.add(line.toString());
        return lines.stream().map(value -> value.startsWith("<7>") ? value : "<7>" + value).toList();
    }
}
