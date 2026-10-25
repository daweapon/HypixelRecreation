package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.type.galatea.support.TalkNPC;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCForagerDaniel extends TalkNPC {
    private static final String TALK = "talk";

    public NPCForagerDaniel() {
        super(new Spec("Forager Daniel", "b",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZkNTRlMjFmYjFkNDE4ZGVmNWE2N2IzMTE0YzJkZGNhZGJmYjhkZGY2ZGI5ZGIyYzNhMGIyYmYxZDViYTAzNyIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiJhNzNlNGFjYjE2NDQyNDgwYTZmZmE4ZjI1YjdhNWEzZiIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzg0Nzg3NjIyNDU0fQ==",
                null,
                new Pos(-636.5, 95, 245.5, 0f, 0)));
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
                        "When cutting <6>Helix Trees<f>, you should always start with the <6>Beige <f>logs.",
                        "Once those have been dealt with, you can move on to the <c>Red <f>ones.",
                        "Make sure you do it in the right order, or you'll get a <c>Sweep Penalty <f>and the <a>Tree <f>will take longer to cut…"
                ).build()
        };
    }
}
