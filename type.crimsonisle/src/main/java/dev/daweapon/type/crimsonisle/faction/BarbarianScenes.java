package dev.daweapon.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.questline.BarbarianStep;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import static dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene.CHIEF_SCORN;
import static dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene.ELLE;
import static dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene.UGO;

/** The Chief's Hut scenes of the Barbarian Faction Questline (wiki: Chief Scorn, Ugo). */
public final class BarbarianScenes {
    private BarbarianScenes() {
    }

    /** Ugo is brought home: Chief Scorn scolds him and welcomes the player into the faction. */
    public static void chiefWelcome(SkyBlockPlayer player) {
        if (CrimsonScene.isPlaying(player) || CrimsonQuestlines.barbarian(player) != BarbarianStep.ESCORT_UGO) return;
        CrimsonScene.create()
                .say(CHIEF_SCORN, "<f><l>Ugo, where have you been?").say(UGO, "I-")
                .say(CHIEF_SCORN, "<f><l>You had ONE job!", "<f><l>3 days later you come back with empty hands and a broken leg!?")
                .say(UGO, "Well-")
                .say(CHIEF_SCORN, "<f><l>And you two, what are your names?").say(ELLE, "I'm-")
                .say(CHIEF_SCORN, "<f><l>Thank you for saving my rascal of a son.", "<f><l>Are you looking to join our ranks?")
                .say(ELLE, "No but-")
                .say(CHIEF_SCORN, "<f><l>Perfect!", "<f><l>I welcome you both to the Barbarian Faction!", "<f><l>If you betray us you'll die.")
                .say(ELLE, "I can't-")
                .say(CHIEF_SCORN, "<f><l>Enough talking!", "<f><l>Ugo, go back out there and finish the job!")
                .say(UGO, "But-")
                .say(CHIEF_SCORN, "<f><l>Guards, escort them out of my Hut!")
                .play(player)
                .thenRun(() -> {
                    if (!CrimsonQuestlines.advance(player, BarbarianStep.ESCORT_UGO, BarbarianStep.TALK_TO_UGO, 0)) return;
                    CrimsonReputationService.setFaction(player, CrimsonFaction.BARBARIAN);
                    player.sendMessage("<a>You joined the " + CrimsonFaction.BARBARIAN.coloured() + " Faction<a>!");
                });
    }

    /** The research under the table in the Dean's office (estimated spot); Elle phones straight after. */
    public static final Pos DEAN_OFFICE_RESEARCH = new Pos(-16.5, 123, -884.5);

    public static void researchStolen(SkyBlockPlayer player) {
        if (!CrimsonQuestlines.advance(player, BarbarianStep.STEAL_RESEARCH, BarbarianStep.RETURN_TO_UGO, 0)) return;
        player.sendMessage("<a>You stole the research from the Dean's office!");
        CrimsonScene.create()
                .raw("<e>✆<c> Elle<f>: <e>✆")
                .raw("<a>✆ Ring... Ring... Ring...")
                .say(ELLE, "<b>✆ <f>You got the right item?", "<b>✆ <f>Are you sure?",
                        "<b>✆ <f>Okay great, let's leave this place. I have Mycelium all over my shoes.",
                        "<b>✆ <f>Let's talk to Ugo again.", "<b>✆ <f>I'll use a Travel Scroll, see you there.")
                .play(player);
    }

    public static void presentResearch(SkyBlockPlayer player) {
        CrimsonScene.create()
                .say(CHIEF_SCORN, "<f><l>Ugo why are you limping?").say(UGO, "My le-")
                .say(CHIEF_SCORN, "<f><l>Have you found the evil wizard machine?").say(UGO, "Y-")
                .say(CHIEF_SCORN, "<f><l>Give it to the guards, we will investigate it.", "<f><l>Enough with the mages and their shenanigans.")
                .say(UGO, "Alri-")
                .say(CHIEF_SCORN, "<l>The Town Board is filling up and <l>you are late on your Training Ugo.", "<f><l>What have you been doing?")
                .say(UGO, "You-")
                .say(CHIEF_SCORN, "<f><l>Hurry up, failure isn't an option.")
                .say(UGO, "Ugh", "I completely forgot about my Training.", "I have to go to the Town Board.")
                .play(player)
                .thenRun(() -> CrimsonQuestlines.advance(player, BarbarianStep.PRESENT_RESEARCH, BarbarianStep.UGO_TOWN_BOARD, 100));
    }

    public static void sulphurStolen(SkyBlockPlayer player) {
        CrimsonScene.create().say(CHIEF_SCORN,
                        "<f><l>Newest member of the Faction!",
                        "<f><l>The entire stash of Sulphur of the Piglin Faction has been stolen this morning.",
                        "<f><l>I sent my son Ugo on a mission to recover the Sulphur.",
                        "<f><l>He might be fighting the Mage Faction as we speak.",
                        "<f><l>I implore you. Please assist Ugo in this operation.",
                        "<f><l>I don't know what I would do without my son. My only son.",
                        "<f><l>They have followed the traces of the Sulphur all the way to the Forgotten Skull in the North.")
                .play(player)
                .thenRun(() -> CrimsonQuestlines.advance(player, BarbarianStep.TALK_TO_CHIEF, BarbarianStep.MEET_AT_FORGOTTEN_SKULL, 0));
    }

    public static void afterKuudra(SkyBlockPlayer player) {
        CrimsonScene.create()
                .say(CHIEF_SCORN, "<f><l>You finally retrieved the Sulphur.", "<f><l>What took you so long?")
                .say(UGO, "A gian-")
                .say(CHIEF_SCORN, "<f><l>You even needed the help of those adventurers.", "<f><l>Do you have no shame?")
                .say(UGO, "I didn't-")
                .say(CHIEF_SCORN, "<f><l>Look at the time. You are late to your training.",
                        "<f><l>Guards, escort my son out of the hut.",
                        "<player> <f><l>I am in your debt.",
                        "<f><l>I cherish Ugo more than anything else in this world.",
                        "<f><l>A father could not be prouder of his son.",
                        "<f><l>My mind is at peace knowing you are by his side.",
                        "<f><l>More importantly, I am glad he finally met someone he can consider his friend.",
                        "<f><l>I taught him everything I could, but there are things only friends can teach.",
                        "<f><l>I won't hold you any longer.",
                        "<f><l>Good luck in your adventures.")
                .play(player)
                .thenRun(() -> CrimsonQuestlines.advance(player, BarbarianStep.TALK_TO_CHIEF_FINAL, BarbarianStep.COMPLETE, 200));
    }
}
