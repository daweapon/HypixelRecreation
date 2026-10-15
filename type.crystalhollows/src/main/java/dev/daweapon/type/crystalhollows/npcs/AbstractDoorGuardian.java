package dev.daweapon.type.crystalhollows.npcs;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.crystalhollows.quest.CrystalHollowsProgress;
import dev.daweapon.type.crystalhollows.temple.JungleTemple;
import dev.daweapon.type.crystalhollows.temple.TempleDoor;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

public abstract class AbstractDoorGuardian extends AbstractStructureNPC {
    protected AbstractDoorGuardian(String anchor) {
        super("Kalhuiki Door Guardian", "<b>", KALHUIKI_TEXTURE, JungleTemple.STRUCTURE, anchor);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        UUID id = player.getUuid();
        if (CrystalNucleus.state(player, NucleusCrystal.AMETHYST) != CrystalNucleus.State.NOT_FOUND) {
            setDialogue(player, "claimed");
            return;
        }
        if (CrystalHollowsProgress.hasActiveJungleKey(id)) {
            TempleDoor.keyAccepted(id);
            setDialogue(player, "open");
            return;
        }
        if (player.getAmountAnywhere(ItemType.JUNGLE_KEY) > 0) {
            player.removeItemFromPlayer(ItemType.JUNGLE_KEY, 1);
            CrystalHollowsProgress.mark(id, CrystalHollowsProgress.JUNGLE_KEY_ACTIVE);
            TempleDoor.keyAccepted(id);
            setDialogue(player, "accepted");
            player.sendMessage(Text.of("<a>The door opens!"));
            return;
        }
        setDialogue(player, "locked");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("locked").lines(
                        "This temple is locked, you will need to bring me a key open the door!"
                ).build(),
                DialogueSet.builder().key("accepted").lines(
                        "A Jungle Key! I will open the door for you."
                ).build(),
                DialogueSet.builder().key("open").lines(
                        "Your key is already recognised. The door is open, go on in."
                ).build(),
                DialogueSet.builder().key("claimed").lines(
                        "This temple does not have the treasure you are seeking at this time."
                ).build()
        };
    }
}
