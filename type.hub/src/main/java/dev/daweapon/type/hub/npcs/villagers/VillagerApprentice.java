package dev.daweapon.type.hub.npcs.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.VillagerConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.missions.MissionGiveWoolToCarpenter;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class VillagerApprentice extends HypixelNPC {
    public VillagerApprentice() {
        super(new VillagerConfiguration(){
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<f>Apprentice", "<e><l>CLICK"};
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-152.5, 63, -33.5, -90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<6>Apprentice";
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.BUTCHER;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;

        MissionData missions = player.getMissionData();
        if (missions.hasCompleted(MissionGiveWoolToCarpenter.class)) {
            setDialogue(player, Math.random() < 0.5 ? "furniture" : "learning");
        } else if (missions.isCurrentlyActive(MissionGiveWoolToCarpenter.class)) {
            setDialogue(player, "wool-hint");
        } else {
            setDialogue(player, "talk-to-carpenter");
        }
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
            DialogueSet.ofTranslation("furniture", "npcs_hub.apprentice.dialogue.furniture"),
            DialogueSet.ofTranslation("learning", "npcs_hub.apprentice.dialogue.learning"),
            DialogueSet.ofTranslation("talk-to-carpenter", "npcs_hub.apprentice.dialogue.talk_to_carpenter"),
            DialogueSet.ofTranslation("wool-hint", "npcs_hub.apprentice.dialogue.wool_hint")
        };
    }
}
