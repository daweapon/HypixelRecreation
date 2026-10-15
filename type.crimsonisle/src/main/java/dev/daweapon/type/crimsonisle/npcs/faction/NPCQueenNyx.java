package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.crimsonisle.faction.CrimsonChoices;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.crimsonisle.faction.MageScenes;
import dev.daweapon.type.crimsonisle.gui.faction.GUIPresentItem;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.crimson.questline.MageStep;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Queen Nyx, leader of the Mage Faction, in the Throne Room. The Dean's letter gets the player into the faction.
public class NPCQueenNyx extends CrimsonNPC implements NPCAbiphoneTrait {
    private static final Pos THRONE = new Pos(-117.5, 105, -754.5, 90, 0);
    private static final Pos MAGE_COUNCIL = new Pos(-120.5, 82, -754.5);

    public NPCQueenNyx() {
        super(CrimsonScene.QUEEN_NYX, CrimsonSkins.QUEEN_NYX, THRONE);
    }

    @Override
    protected boolean moves() {
        return true;
    }

    @Override
    protected Pos position(SkyBlockPlayer player) {
        return CrimsonQuestlines.mage(player) == MageStep.MAGE_COUNCIL ? MAGE_COUNCIL : THRONE;
    }

    @Override
    public String getAbiphoneKey() {
        return "queen_nyx";
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        MageStep step = CrimsonQuestlines.mage(player);
        if (step == MageStep.MAGE_COUNCIL) {
            MageScenes.council(player);
            return;
        }
        if (step != MageStep.DELIVER_LETTER) {
            sayRandom(player, "The Mage Faction is growing, we will soon uncover all the secrets of Mana.",
                    "I can sense great power in you.");
            return;
        }
        say(player, "What does a stranger have to do with the <c>Taika <f>people? Speak!")
                .thenRun(() -> CrimsonChoices.show(player, "queen_nyx", "I have something to give you:",
                        () -> player.openView(new GUIPresentItem(ItemType.DEAN_LETTER_OF_RECOMMENDATION, () -> welcome(player)))));
    }

    private void welcome(SkyBlockPlayer player) {
        if (!CrimsonQuestlines.advance(player, MageStep.DELIVER_LETTER, MageStep.BUY_ABIPHONE, 0)) return;
        player.removeItemFromPlayer(ItemType.DEAN_LETTER_OF_RECOMMENDATION, 1);
        say(player, "A letter of recommendation from the <c>Dean<f>, let me read this.", "...",
                "Intriguing to say the least.",
                "I have the upmost faith in our <c>Dean<f>, if they value your company then I'll welcome you to our faction.",
                "But if you are up to no good, I'll revoke this access immediately.",
                "Now go, and enjoy our great city, new Faction member!")
                .thenRun(() -> {
                    CrimsonReputationService.setFaction(player, CrimsonFaction.MAGE);
                    player.sendMessage("<a>You joined the " + CrimsonFaction.MAGE.coloured() + " Faction<a>!");
                });
    }
}
