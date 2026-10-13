package net.swofty.type.crystalhollows.npcs;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCXalx extends AbstractStructureNPC {
    private static final String MEAT_GIVEN = "xalx_meat_given";

    public NPCXalx() {
        super("Xalx", "<a>", DEFAULT_TEXTURE, "king_yolkars_tower", "xalx");
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        if (CrystalHollowsProgress.mark(player.getUuid(), MEAT_GIVEN)) {
            setDialogue(player, "recipe").thenRun(() -> player.addAndUpdateItem(ItemType.MYSTERIOUS_MEAT));
            return;
        }
        setDialogue(player, "again");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("recipe").lines(
                        "Hello traveler! Would you like to try my new recipe?",
                        "Here, have some Mysterious Meat. Don't ask what it is."
                ).build(),
                DialogueSet.builder().key("again").lines(
                        "I'm still working on a Zog stew. It needs more flavor!"
                ).build()
        };
    }
}
