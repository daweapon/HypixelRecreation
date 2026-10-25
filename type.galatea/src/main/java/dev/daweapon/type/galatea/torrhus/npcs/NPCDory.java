package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.type.galatea.support.TalkNPC;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCDory extends TalkNPC {
    private static final String TALK = "talk";

    public NPCDory() {
        super(new Spec("Dory", "b",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZhY2QwNmU4NDgzYjE3NmU4ZWEzOWZjMTJmZTEwNWViM2EyYTQ5NzBmNTEwMDA1N2U5ZDg0ZDRiNjBiZGZhNyIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiI0Nzc2ZGE3ODQxNmYyYTRmYmRiZWIxYTA5ZWZjNzlmZiIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzg0Nzg3NzAwNjY2fQ==",
                null,
                new Pos(-645.5, 161, 194.5, 0f, 0)));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (isInDialogue(event.player())) return;
        setDialogue(event.player(), TALK);
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key(TALK).lines(
                        "<b>Torrhus Springs <f>being so high up means they run down super deep.",
                        "The further down you go, the more <9>Pressure <f>builds up.",
                        "If you're thinking of taking a dive, make sure you've got enough <9> Pressure Resistance <f>and <3> Respiration <f>to reach your destination.",
                        "Last thing I want to be doing on my day off work is hauling your butt out of there."
                ).build()
        };
    }
}
