package dev.daweapon.type.galatea.npcs;

import dev.daweapon.type.galatea.support.TalkNPC;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCTilus extends TalkNPC {
    private static final String TALK = "talk";

    public NPCTilus() {
        super(new Spec("Tilus", "b",
                "ewogICJ0aW1lc3RhbXAiIDogMTcxODA0Nzg0MjMwOCwKICAicHJvZmlsZUlkIiA6ICI5YzYwYzU3MzRmMDY0YjVkOWE1OTdlNjBjMjI3NjllMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJFcmlja05vb2IwNiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84ZGUxMjBkZWJjYzZjNTg3M2JmNzMyMzJjYjZiZjk2NzcyNDBhYTM3MTg0MTJiMTI5MjkzMTMyMTFlN2U0MmY5IgogICAgfQogIH0KfQ==",
                "Fxm9ULjNgmLxdq6La9nYMATY3mIMrQ+Pj3+BhK1kcz4O8qi7O5JFqyjBQI3DHDQXz34I4zZUxXvc4u3i4MXNjxMCnuo4vbFqevwItsgCvZ7NWFFwQG8Wva1SJqNMCSYexwrK3nOtbL4QhArZ2OuwmSBF8uRc+gOQDVf0pIBnOm2evjnyOW2J5IlSOrQO1yB/pIJnEosuZzeW6rXKThDLCX0ivTx4PMXyqyN4H1CLtGxMLuR4REPWKvioZhM0puTvbN/fuTX64DgYUl0iwUH8ab6qHjCHPfp1KnVcqL3L14dqIhkR4hKXwcArEvH+fpQUelsBRRylis9ifzQ+P5BOrpgydib3Km2GpVyZejn5h++XNyBAG1jB8Eok9pjT8wpzi8dbGGKwFH3yy1MLRfM3KbZCUg+OxT6bJPaIEhJ1JKf/oQDkK2Ax0MxCInwd8u1TC0og6aCjRI7JOgYQ6z+sCyJZN/cMHuuw1nuryrdYe7/Aj/5m03JEllwH/ROQS/W2ckv06hObavNwpP6ATzXnUFSEKYDGjIgQPsKvxR9kXMFXrYcU8ScG/exGWdeNpV6RzlNw8WCwVIUzfm2+pzC4InMjtsTZ66tNI3drUAr5ImhZf6DmNSDgEFQ9VGe4aG+SpBeqY5MmlZXAORMX+cIG+S1qK2wcT0nXjfwzapvJYpk=",
                new Pos(-642, 55, 48, -146.25f, 0)));
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
                        "<o>blub <f>punch <o>bulb <f>lantern <o>bulb",
                        "<o>blub blub",
                        "Bubble <o>blub",
                        "<o>blub"
                ).build()
        };
    }
}
