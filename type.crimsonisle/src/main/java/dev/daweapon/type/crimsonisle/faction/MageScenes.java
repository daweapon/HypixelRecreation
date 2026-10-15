package dev.daweapon.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.crimson.questline.MageStep;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import static dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene.CAPTAIN_AHONE;
import static dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene.DEAN;
import static dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene.ELLE;
import static dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene.QUEEN_NYX;

/** Shared scenes of the Mage Faction Questline (wiki: Dean, Queen Nyx, Captain Ahone, Elle). */
public final class MageScenes {
    public static final Pos BASTION_RESEARCH = new Pos(-684, 124, -1048);

    private MageScenes() {
    }

    /** The Mage Council meeting below the Community Center (+100 Mage Reputation). */
    public static void council(SkyBlockPlayer player) {
        if (CrimsonScene.isPlaying(player) || CrimsonQuestlines.mage(player) != MageStep.MAGE_COUNCIL) return;
        CrimsonScene.create()
                .say(DEAN, "Oh, <player> is here! They're the one who retrieved the research!")
                .say(QUEEN_NYX, "I see, we owe you adventurer, this object is of great value to us.")
                .say(DEAN, "Yes indeed! With it we are able to extract salt from Netherrack, it will be a massive asset when cooking.",
                        "You see, while we evolved past the need for crops and cattle, the quality of our food remains low.",
                        "I would say close to 50% of our research is still spent on food artifacts and machines.")
                .say(CAPTAIN_AHONE, "And once again those Piglins stole our research.", "They believe we are working on war engines.",
                        "The tension is rising and soon we might have to cross swords with them.")
                .say(QUEEN_NYX, "Captain Ahone, this isn't a conversation we should have right now.",
                        "Thank you very much, <player>. Would you mind leaving us now, we have matters to discuss.")
                .say(DEAN, "<player> please meet me back at the Academy, we have a lot to talk about!")
                .play(player)
                .thenRun(() -> CrimsonQuestlines.advance(player, MageStep.MAGE_COUNCIL, MageStep.RETURN_TO_ACADEMY, 100));
    }

    /** Recovering the research from The Bastion; Elle phones straight after. */
    public static void researchRecovered(SkyBlockPlayer player) {
        if (!CrimsonQuestlines.advance(player, MageStep.RECOVER_RESEARCH, MageStep.CALL_DEAN, 0)) return;
        player.sendMessage("<a>You recovered the stolen research!");
        CrimsonScene.create()
                .raw("<e>✆<c> Elle<f>: <e>✆")
                .raw("<a>✆ Ring... Ring... Ring...")
                .say(ELLE, "<b>✆ <f>Hello? Oh, you are still alive, awesome!",
                        "<b>✆ <f>Have you got the research back or did you chicken out?",
                        "<b>✆ <f>Great! You are cooler than you look!",
                        "<b>✆ <f>Go back to the Dean then, I'm sure this will grant you a lot of reputation!",
                        "<b>✆ <f>Alright then! I have to go now, was nice talking to you!")
                .play(player);
    }
}
