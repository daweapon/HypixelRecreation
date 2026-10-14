package net.swofty.type.crystalhollows.npcs;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCXalx extends AbstractStructureNPC {
    public NPCXalx() {
        super("Xalx", "<a>", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE3Mjg1MDkwNmI3ZjBkOTUyYzBlNTA4MDczY2M0MzlmZDMzNzRjY2Y1Yjg4OWMwNmY3ZThkOTBjYzBjYzI1NWMifX19", "king_yolkars_tower", "xalx");
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        setDialogue(player, "recipe").thenRun(() -> player.addAndUpdateItem(ItemType.MYSTERIOUS_MEAT));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("recipe").lines(
                        "Hello traveler! Would you like to try my new recipe?",
                        "This meat is a special blend of well... everything."
                ).build()
        };
    }
}
