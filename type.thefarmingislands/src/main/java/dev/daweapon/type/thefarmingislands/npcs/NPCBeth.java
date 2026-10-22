package dev.daweapon.type.thefarmingislands.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.farming.lab.LabQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;

import dev.daweapon.type.generic.event.custom.NPCInteractEvent;

public class NPCBeth extends HypixelNPC {
    public NPCBeth() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Beth", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "V7GBdnK+EHOLEjV0g07BF/451Vl3xJOt8hbb3yLFAnt4ljf1ALdyTO9cprUTRV+Gr+dmp4i+2FikXHhA92vNZJ/GWCBvuW0VWFMu/aX3aFoaTbHSMR7Yl2OC/+eZsSUt3DCCc6IDysgNWW9+1G+OKpACiYVBwdw5mHSIGRptS54Jq9Xomj+EF+knCTwetYNKeLI5RRrVlfB5BKTM0frybqroNXHFftubEf8CNbJWW6hgQkr7PvLWQrelc5YG0928Mi7ZtbRNe4QgFBHI70WTtL0tAdIUTw3h0f/GNCGLsoyuahBsaKufGsnqRQcLsHUJjRP/gupegSru6yq+yLcBzi1A3czsWQ1tdqGpQyhRDUK22o3ue3c1cNU0MjrFz0U9HT3HMsAgpvH9F4MSU0g+uaLffj9TR5oPeV5exSIPfkpr/yQFFvglw8YHUpVmvXtMj3+v7ckGDZZJiDtG8aVS0jdB38zXa8I9tuG3Fkf2L5uiYygLncUOcd3AgYZJIk1fhHX8imtNmvBmaG4i6LDuNyac1POBA/694bGkhzKSpCfFQ+ZYm3tr4zajswZxtQbVTvROKyaSali4Mv0jhFoxyfcuIkWF5/gfagUrxaOcJ1sT6Dz2xqCp32WZ9VaPmob/EBO3WEtLFHOTPcieMGdEpWopTFLQCPYQGT2nYXYiSOM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNjkyMTAxODM4NSwKICAicHJvZmlsZUlkIiA6ICIxNzhmMTJkYWMzNTQ0ZjRhYjExNzkyZDc1MDkzY2JmYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJzaWxlbnRkZXRydWN0aW9uIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NhYmIyYzA0NjM1YTI5YjA4ZTE2MWQ4NTliMTFiZGFkYjc1YTI5ZDNjNjRkZjdlODRhMTA4YzM3ZjdmODNjMCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(163.5, 77, -359, -140, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        setDialogue(player, dialogueFor(player));
    }

    private static String dialogueFor(SkyBlockPlayer player) {
        LabQuest.Stage stage = LabQuest.stage(player);
        int visits = LabQuest.acceptedVisits(player);
        return switch (stage) {
            case VISITING -> visits == 0 ? "none" : "during";
            case NOTE -> "coordinates";
            case OPENED, LAB1 -> "during";
            case LAB1_DONE, LAB2_PENDING, LAB2_STARTED, LAB2_LEVER -> "after-lab";
            case COMPLETE -> "complete";
        };
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("none").lines(
                        "That Jake fellow is a bit suspicious.",
                        "He says he can't leave his house but I've seen him walking around all the time...").build(),
                DialogueSet.builder().key("during").lines(
                        "I keep thinking about Jake's behavior...",
                        "I know I'll catch him doing something!").build(),
                DialogueSet.builder().key("coordinates").lines(
                        "Did you figure out the meaning of the note yet?",
                        "Maybe you should go to the cave where I followed him. I remember there were a lot of mushrooms.").build(),
                DialogueSet.builder().key("after-lab").lines(
                        "Seems that I have misjudged Jake, he might not have been as suspicious as I thought.",
                        "The plushie he gave me is quite cute actually!").build(),
                DialogueSet.builder().key("complete").lines(
                        "Thanks again for helping me discovering the truth about Jake.",
                        "Can't believe he had a second secret lab to hide!").build()
        };
    }
}
