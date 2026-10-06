package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.jerrysworkshop.dialogue.IdleLineNPC;

import java.util.List;

// One of the Glacial Cave penguins, who share Maaike's skin and idle lines.
public class NPCDaan extends IdleLineNPC {
    public NPCDaan() {
        super("Daan", new Pos(80.5, 78.5, 110.5), NPCMaaike.TEXTURE, NPCMaaike.SIGNATURE, List.of(
                "I wonder where Hendrik is now...",
                "It's a warzone in here, everyone fighting over ice!",
                "I think we got demoted for some reason...",
                "There always seems to be gifts and other items in these walls!",
                "The volcano really seems to bring out more treasure in these walls.",
                "Do you think they'd let me bring snowballs in here?",
                "I heard theres bioluminescent creatures living in this ice! It's Amazing!"));
    }
}
