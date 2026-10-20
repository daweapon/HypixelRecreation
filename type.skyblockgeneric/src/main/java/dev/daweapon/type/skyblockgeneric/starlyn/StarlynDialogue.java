package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class StarlynDialogue {
    private StarlynDialogue() {
    }

    public static String speaker(StarlynSister sister) {
        return "<e>[NPC] " + sister.npcName() + "<f>: ";
    }

    public static String[] gateLines(StarlynContests.Gate gate, String username) {
        return switch (gate) {
            case NEEDS_HOTF -> new String[]{"<7>" + username + "<f>! Have you talked to <b>Hina <f>at the front of the island to get your hands on a <a>Heart of the Forest<f>?"};
            case NEEDS_SWOOP -> new String[]{
                    "I talked to <b>Swoop <f>the other day, he explained a lot of interesting <a>Foraging Mechanics <f>to me.",
                    "Perhaps you should talk to him too, he's at the <d>entrance of the village<f>!"};
            case NEEDS_DAVID -> new String[]{"<a>Hunting <f>is incredible! You should talk to <b>David Hunterborough <f>in the Village to learn about it!"};
            case NEEDS_KYSHA -> new String[]{"Have you talked to <b>Kysha<f>, she knows all about <5>Fusions<f>!"};
            case READY -> new String[0];
        };
    }

    public static String[] agathaIntro() {
        return new String[]{
                "Hello, aspiring forager!",
                "I'm Agatha, the youngest of the <d>Starlyn Sisters<f>.",
                "We each host contests on our own islands.",
                "Here in <2>Moonglade Marsh<f>, I'll reward your daily activities each SkyBlock day.",
                "So stick around, and please, avoid wandering into my sisters' territories."};
    }

    public static void mangroveWarning(SkyBlockPlayer player) {
        player.sendMessage(speaker(StarlynSister.AGATHA) + "Hey! You should probably stick to <a>Fig Trees<f>!");
        player.sendMessage(speaker(StarlynSister.AGATHA) + "<a>Mangrove Trees <f>are too tough for you yet, try getting at least <2>50∮ Sweep<f>!");
    }

    public static List<String> hiddenBracket(StarlynSister sister, StarlynBracket bracket) {
        if (sister != StarlynSister.MIRIA) return List.of();
        String prefix = speaker(sister);
        return switch (bracket) {
            case MYTHIC -> List.of(prefix + "Huh? You reached my hidden <d><l>MYTHIC <r><f>bracket already? You're sure good at this Foraging business!");
            case DIVINE -> List.of(prefix + "Whaaat? You reached my hidden <b><l>DIVINE <r><f>bracket? Great job - keep it going!");
            case SPECIAL -> List.of(prefix + "You found my hidden <c><l>SPECIAL <r><f>bracket? Wow, my sister was right about you!");
            default -> List.of();
        };
    }

    public static String[] miriaContactRefusal() {
        return new String[]{
                "Nice to see ya!",
                "Hmm, you want my <3>Abiphone Contact<f>?",
                "Sure, I guess I can do that...",
                "But only if you reach the <6><l>LEGENDARY <r><f>bracket in my contest first!"};
    }

    public static String[] miriaContactBefore() {
        return new String[]{
                "Nice to see ya!",
                "Hmm, you want my <3>Abiphone Contact<f>?",
                "Sure, I guess I can do that...",
                "But only if you reach the <6><l>LEGENDARY <r><f>bracket in my contest first!",
                "...",
                "Wait...",
                "You already reached it?",
                "<c><l>HOW?",
                "Deal's a deal, I guess.",
                "Here's my contact."};
    }

    public static String[] miriaContactAfter() {
        return new String[]{
                "I'll give you a call whenever you finish a <b>Miria's Contest<f> so that you can claim your <6>rewards<f>.",
                "...Or you can call me anytime to view my <e>Shop<f>!"};
    }

    public static String[] agathaContactBefore() {
        return new String[]{
                "Hello again!",
                "You've been doing well in my contests recently, don't think I haven't noticed!",
                "Oh, my <3>Abiphone Contact<f>?",
                "Sure, I have some space."};
    }

    public static String[] agathaContactAfter() {
        return new String[]{
                "I'll give you a call whenever you finish an <d>Agatha's Contest <f>so that you can claim your <6>rewards<f>."};
    }
}
