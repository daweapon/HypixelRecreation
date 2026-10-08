package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crimsonisle.faction.ActiveLine;
import net.swofty.type.crimsonisle.faction.BarbarianScenes;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.skyblockgeneric.crimson.questline.BarbarianStep;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import static net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene.ELLE;
import static net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene.UGO;

// Ugo, Chief Scorn's son, once he has his Sulphur back (wiki: Ugo). His pig form is NPCUgoPig.
public class NPCUgo extends CrimsonNPC {
    private static final Pos CHIEFS_HUT = new Pos(-587.5, 115, -688.5);
    private static final Pos OUTSIDE_HUT = new Pos(-612.5, 107, -688.5);
    private static final Pos TOWN_BOARD = new Pos(-581.5, 100, -684.5);
    private static final Pos WASTELAND = new Pos(-369.5, 117, -977.5);

    public NPCUgo() {
        super(UGO, CrimsonSkins.UGO, TOWN_BOARD);
    }

    @Override
    protected boolean visible(SkyBlockPlayer player) {
        return CrimsonQuestlines.barbarian(player).ordinal() >= BarbarianStep.ESCORT_UGO.ordinal()
                || ActiveLine.atForgottenSkull(player);
    }

    @Override
    protected boolean moves() {
        return true;
    }

    @Override
    protected Pos position(SkyBlockPlayer player) {
        if (ActiveLine.atForgottenSkull(player)) return WASTELAND;
        return switch (CrimsonQuestlines.barbarian(player)) {
            case ESCORT_UGO, PRESENT_RESEARCH -> CHIEFS_HUT;
            case TALK_TO_UGO, MEET_ELLE_AT_CATHEDRAL, STEAL_RESEARCH, RETURN_TO_UGO -> OUTSIDE_HUT;
            default -> TOWN_BOARD;
        };
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (ActiveLine.atForgottenSkull(player)) {
            say(player, "I agree, let's get inside.");
            return;
        }
        switch (CrimsonQuestlines.barbarian(player)) {
            case ESCORT_UGO -> BarbarianScenes.chiefWelcome(player);
            case TALK_TO_UGO -> CrimsonScene.create()
                    .say(UGO, "My leg really hurts.", "It was already quite bad before this fall but now...")
                    .say(ELLE, "Your father sure has some character.")
                    .say(UGO, "He's not that bad, you get used to it.")
                    .say(ELLE, "I don't know what he wants you to do out there, but we can't let you go back there with your injured leg.")
                    .say(UGO, "I'm afraid I have to.")
                    .say(ELLE, "Just tell us what you have to do and we'll do it for you.")
                    .say(UGO, "I can't tell you it's highly confidential.")
                    .say(ELLE, "I don't care, you can't go back out there with a broken leg.")
                    .say(UGO, "<o>Sigh",
                            "My father believes that the <c>Mage Faction <f>is working on some kind of powerful <2>war instrument.",
                            "We know they are hiding something inside their <c>Cathedral<f>.",
                            "My job is to infiltrate their <c>Cathedral <f>and steal their <2>war machine <f>so we can study it.")
                    .say(ELLE, "Say no more, that's a perfect job for <player>.")
                    .say(UGO, "Why them?")
                    .say(ELLE, "Alright let's go.")
                    .play(player)
                    .thenRun(() -> CrimsonQuestlines.advance(player, BarbarianStep.TALK_TO_UGO, BarbarianStep.MEET_ELLE_AT_CATHEDRAL, 0));
            case MEET_ELLE_AT_CATHEDRAL, STEAL_RESEARCH -> sayRandom(player, "Did you even agree to do this?",
                    "Thanks for helping out, and good luck!");
            case RETURN_TO_UGO -> say(player, "You actually did it, I can't thank you enough!", "Let's show it to my father.",
                    "Oh and hum...", "Please don't mention that I didn't get it myself.")
                    .thenRun(() -> CrimsonQuestlines.advance(player, BarbarianStep.RETURN_TO_UGO, BarbarianStep.PRESENT_RESEARCH, 0));
            case PRESENT_RESEARCH -> BarbarianScenes.presentResearch(player);
            case UGO_TOWN_BOARD -> say(player, "This is our <c>Town Board<f>.",
                    "Whenever someone needs something they put a request here and anyone can fulfill it.",
                    "Not enough people are doing them so I usually have to do most requests myself.",
                    "There are also <c>Dojo<f> requests, those are my training sessions...",
                    "I don't think I can do my training with my bad leg.",
                    "<c>Master Tao<f> has terrible sight, maybe you could go to the <c>Dojo<f> and pretend to be me?",
                    "I'll wait for you here.")
                    .thenRun(() -> CrimsonQuestlines.advance(player, BarbarianStep.UGO_TOWN_BOARD, BarbarianStep.DOJO_TRAINING, 0));
            case DOJO_TRAINING -> say(player, "The <c>Dojo<f> is on the opposite side of the island, south of <5>Scarleton<f>, the Mage city.");
            case REPORT_DOJO -> say(player, "How was the training? Not too bad?", "I'm sorry you had to go through this.",
                    "My father wants me to become the next Chief one day.", "I don't really have a choice.",
                    "The <c>Town Board <f>missions get harder the higher your Reputation gets.",
                    "If that's fine with you, I'll give you a couple of my missions, but they are quite tough.",
                    "The first one requires eliminating a <c>Mage Outlaw<f>.",
                    "They are technically Mages, but not part of the Mage Faction, they constantly cause problems.",
                    "The one we need to eliminate is in the <c>Courtyard<f>, it's on the outskirts of <5>Scarleton<f>, towards the north.")
                    .thenRun(() -> CrimsonQuestlines.advance(player, BarbarianStep.REPORT_DOJO, BarbarianStep.KILL_MAGE_OUTLAW, 0));
            case KILL_MAGE_OUTLAW -> sayRandom(player,
                    "Mage Outlaws often attack Piglins, the Wizards don't really want to deal with them so we have to do it ourself.",
                    "The Courtyard is a bit hidden, it is north-west of <5>Scarleton<f>, just outside of the city.");
            case REPORT_MAGE_OUTLAW -> say(player, "You dealt with it? Great!",
                    "You are quite competent, those Mages are usually quite strong, they also think very highly of themselves.",
                    "The next mission is to kill the <c>Magma Boss<f>.",
                    "He's an overgrown Magma Cube that keeps on growing infinitely.",
                    "We have to defeat him every few weeks to keep it in check, otherwise it can grow out of control.",
                    "It's very easy to find the <c>Magma Boss<f>, simply take any of the <7>tunnels below the Volcano <f>and descend until you reach the center.")
                    .thenRun(() -> CrimsonQuestlines.advance(player, BarbarianStep.REPORT_MAGE_OUTLAW, BarbarianStep.KILL_MAGMA_BOSS, 0));
            case KILL_MAGMA_BOSS -> sayRandom(player, "Don't feel too bad about killing those Magma Cubes, they will be born again.",
                    "Once you find the large room below the Volcano, you'll be able to see it.");
            case REPORT_MAGMA_BOSS -> say(player, "I am glad you are back.", "You completed all my missions, I am very grateful.",
                    "Now if you will excuse me, it's time for me to take care of my leg.", "See you around <player>.")
                    .thenRun(() -> CrimsonQuestlines.advance(player, BarbarianStep.REPORT_MAGMA_BOSS, BarbarianStep.HELP_LASEA, 150));
            default -> say(player, "Time to focus on myself.");
        }
    }
}
