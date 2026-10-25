package dev.daweapon.type.skyblockgeneric.greatspook;

import java.util.List;

public final class PrimalFearDialogue {
    public static final List<String> DEADLINE = List.of(
            "Every heartbeat a silent echo in eternity's corridor.",
            "In stillness, perceive the dance of fleeting seconds.",
            "The sands flow, even as you stand still.",
            "Every second a choice, every moment a consequence.",
            "Frozen in the now, yet time marches on.",
            "Awaiting the inevitable tick that follows the tock.",
            "Time is both jailer and liberator.",
            "Endings and beginnings, bound by my rhythm.",
            "Time's embrace tightens with each passing second.",
            "Do you hear the whisper of hours long past?",
            "Moments lost, never to return.",
            "You may stop, but I never will.",
            "Stillness won't halt the relentless forward push.",
            "Each pause, a testament to moments gone by.");

    public static final List<String> DARKNESS_SHADE = List.of(
            "The darkness consumes all.",
            "Lost in the void?",
            "Guess who's behind you...",
            "You can't run from the unseen.",
            "Embrace the night.",
            "Shadows never lie.",
            "Closer than you think...",
            "Where am I?",
            "Can't see me, can you?",
            "Feeling vulnerable?",
            "BOO");

    public static final List<String> COMMITMENT_SIGNED = List.of(
            "The spectres of commitment watch over you.",
            "Your echo will resonate in the void.",
            "Your fate is now intertwined with the unseen.",
            "Bound by ink, sealed by destiny.",
            "You've danced with the shadows; may they be kind to you.",
            "By your hand, the silent pact is forged.",
            "May you find solace in the choices you've sealed.",
            "Ink fades, but consequences linger eternally.",
            "Your signature resonates in the silent abyss.",
            "The chains of eternity bind you.");

    public static final List<String> DOCUMENTS = List.of(
            "Skill Swap Scroll", "Alliance Pact", "Debt Bond", "Eternal Obligation Scroll",
            "Pact of the Shadow Realm", "Soul Pact", "Quest Contract");

    public static final List<String> SPEAK = List.of("Speak <7>{}<f>!", "Say something interesting <7>{}<r>!");

    public static final String COMMITMENT_ASK = "Please sign this paper!";
    public static final String COMMITMENT_OUTDATED = "Trying to pull a fast one? That's not even the paper I gave you!";
    public static final String SPEAK_TOO_SHORT = "Too short! Say more!";
    public static final String SPEAK_DONE = "Woohoo! Thank you for speaking out loud!";
    public static final int MINIMUM_SPEECH_CHARACTERS = 10;

    private PrimalFearDialogue() {
    }

    public static String line(Manifestation manifestation, String text) {
        return "<4>[FEAR] " + manifestation.displayName() + "<r>: " + text;
    }
}
