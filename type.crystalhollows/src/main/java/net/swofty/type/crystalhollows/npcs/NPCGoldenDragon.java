package net.swofty.type.crystalhollows.npcs;

import net.swofty.type.crystalhollows.gui.GUIShopGoldenDragon;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCGoldenDragon extends AbstractStructureNPC {
    private static final String MET = "met_golden_dragon";

    public NPCGoldenDragon() {
        super("Golden Dragon", "<6>", DEFAULT_TEXTURE, "dragons_lair", "golden_dragon");
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        if (!hasMet(player, MET)) {
            setDialogue(player, "hello").thenRun(() -> markMet(player, MET));
            return;
        }
        player.openView(new GUIShopGoldenDragon());
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{DialogueSet.builder().key("hello").lines(
                "So, a visitor finds my lair. I have hoarded the finest treasures of the Hollows for centuries.",
                "An egg of my line can be yours, for a hoard worthy of a dragon: coins, gold, and a Perfect Gemstone of every crystal."
        ).build()};
    }
}
