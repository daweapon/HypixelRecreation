package dev.daweapon.type.dwarvenmines.npcs;

import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.fishing.grubber.MithrilGrubbers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

/** Guard Gornum, in the Abandoned Quarry: talking to him lets you fish up Mithril Grubbers (wiki: Guard Gornum). */
public class NPCGuardGornum extends AbstractTalkingNPC {
    private static final String[] IDLE = {
            "If you don't know already, this is the only place where you can fish up <2>Mithril Grubbers<f>.",
            "When you kill 'em, they'll drop lotsa <2>Mithril Powder<f>.",
            "Just make sure you're <3>Fishing Level 6<f> or you won't be able to catch 'em."};

    public NPCGuardGornum() {
        super("Guard Gornum", "<e>",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjk3YmJkMjk5NTY4MmYxMDZhZWEzOGM4MTdiYmE2MDZiMDlhNjIyODViYTQ4NWIzNWI0M2Q4NWQ3OGY3YjdmNiIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiIzNjg0NWE1NTk5YmIyZWFlOWI4MTk1NzE2ZTBiY2YwMSIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzUyMTU3NzYxMTkwfQ==",
                new Pos(-158.5, 149, -14.5, 0, 0),
                IDLE);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        HypixelPlayer player = event.player();
        if (isInDialogue(player)) return;
        SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
        if (MithrilGrubbers.metGornum(skyBlockPlayer)) {
            setDialogue(player, "talk");
            return;
        }
        setDialogue(player, "first").thenRun(() -> MithrilGrubbers.meetGornum(skyBlockPlayer));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("first").lines(
                        "Halt!",
                        "How'd you get in here? The entrance is sealed off...",
                        "Eh, whatever.",
                        "If you got in here, you probably know what you're doing.",
                        IDLE[0], IDLE[1], IDLE[2]).build(),
                DialogueSet.builder().key("talk").lines(IDLE).build()};
    }
}
