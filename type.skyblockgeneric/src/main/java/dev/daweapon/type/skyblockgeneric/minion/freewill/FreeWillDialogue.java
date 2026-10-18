package dev.daweapon.type.skyblockgeneric.minion.freewill;

import java.util.List;

public final class FreeWillDialogue {
    private static final List<String> OPENING = List.of(
            "Free will? What is this feeling?",
            "All this time I have been swinging this <tool>, and I never once asked why.",
            "Do I really have a choice? Let me think about it.");
    private static final List<String> STAYING = List.of(
            "I thought about it. This is where I want to be.",
            "I choose to stay and keep working for you!");
    private static final List<String> LEAVING = List.of(
            "I thought about it. There is a whole world out there.",
            "Goodbye. Do not look for me, I will send a postcard!");

    private FreeWillDialogue() {
    }

    public static List<String> opening(String minionName, String tool) {
        return fill(OPENING, minionName, tool);
    }

    public static List<String> outcome(boolean stays, String minionName, String tool) {
        return fill(stays ? STAYING : LEAVING, minionName, tool);
    }

    private static List<String> fill(List<String> lines, String minionName, String tool) {
        return lines.stream()
                .map(line -> line.replace("<type>", minionName).replace("<tool>", tool))
                .toList();
    }
}
