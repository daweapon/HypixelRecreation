package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

// Frozen Alex, in his igloo by Jerry Pond. On Hypixel he runs the Ice Essence shop; Ice Essence has
// no source on this server yet, so for now he only talks.
public class NPCFrozenAlex extends HypixelNPC {

    public NPCFrozenAlex() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Frozen Alex", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTlhOWViMWZiYTU2Y2Q0NjIxYTVmN2Y2Mjk3OTNjYzFkMzYxODA4N2VlYTM2ZTE4YTUwYjVjOWFkYjNkNmE4NyIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiJlODM4ZmU5YTFiYTIyNGJhYjlmZTMzODE3ZWMwZDI2MyIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzc1NDM3NDc3OTk0fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-43, 84, -1);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (isInDialogue(event.player())) return;
        setDialogue(event.player(), "hello");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(
                        "Brrrrr, I've been stuck down here ever since I fell into the pond with Steve.",
                        "Here, let me share some of my ice with you."
                ).build()
        };
    }
}
