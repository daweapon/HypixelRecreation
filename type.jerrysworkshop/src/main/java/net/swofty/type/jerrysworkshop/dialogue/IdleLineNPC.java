package net.swofty.type.jerrysworkshop.dialogue;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** A Jerry's Workshop NPC with no function beyond saying one of its idle lines when clicked. */
public abstract class IdleLineNPC extends HypixelNPC {
    private final List<String> lines;

    protected IdleLineNPC(String name, Pos position, String texture, String signature, List<String> lines) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{name};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return signature;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return texture;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return position;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
        this.lines = lines;
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (isInDialogue(event.player())) return;
        setDialogue(event.player(), "idle");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("idle")
                        .lines(lines.get(ThreadLocalRandom.current().nextInt(lines.size())))
                        .build()
        };
    }
}
