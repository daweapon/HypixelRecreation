package dev.daweapon.type.dwarvenmines.npcs;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public class NPCQueenMismyla extends HypixelNPC {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmY3MDM4NWY0NzJjMDM4MjVjMGU4OTRkOTRhYzIwMWUxYjY0MmQxMDMyMjAyZDRiZjMzMzcyZmEzYWEyOWZiYSJ9fX0=";
    private static final List<String> KING_LINES = List.of(
            "Grandan is my 7th favourite King.",
            "Thormyr only sleeps and eats when he's not at this table. Nothing else.",
            "Norry, Brammor zbells too bad.",
            "I'm fairly sure Redros used to be a guard, and they added him to the table one day by mistake.",
            "Emmor is the King today... sigh.",
            "Erren keeps losing at Tic Tac Toe, but he loves the game. A brave man in a way.",
            "Emkam is funny at least. But that's about it.");

    public NPCQueenMismyla() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Queen Mismyla", "<e><l>CLICK"};
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
                return new Pos(126.5, 195, 195.5, 180, 0);
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
        setDialogue(event.player(), "king");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        String line = KING_LINES.get(Math.floorMod(SkyBlockCalendar.getDay(), KING_LINES.size()));
        return new DialogueSet[]{DialogueSet.builder().key("king").lines(line).build()};
    }
}
