package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.type.galatea.support.TalkNPC;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCHunterMcNeil extends TalkNPC {
    private static final String TALK = "talk";

    public NPCHunterMcNeil() {
        super(new Spec("Hunter McNeil", "b",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjBiMWJjYWEzMzFhMmY4ZjE4NWVjODlkMmQ3NWYxNDQ5ZGQ5M2M5NTU1MmQxODdlODRhMjdiYmQyNzhlYTU2YiIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiJhM2M0OTRkM2M1MDEyMDAxYjc4MDA0YmIyMmFiYWI0MCIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzg0NzkwMzQ2NTgwfQ==",
                null,
                new Pos(-652.5, 103, 167.5, 0f, 0)));
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
                        "That there <d>Fishing Hotspot <f>is the only place to catch <9>Giant Water Bug <f>shards.",
                        "Who knows why they like it so much down here?",
                        "Getting all the way down here dang near killed me..."
                ).build()
        };
    }
}
