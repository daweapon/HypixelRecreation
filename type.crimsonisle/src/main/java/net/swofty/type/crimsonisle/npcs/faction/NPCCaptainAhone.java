package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crimsonisle.faction.ActiveLine;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.crimsonisle.faction.CrimsonSkins;
import net.swofty.type.crimsonisle.faction.MageScenes;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.skyblockgeneric.abiphone.impl.AbiphoneCaptainAhone;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonAbiphone;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Captain Ahone of the Mage guard; appears where the Mage questline needs him (wiki: 5 spots).
public class NPCCaptainAhone extends CrimsonNPC implements NPCAbiphoneTrait {
    private static final Pos MAGE_COUNCIL = new Pos(-121.5, 82, -756.5);
    private static final Pos BURNING_DESERT = new Pos(-526.5, 86, -885.5);
    private static final Pos WASTELAND = new Pos(-373.5, 117, -977.5);

    public NPCCaptainAhone() {
        super(CrimsonScene.CAPTAIN_AHONE, CrimsonSkins.CAPTAIN_AHONE, MAGE_COUNCIL);
    }

    @Override
    public String getAbiphoneKey() {
        return "captain_ahone";
    }

    @Override
    protected boolean moves() {
        return true;
    }

    @Override
    protected boolean visible(SkyBlockPlayer player) {
        if (ActiveLine.atForgottenSkull(player)) return true;
        MageStep step = CrimsonQuestlines.mage(player);
        return step == MageStep.MAGE_COUNCIL || step == MageStep.FIND_AHONE || step == MageStep.FIND_APPRENTICE
                || step == MageStep.TALK_TO_AHONE;
    }

    @Override
    protected Pos position(SkyBlockPlayer player) {
        if (ActiveLine.atForgottenSkull(player)) return WASTELAND;
        MageStep step = CrimsonQuestlines.mage(player);
        return step == MageStep.FIND_AHONE || step == MageStep.FIND_APPRENTICE ? BURNING_DESERT : MAGE_COUNCIL;
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (ActiveLine.atForgottenSkull(player)) {
            sayRandom(player, "Some matters cannot be solved peacefully.", "This giant tentacle monster story is real?");
            return;
        }
        switch (CrimsonQuestlines.mage(player)) {
            case MAGE_COUNCIL -> MageScenes.council(player);
            case FIND_AHONE -> say(player, "You are looking for the Dean's Apprentice?",
                    "I'm afraid he might be in great danger, I just found his backpack and there is some <c>blood <f>on it...",
                    "We must find him at once, I really hope this isn't the Piglin's doing.",
                    "Let's separate. Take my <a>contact <f>and call me if you find anything.")
                    .thenRun(() -> {
                        if (CrimsonQuestlines.advance(player, MageStep.FIND_AHONE, MageStep.FIND_APPRENTICE, 0)) {
                            CrimsonAbiphone.grant(player, "captain_ahone");
                        }
                    });
            case FIND_APPRENTICE -> say(player, "He might be nearby, let me know if you find anything.");
            case TALK_TO_AHONE -> say(player, AbiphoneCaptainAhone.ahoneSulphurStolen())
                    .thenRun(() -> CrimsonQuestlines.advance(player, MageStep.TALK_TO_AHONE, MageStep.MEET_AT_FORGOTTEN_SKULL, 0));
            default -> say(player, "Our relations with the Piglins is getting worse, things are spiraling out of control.");
        }
    }
}
