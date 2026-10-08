package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.crimsonisle.faction.DuelistNPC;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;

// Kaus trains with the player for a week before his duel with a Mage (wiki: Kaus).
public class NPCKaus extends DuelistNPC {
    private static final String[][] INVITATIONS = {
            {"A week...I only have a week...", "I have beef with a mage and we agreed to have a duel in a week.",
                    "Why did I agree to this? I've never fought in my life!",
                    "Would you mind helping me train? You would save my life!"},
            {"I'm ready to fight again, are you also ready?"},
            {"I'm ready for my lesson, are you?"},
            {"I can tell I'm improving, let's keep going, shall we?"},
            {"Only 3 more training sessions, are you ready?"},
            {"I had a good night's sleep. I'm so ready for this session, are you?"},
            {"7 days, 7 lessons.", "Are you ready for my last training session?"}};
    private static final String[][] AFTER = {
            {"I got one tapped...", "That's disturbing.", "Please come back tomorrow and help me train again!"},
            {"You are beating me too easily, this isn't helping with my insecurity...", "Please come back tomorrow."},
            {"My lord...", "Just how weak am I?", "Please come back tomorrow."},
            {"You are the best teacher I could have hoped for...", "I really hope I'll be ready on time.", "Please come back tomorrow."},
            {"I'm getting better! Let's go!!", "We are getting there!", "Please come back tomorrow."},
            {"What a match, I truly enjoy sparring with you.", "Please come back tomorrow."},
            {"I am so happy with the improvements I've made this week, and it's all thanks to you.",
                    "Just a week ago I was feeling insecure, now I'm going to this duel with confidence.",
                    "I am finally ready for my duel.", "Please accept this, I can't thank you enough.", "I'll prepare for my duel now.",
                    "I think I'll become a motivational speaker.", "Crazy to think I could improve so much in a week.",
                    "I want to improve every day, for the rest of my life."}};

    public NPCKaus() {
        super("<d>Kaus", "Kaus", CrimsonSkins.KAUS, new Pos(-596, 113, -640), CrimsonFaction.BARBARIAN);
    }

    @Override
    protected String[] invitation(int day) {
        return INVITATIONS[day - 1];
    }

    @Override
    protected String[] afterFight(int day) {
        return AFTER[Math.min(day, AFTER.length) - 1];
    }

    @Override
    protected String[] firstYes() {
        return new String[]{"Great! Let's do this then!", "Don't go easy on me, I'm very bad but I only have a week to improve."};
    }

    @Override
    protected String countdownEnd() {
        return "Let's fight!";
    }

    @Override
    protected String declined() {
        return "Heck, I'm in trouble then.";
    }

    @Override
    protected String[] afterTraining() {
        return new String[]{"Hello!", "I went to the duel yesterday but we both agreed not to fight.",
                "We both became much stronger during the last week, but strength should not be used for violence.",
                "I spent a week training my body, but I think my mind improved even more.",
                "I feel so much more sure of myself! I've grown beyond my old futile conflicts.",
                "This was a life changing experience."};
    }

    @Override
    protected String notMember() {
        return "Ohh man I'm in trouble! If you were part of my <e>faction<f>, maybe you could help me.";
    }

    @Override
    protected String otherFaction() {
        return "Eek, you remind me of someone I need to duel in a week! If you weren't in his <e>faction<f>, maybe you could help me...";
    }
}
