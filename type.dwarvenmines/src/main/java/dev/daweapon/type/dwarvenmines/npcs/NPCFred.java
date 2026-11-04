package dev.daweapon.type.dwarvenmines.npcs;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCFred extends HypixelNPC {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmRkZDg5YTZhZTk1N2Y3NjZkMzAwMDExZmY0NDcxNDgxYzNiYjYxYjY3NjA3OGE4YzZjM2M0MDgzMjAxYjVjMiJ9fX0=";

    public NPCFred() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b><l>Forge Foreman", "Fred", "<e><l>CLICK"};
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
                return new Pos(-2.5, 148, -68.5, 0, 0);
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
                "These forges have been running at an endermite's pace!",
                "My job here is to make sure the lava flows and that the titanium shines.",
                "Yep, it's mostly watching other people work!").build()};
    }
}
