package dev.daweapon.type.galatea.npcs;

import dev.daweapon.type.galatea.support.TalkNPC;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCTalia extends TalkNPC {
    private static final String TALK = "talk";

    public NPCTalia() {
        super(new Spec("Talia", "b",
                "ewogICJ0aW1lc3RhbXAiIDogMTczNzc1MDUyMzE4MywKICAicHJvZmlsZUlkIiA6ICI1MzE4YWJhNDJiMTk0ODNiODFiMWY2N2Y1ODVjNDdkNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJocHllZiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hYmRiNmY1N2RmNmU4NDUxOGY2N2I0MjM1NDEyNzc0YzRmNjFjNzJiOTFhMmI3NDBiMDZhNTBhMGY3MzQ3ZTIxIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                "xj0mrGURiI0P1PMoorkj3lWoOWlQ/d2ZWkAcf4rBV6WaSJAHbc27MXieaI671ZroQXHk1uq0UQAkJZWFTkBdmiv43aGlL5WO+DgUvJ5b7Sb2AXfLR6DH3JE2yjnDXRqUudXA6OobgCM+fsYn0FutdMwCcfhwjU+s0oGuBi6QCPv9OQJ3Y01BKs9gPmQD502nRGiC5/m1m/QTbAktcF++dgTu9jMuH7fmfDU600NrpHzPTpS8xwFgRJDjMSR/REVkdU1UM97dJ6yj/NiDLxfqAUJrqKr98Apo4nR+WTjneMBe71YYz4osW7WVOdXaXSgYMv2AB5yj8XhsLbZYQuj+FlXi02iR7G95yyFA8rLX+DvbcAAewc828Wn4HT9599+2ob8meRKLEm6g6jLzxQsgDhoXpTUlQKOuO/suezpvJrrXjInPBRKzn0Fc61+qL5C9BFjpxjx5lolmviW3jiaF8kbpSBjJYR5iTwfxmVW6Fk54APwTG+LYKoD3SzMI5TglWvhUnZIMtG+Oj00Nx4VIysG6QBJ9nrg+6iwTvSBuoqXhNREgul4IRfNtK5GSyknEfyTmlPZ41jCDSPVr2U1bm+OshV874h4adjb+y0V//y0C/IXXM88f2RVJfwP7lo10rKYCwG0x+9jYdJ7UXakLhcmCVFklYvN0RXJnxPisg98=",
                new Pos(-615.5, 108, 2.5, -56.25f, 0)));
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
                        "The view from here is just so bad.",
                        "I mean look at it! Look!",
                        "It’s hard to even describe how awful this view is.",
                        "I come here everyday just to see how terrible this view is.",
                        "Every time I look, it somehow gets worse!"
                ).build()
        };
    }
}
