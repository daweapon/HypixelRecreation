package dev.daweapon.type.galatea.npcs;

import dev.daweapon.type.galatea.support.TalkNPC;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCMartin extends TalkNPC {
    private static final String TALK = "talk";

    public NPCMartin() {
        super(new Spec("Martin", "b",
                "ewogICJ0aW1lc3RhbXAiIDogMTcyNTk5OTY1ODc4NSwKICAicHJvZmlsZUlkIiA6ICJiZmMyODE3ZWRhZWM0OWY5YTVmMWVkNTdjODZhMGFjOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJFYm95UmV6YSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMjI4ZDExYzk0MzIyYWEyNmFhZmM4ZjQ2YjU0Y2Y3ZWVhNzM4N2U3N2ViZDE5ZjRmMjBkZmU4NzdiZmZhNjU1IgogICAgfQogIH0KfQ==",
                "OAI9Bc/xYxdQP4aPov+sUuHjb37QmNdiRawMFjrooB2LYa7tUHMM+Tp9OxdtkyZVZMukBnecI0aCTWpmDuDEelUSszKh3eLI9C5wF9bLihspzsdN3eKfJfrOBbHNtU96EmrAW7NU3BlEtBX+e1L3fGQb7d0u8rxtHkWtFNhGhXPk0yHjiTvrgdl0MBhaFYaL0U6/nmwaG95y+h9dow78VSVRkPd0ii4LQXFz2Gm4y7VAqXW2UljyR5Ea/hNvE9p750DhvDzJp1c7ncVAPN6an+qY582vupyPCWFhPZ/zJ/wjmMhtSUkTkabsJiqG4Rpp02NBD9qT4CCie19w+33OC8f9Qgf2n/iaCGDNZGLjr7cwaifvSnNl1S5MrgNt4AIF7oz6OMG0l07flyK0gSqQadGpWudC7HNyw5OgcMgeEC4FdC9xrCSXGuWcsFQWb1mteC73VQLy3dX1MJ7sWP4TBbFxU3dclru4uY7wpVUPVDAnqV0Zm+yY7JuelIEMxjSTydDpZfEHBDjpH3IChecVDpxvbXzpVwCCSbrb0G0nvK63zU3/p7vrH1xgxfV5QYBih+62+SnYxxmxkzsVa8KqrVRY4paUv91EAVcq3scpwf+m8hJCDOI1PSFT28hmmGHy6fqQ0raC5uRiAjdS8Iqz5FrVPLEBHsdoriDQwt99Mlg=",
                new Pos(-579.5, 93, 31.5, -95f, 0)));
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
                        "Isn't this place fascinating?",
                        "Absolutely no sea creatures here, can you believe it?",
                        "Still, you can catch some rare materials to craft top-quality fishing nets."
                ).build()
        };
    }
}
