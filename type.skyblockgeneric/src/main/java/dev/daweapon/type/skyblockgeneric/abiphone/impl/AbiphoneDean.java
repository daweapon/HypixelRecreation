package dev.daweapon.type.skyblockgeneric.abiphone.impl;

import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonContact;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.crimson.questline.MageStep;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// The Dean of the Academy. Calling him drives several steps of the Mage Faction Questline.
public class AbiphoneDean extends CrimsonContact {
    public AbiphoneDean() {
        super("dean", CrimsonScene.DEAN, "The Dean of the <5>Academy<7> in the Cathedral.",
                "52439b7110e8a1fb9a63e39391eefe58a2e7d048117f4081ebea326644637a0");
    }

    @Override
    protected String[] refusal(SkyBlockPlayer player) {
        if (CrimsonQuestlines.mage(player).ordinal() >= MageStep.BUY_ABIPHONE.ordinal()) return null;
        return new String[]{"Sorry, I only work with Mages..."};
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        switch (CrimsonQuestlines.mage(player)) {
            case BUY_ABIPHONE -> CrimsonScene.create().phone().say(CrimsonScene.DEAN,
                    "Hello?",
                    "Oh, it's you! So the Queen welcomed you in our Faction? That's great",
                    "Listen, I don't have a ton of time right now, the object of my current research has just been stolen!",
                    "I'm sorry, but I'll have to call you back later. Enjoy your Abiphone!"
            ).play(player).thenRun(() -> CrimsonQuestlines.advance(player, MageStep.BUY_ABIPHONE, MageStep.TALK_TO_ELLE, 0));
            case CALL_DEAN -> CrimsonScene.create().phone().say(CrimsonScene.DEAN,
                    "Oh <player> is that you again?",
                    "I am still really stressed out about the long research.",
                    "It's a long story but in short, we believe the Piglins stole it, it's a real problem.",
                    "What did you just say? You recovered it? And they didn't even see you?",
                    "I can't believe it, you are truly a hero! I'm currently near the Community Center, please come here and bring the research back."
            ).play(player).thenRun(() -> CrimsonQuestlines.advance(player, MageStep.CALL_DEAN, MageStep.MAGE_COUNCIL, 0));
            case REPORT_TO_DEAN -> CrimsonScene.create().phone().say(CrimsonScene.DEAN, deanApprenticeNews())
                    .play(player).thenRun(() -> CrimsonQuestlines.advance(player, MageStep.REPORT_TO_DEAN, MageStep.BRING_MATERIALS, 0));
            case HALLOWED_SKULL -> replyRandom(player,
                    "Hallowed Skulls can be obtained from <c>Bladesoul <f>inside the Stronghold.",
                    "The Stronghold was built by Blazes and Wither Skeletons, they are still expanding it slowly, quite fascinating.");
            case LUMINO_FIBER -> replyRandom(player,
                    "The reason why I need a <2>Lumino Fiber<f>? Well...it's hard to explain.",
                    "I have reasons to believe there is a lot of Sulphur in the <c>North<f>, it would explain the appearance of those Blaze species.");
            case TALK_TO_DEAN_FINAL -> CrimsonScene.create().phone().say(CrimsonScene.DEAN, deanAfterKuudra())
                    .play(player).thenRun(() -> CrimsonQuestlines.advance(player, MageStep.TALK_TO_DEAN_FINAL, MageStep.COMPLETE, 0));
            case COMPLETE -> replyRandom(player, "This monster probably feeds itself with Sulphur.",
                    "If left unchecked, it is possible that the powers of this creature will grow.");
            default -> replyRandom(player, "I am busy right now.", "There is still so much to learn about Sulphur.");
        }
    }

    public static String[] deanApprenticeNews() {
        return new String[]{
                "You found my Apprentice and he WHAT?!",
                "SULPHUR! This is catastrophic!!",
                "My poor Apprentice, it is my vault, I shouldn't send him on difficult missions.",
                "This will also delay my work considerably, how am I going to obtain all those precious materials now...",
                "Oh, you are willing to help?",
                "Well in that case, I will send you the list of items I need.",
                "Thank you very much you are a life savior, quite literally."
        };
    }

    public static String[] deanAfterKuudra() {
        return new String[]{
                "Hello <player>?",
                "You found who stole our Sulphur?",
                "A giant magma creature with massive tentacles?",
                "Well that explains a lot, my Apprentice just awoken from his coma, he described the same creature.",
                "In a way it is good that the Piglins were not behind this.",
                "We need to learn more about that monster, where it is from and what it wants.",
                "I will call you later!"
        };
    }
}
