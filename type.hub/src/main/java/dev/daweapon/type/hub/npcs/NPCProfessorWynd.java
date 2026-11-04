package dev.daweapon.type.hub.npcs;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCProfessorWynd extends HypixelNPC {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTMwMzU3ZTFmMjk1NzUxOTI1ODMxNGI0NThkYzljMzllOGQ3YjdkOGJhNzdlOWU2NDZhN2Q1Yjk5NDVjYjdmZCJ9fX0=";

    public NPCProfessorWynd() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b><l>METEOROLOGIST", "Professor Wynd", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(2.5, 180, 60.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (isInDialogue(event.player())) return;
        setDialogue(event.player(), "hello");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{DialogueSet.builder().key("hello").lines(
                "Have you noticed any peculiar <b>weather <f>recently?",
                "That's the <d>Origin Cloud's <f>doing - it seems to amplify even the faintest of weather patterns, turning them into something <c>extreme<f>.",
                "<6>Sunny <f>and I have soared far and wide in my <5>flying contraption <f>in pursuit of documenting the <d>Origin Cloud<f>!",
                "However, all it took was one rogue <e>lightning <f>bolt to send us crashing into this mountain.",
                "We're both fine, but it's going to take a while to fix up our ship, given that it's... well, <d>embedded in the mountain<f>.",
                "While we're busy dealing with that, feel free to stop by anytime.").build()};
    }
}
