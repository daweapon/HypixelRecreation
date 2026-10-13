package net.swofty.type.crystalhollows.npcs;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

public class NPCKalhuikiDoorGuardian extends AbstractStructureNPC {
    public NPCKalhuikiDoorGuardian() {
        super("Kalhuiki Door Guardian", "<b>", DEFAULT_TEXTURE, "jungle_temple", "door_guardian");
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        UUID id = player.getUuid();
        if (CrystalHollowsProgress.hasActiveJungleKey(id)) {
            setDialogue(player, "open");
            return;
        }
        if (player.getAmountAnywhere(ItemType.JUNGLE_KEY) > 0) {
            player.removeItemFromPlayer(ItemType.JUNGLE_KEY, 1);
            CrystalHollowsProgress.mark(id, CrystalHollowsProgress.JUNGLE_KEY_ACTIVE);
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
                        "The temple is sealed. Only those who carry a Jungle Key may pass."
                ).build(),
                DialogueSet.builder().key("accepted").lines(
                        "A Jungle Key! I will open the door for you."
                ).build(),
                DialogueSet.builder().key("open").lines(
                        "Your key is already recognised. The door is open, go on in."
                ).build()
        };
    }
}
