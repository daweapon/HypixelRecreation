package dev.daweapon.type.thefarmingislands.npcs;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.farming.lab.LabQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.thefarmingislands.lab.LabScenes;
import net.minestom.server.coordinate.Pos;

public class NPCBethLab extends HypixelNPC {
    public NPCBethLab() {
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
                return switch (LabQuest.stage((SkyBlockPlayer) player)) {
                    case LAB1 -> LabScenes.FIRST_ROOM;
                    case LAB2_LEVER -> LabScenes.SECOND_ROOM_WAIT;
                    case COMPLETE -> LabScenes.HIDDEN_SECTION_BETH;
                    default -> LabScenes.SECOND_VISIT_START;
                };
            }

            @Override
            public boolean dynamicPosition() {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return switch (LabQuest.stage((SkyBlockPlayer) player)) {
                    case LAB1, LAB2_PENDING, LAB2_STARTED, LAB2_LEVER, COMPLETE -> true;
                    default -> false;
                };
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
        switch (LabQuest.stage(player)) {
            case LAB1 -> LabScenes.firstVisit(player);
            case LAB2_PENDING -> LabScenes.secondVisitStart(player);
            case LAB2_STARTED -> LabScenes.leverHint(player);
            case LAB2_LEVER -> sendNPCMessage(player, "Let's see what's behind it. You go first.");
            case COMPLETE -> setDialogue(player, "complete");
            default -> {
            }
        }
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("complete").lines(
                        "Thanks again for helping me discovering the truth about Jake.",
                        "Can't believe he had a second secret lab to hide!").build()
        };
    }
}
