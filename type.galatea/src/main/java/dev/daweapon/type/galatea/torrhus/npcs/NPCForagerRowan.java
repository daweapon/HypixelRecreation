package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.type.galatea.support.TalkNPC;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCForagerRowan extends TalkNPC {
    private static final String TALK = "talk";

    public NPCForagerRowan() {
        super(new Spec("Forager Rowan", "b",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmJlOTUzYjE5ZDYwYTRmZWRmOTMzYTU0ZmFlNDJmOGUzZjU2YzY3MTA2MWRiYTg1MzcxNThiMTcxNDJiZGY3ZSIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiIzYTgwMTcyMzhlYWIyYzczYTFkOTJhMzIyOTY2NTNhYiIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzg0Nzg4MDE2NzU2fQ==",
                null,
                new Pos(-725.5, 137, 228.5, 0f, 0)));
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
                        "The axe forgets, but the tree remembers."
                ).build()
        };
    }
}
