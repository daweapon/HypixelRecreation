package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.type.galatea.support.TalkNPC;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCTreehuggerTimothy extends TalkNPC {
    private static final String TALK = "talk";

    public NPCTreehuggerTimothy() {
        super(new Spec("Treehugger Timothy", "2",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNjN2E4YzUyODJhMjg1MWExZDljMzcxZDU1NzU3ZmViMDQxNWU0NjJkZWUwNzVjMmY0NTE2ZTA0YzEzZjg0IiwibWV0YWRhdGEiOnt9fX0sInByb2ZpbGVJZCI6ImRjMzEzMDhmMzQwNDI2Y2E5NDdhMmM0YTM2MjY4MzExIiwicHJvZmlsZU5hbWUiOiJuZWE4OSIsImlzUHVibGljIjp0cnVlLCJ0aW1lc3RhbXAiOjE3ODQ3ODc1NTgwMzJ9",
                null,
                new Pos(-541.5, 110, 276.5, 0f, 0)));
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
                        "The <2>Tree <f>behind me is pretty <6>mega<f>, huh?",
                        "I bet you wanna cut it so bad, right? Get all that loot and everything.",
                        "Well, too bad! I’ve just had a <c>Tree Protection Order <f>placed upon it!",
                        "So none of you people, with all of your fancy axes, can try to fell it!",
                        "Why don’t you go and try, huh? See how that works for you.",
                        "Enjoy the fine!"
                ).build()
        };
    }
}
