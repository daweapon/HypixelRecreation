package net.swofty.type.crystalhollows.npcs;

import net.swofty.type.crystalhollows.gui.GUIShopGoldenDragon;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCGoldenDragon extends AbstractStructureNPC {
    private static final String MET = "met_golden_dragon";

    public NPCGoldenDragon() {
        super("Golden Dragon", "<e>", DEFAULT_TEXTURE, "dragons_lair", "golden_dragon");
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
                "Do not think I am sleeping, thief.",
                "Those eggs are mine, but I would be willing to sell them against pure gold and gemstones.",
                "But are you rich enough?"
        ).build()};
    }
}
