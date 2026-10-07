package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.ClaimRewardView;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.marco.MissionGiveRoseRedDyeToMarco;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCMarco extends HypixelNPC {

    public NPCMarco() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Marco", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "q+SigjPrEtKesr/szWqXWuA0Scs0NgEhDCtp1VRXB0a5uxOkdo4YgFRsP9wLPyaeQPXWI5FQOxbtOy6lxHLpTArbQgIzzdoyZ72mhyuXH+w924+x8+zUWhKiPkA+zTCxn8T/SYulQ0ptigs+vxTFkWyNp+I2QkzgB6fnmh2/IHRKP42PyIiLfPsLh9cTBm7ekXSrCUcSXlbX0BRNPmkpWv8KLx3/iK0diZB+oz0eBO13JHLKITO/unjLMRKt9LC3q1L3WSPFFjOJzN5cJmD6EwO5ANWhERHruwF+UlrZDm1tcOLXGoqyYo1p+gmy2Ims13H1kPJG3807jtnrmXOXrgkQTfddBpCm2gi593hnpsvBoNfAfmjE+CkdhnE7xlCwD5I6SWUrYGZ6kFgGoxTuv31zAVUKEGCMryKgch7AEX8OYCDfvmncpnqkHCkrxlVNE9fQ4/iI20R77Muw+iOTcQTQOU4KMI48w6O5tlfpZjm2vtJlaW2vaN2zmba1R2v/ncMi1p2LidZcwfEe5EV15WAOkHSo3nEAvMprV6uiLbRTXOK1RZmNQPFVIJ7Gb+5q9X0Nv9S4OH/U/SiN83VKy9KCUhZ7IemL5epT/pP1pjbhTMuxVmQlfHp7qwUxLMbRwP/P8UWjPq3nhnB3cnQ763Dr+2+OOpYoTbmAqtLmNqM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1MDAwMDQ3OTM5MjMsInByb2ZpbGVJZCI6IjkzYzdmMmUxMTg2MzQ5NzU4OGE2ZWI0YzUwYjRhZGZiIiwicHJvZmlsZU5hbWUiOiJUYWN0ZnVsIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80OTQ1ZDA2NDM4ZjdmZmYyOTFjY2QxNWIxNTZlM2ZiODcyYmMyNWNkOGUyMmE1Zjc2NWM3MDNlYjMzOWY4In19fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(90.5, 76, 3.5, 0, 0);
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

        MissionData missions = player.getMissionData();
        if (missions.hasCompleted(MissionGiveRoseRedDyeToMarco.class)) {
            setDialogue(player, "idle-" + (1 + (int) (Math.random() * 3)));
            return;
        }
        if (!missions.isCurrentlyActive(MissionGiveRoseRedDyeToMarco.class)) {
            setDialogue(player, "quest-start").thenRun(() -> missions.startMission(MissionGiveRoseRedDyeToMarco.class));
            return;
        }
        if (player.getAmountInInventory(ItemType.RED_DYE) < MissionGiveRoseRedDyeToMarco.REQUIRED_DYE) {
            setDialogue(player, "missing-items");
            return;
        }
        player.takeItem(ItemType.RED_DYE, MissionGiveRoseRedDyeToMarco.REQUIRED_DYE);
        setDialogue(player, "quest-complete").thenRun(() -> player.openView(new ClaimRewardView(),
            new ClaimRewardView.State(ItemType.SPRAY_CAN, () -> missions.endMission(MissionGiveRoseRedDyeToMarco.class))));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
            DialogueSet.ofTranslation("quest-start", "npcs_hub.marco.dialogue.quest_start"),
            DialogueSet.ofTranslation("missing-items", "npcs_hub.marco.dialogue.missing_items"),
            DialogueSet.ofTranslation("quest-complete", "npcs_hub.marco.dialogue.quest_complete"),
            DialogueSet.ofTranslation("idle-1", "npcs_hub.marco.dialogue.idle_1"),
            DialogueSet.ofTranslation("idle-2", "npcs_hub.marco.dialogue.idle_2"),
            DialogueSet.ofTranslation("idle-3", "npcs_hub.marco.dialogue.idle_3")
        };
    }
}
