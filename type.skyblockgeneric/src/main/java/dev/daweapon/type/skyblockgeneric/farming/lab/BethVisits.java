package dev.daweapon.type.skyblockgeneric.farming.lab;

import java.util.List;
import java.util.Map;

public final class BethVisits {
    private static final Map<Integer, List<String>> OFFER = Map.of(
            1, List.of("I think I am really onto something here.",
                    "Jake says he can't walk, but I've seen him walk yesterday.",
                    "He also asks strangers to get him some animals, but these animals are nowhere to be found!"),
            2, List.of("I followed him today to where he brings all the animals he catches. He goes down some weird cave..."),
            3, List.of("I managed to follow him into the cave today, he had a shovel with him and a note fell out of his pocket..."),
            4, List.of("You found what? A secret lab hidden inside the cave???"),
            5, List.of("Hey, {player}, I've been thinking a lot about that lab we found... I think there's more to it than just the plushies."));
    private static final Map<Integer, List<String>> ACCEPT = Map.of(
            1, List.of("I'm getting really close to solving this mystery."),
            2, List.of("Unfortunately, I couldn't follow him any further as he almost noticed me, so I backed off for now."),
            3, List.of("The note reads: \"175 42 -470\"",
                    "Not really sure what these numbers mean yet, do you have any ideas?"),
            4, List.of("We need to check this out together. I'll meet you there!"),
            5, List.of("Especially with all those strange machines and experiments. I need your help again... let's meet back in the lab."));

    private BethVisits() {
    }

    public static List<String> offer(int visit) {
        return OFFER.getOrDefault(visit, List.of());
    }

    public static List<String> accept(int visit) {
        return ACCEPT.getOrDefault(visit, List.of());
    }
}
