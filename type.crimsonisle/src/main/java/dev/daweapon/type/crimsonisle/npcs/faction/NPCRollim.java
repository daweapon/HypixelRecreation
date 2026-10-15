package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.crimsonisle.faction.DuelistNPC;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;

// Rollim trains with the player for a week before his duel with a Piglin (wiki: Rollim).
public class NPCRollim extends DuelistNPC {
    private static final String[][] INVITATIONS = {
            {"I am really stressing out...", "Yesterday I had a heated argument with a Piglin and we agreed to have a duel in a week.",
                    "The problem is that I'm not a fighter, at ALL!",
                    "Would you mind helping me train a little bit? You look like a seasoned veteran, no offense."},
            {"I'm ready to be defeated again... Are you ready?"},
            {"I barely recovered from the last training session but I'm ready, are you?"},
            {"I can feel that I'm slowly growing stronger, are you ready for today's training session?"},
            {"Only 3 days left to train, are you ready?"},
            {"I feel so strong today, are you ready?"},
            {"It has already been a week, time flies!", "Are you ready for my last training session?"}};
    private static final String[][] AFTER = {
            {"Holy crap you are strong.", "That was embarrassing.", "Please come back tomorrow and help me train again!"},
            {"That really hurts, but I need to get stronger at ALL COST!", "Please come back tomorrow."},
            {"Wow!", "You are truly on another level...", "Please come back tomorrow."},
            {"You still beat me effortlessly...", "I really hope I'll be ready on time.", "Please come back tomorrow."},
            {"That was intense!", "We are getting there!", "Please come back tomorrow."},
            {"I really don't deserve such a skilled trainer.", "Please come back tomorrow."},
            {"I've grown so much over the last week thanks to you.", "Just a week ago I was stressed and scared, now I feel confident.",
                    "I am finally ready for my duel.", "Please accept this, I can't thank you enough.", "I'll prepare for my duel now.",
                    "I think I'll become a motivational speaker.", "Crazy to think I could improve so much in a week.",
                    "I want to improve every day, for the rest of my life."}};

    public NPCRollim() {
        super("<5>Rollim", "Rollim", CrimsonSkins.ROLLIM, new Pos(-147, 106, -854), CrimsonFaction.MAGE);
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
        return new String[]{"In that case, please show me your fighting moves.",
                "Don't go easy on me, I'm very bad but I only have a week to improve."};
    }

    @Override
    protected String countdownEnd() {
        return "En garde!";
    }

    @Override
    protected String declined() {
        return "Welp, no hard feelings, I guess.";
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
