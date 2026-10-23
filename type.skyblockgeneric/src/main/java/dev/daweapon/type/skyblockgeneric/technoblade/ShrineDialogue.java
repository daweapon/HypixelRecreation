package dev.daweapon.type.skyblockgeneric.technoblade;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ShrineDialogue {
    private static final List<String> WHISPERS = List.of(
            "The resistance leader is a follower of the <c>blood god<f>.",
            "As mayor, he built this shrine to its magnificence.",
            "The community shall find the <d>7 Pig Relics<f> and donate them to the shrine.",
            "Purchase items from the <6>Technoshop<f> to prepare for the hunt.",
            "The relics are hidden across the Hub. Whoever finds one claims it for the shrine.");

    private ShrineDialogue() {
    }

    public static String whisper() {
        return "<d>[SHRINE] Whispers<f>: " + WHISPERS.get(ThreadLocalRandom.current().nextInt(WHISPERS.size()));
    }

    public static List<String> ancientPig(AncientPigEffect current) {
        return List.of(
                "<e>[NPC] <6>Ancient Pig<f>: The <d>7<f> Pig Relics have been found, therefore I am.",
                "<e>[NPC] <6>Ancient Pig<f>: People call me the Ancient Pig, and I bring good fortune in the land.",
                "<e>[NPC] <6>Ancient Pig<f>: A random effect will appear every <b>20<f> minutes for the next <b>2 days<f>.",
                "<e>[NPC] <6>Ancient Pig<f>: The current effect is <6>" + (current == null ? "not chosen yet" : current.displayName()) + "<f>.");
    }

    public static List<String> technoshopPig() {
        return List.of(
                "<e>[NPC] <d>Pig<f>: Oink! Welcome to the <6>Technoshop<f>.",
                "<e>[NPC] <d>Pig<f>: Mayor <b>Technoblade<f> says the blood god is pleased with the community.");
    }
}
