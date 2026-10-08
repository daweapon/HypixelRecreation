package net.swofty.type.skyblockgeneric.abiphone.impl;

import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonContact;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Captain Ahone of the Mage guard. His contact comes from the search for the Dean's Apprentice.
public class AbiphoneCaptainAhone extends CrimsonContact {
    public AbiphoneCaptainAhone() {
        super("captain_ahone", CrimsonScene.CAPTAIN_AHONE, "Captain of the <5>Mage Faction<7>'s guard.",
                "e21ab4ac2fe5b449f32db4ea5609aee796794e1157764c57928f88610ca31884");
    }

    @Override
    protected String[] refusal(SkyBlockPlayer player) {
        if (CrimsonQuestlines.mage(player).ordinal() >= MageStep.FIND_APPRENTICE.ordinal()) return null;
        return new String[]{"Our relations with the Piglins is getting worse, things are spiraling out of control."};
    }

    @Override
    protected void call(SkyBlockPlayer player) {
        switch (CrimsonQuestlines.mage(player)) {
            case CALL_AHONE -> CrimsonScene.create().phone()
                    .say(CrimsonScene.CAPTAIN_AHONE, "You found the Apprentice?", "Good work. We will come to you at once!")
                    .play(player)
                    .thenCompose(_ -> CrimsonScene.create().say(CrimsonScene.CAPTAIN_AHONE,
                            "Looks like we found him just in time, he will live, but he needs medical attention.",
                            "I know the Dean's Apprentice, they would never take that much damage from the monsters around here.",
                            "Once they wake up from their coma we will know what happened to them, but I have my suspicions.",
                            "The guards and I will bring him back to the city, please explain the situation to the Dean."
                    ).play(player))
                    .thenRun(() -> CrimsonQuestlines.advance(player, MageStep.CALL_AHONE, MageStep.REPORT_TO_DEAN, 0));
            case TALK_TO_AHONE -> CrimsonScene.create().phone().say(CrimsonScene.CAPTAIN_AHONE, ahoneSulphurStolen())
                    .play(player)
                    .thenRun(() -> CrimsonQuestlines.advance(player, MageStep.TALK_TO_AHONE, MageStep.MEET_AT_FORGOTTEN_SKULL, 0));
            case MEET_AT_FORGOTTEN_SKULL -> replyRandom(player,
                    "The Forgotten Skull in the North is close to the Barbarian borders, they are probably hiding the stolen Sulphur there.",
                    "Some matters cannot be solved peacefully.");
            case COMPLETE -> reply(player, "Thank you for helping with the tentacle monster.",
                    "We owe the Piglins an apology.", "I will stay in Scarleton and prepare for the next battle!");
            default -> reply(player, "He might be nearby, let me know if you find anything.");
        }
    }

    public static String[] ahoneSulphurStolen() {
        return new String[]{
                "<player>! Perfect timing",
                "Our entire stash of Sulphur got stolen. I have no idea how \"they\" did it.",
                "The Queen is warning me against hasty conclusions, but enough is enough.",
                "We cannot let the Barbarians trespass our borders without retaliation.",
                "My scouts told me they sighted Barbarian activities in the <c>North, near the Forgotten Skull<f>.",
                "Let's meet again there, and be prepared for combat."
        };
    }
}
