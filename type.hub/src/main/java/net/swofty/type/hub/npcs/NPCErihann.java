package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointBoolean;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIErihann;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCErihann extends HypixelNPC {

    public NPCErihann() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<9>Erihann", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "wTTrOSMpXVRKUThe2HctBxvPPck6X2BFDuex+BnYAleMcJ5k72b2zr12RB4mFFy5AenOMaWIpkcFma8X/varRaNl9FXos+uAgWKFLZhiYzl/0Cid8DAZY1sC4zE/n+N81Nl50diOy+8l8HVjm+cqZDLSPHg+S9Hwi7sOWcp5xVpCIHpMBqd4kXNX/vQjuiLledIGpsGUnlEdF9qOnvg3T2jz+f75ByQi542F0SJk/qE026B/vVxJSs7OiT6gJZzS92RWtuLcrbjn+cOPIl3blkkX6IjX3vMWRbXDVy1jrOqihTWvl32vQbPRc0DaLoFDxHmLDPbpLZDfA6/bvaJunmsszeDL6fDFOHDiVNW9jfxRDzFt08PzWdb3/pvA7HvnsYJEJlpybIfQfFSblVrxKJC3XnBN6d5J9W2cZqt++3juFxHfyqyGcyBf7OF6IV/OdNRb72kqA5zbeU7X8KrdBhqhBHiXa2znhn5suV2VNOYRMisABXYJmXXeJM4fsQqSIHNbKT/18oZuHEIaF2R+vC5h3nlmnQo0J5quSXxj1BGOJ8kxWZv4fuWuEoTDg1+2nwe72/LDPuOFKETLYzZmcIgjAu9Vygs9DCgGlhaMhrPs76Ab4T7iSThgrgvHL1adsqGd3Z79mIiAKGQbnlUMKO6lwYchHgHJ6N3FjqlzZ+Y=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY3NTE0MDc4NzIzNiwKICAicHJvZmlsZUlkIiA6ICI5NDFjNDM1MDUxMzI0YWVkOGUyOWYzZjgwYTcwNzk4ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJCcmFuZFdlWF85MiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85MWYwN2M2MzM2ZTQzNjU5YzUxNzM3NDYzOWJmNmM5NmQ4NGViODI0MDgwMmVjZGZiYTRhNWY1NDQ5OTg3NDcxIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(42.5, 97, 93.5, -45, 0);
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

        if (player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.RIFT_PRISM_IMBUED, DatapointBoolean.class).getValue()) {
            setDialogue(player, "imbued");
            return;
        }
        if (player.countItem(ItemType.RIFT_PRISM) > 0) {
            setDialogue(player, "has-prism").thenRun(() -> player.openView(new GUIErihann()));
            return;
        }
        setDialogue(player, "idle");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.ofTranslation("idle", "npcs_hub.erihann.dialogue.idle"),
                DialogueSet.ofTranslation("has-prism", "npcs_hub.erihann.dialogue.has_prism"),
                DialogueSet.ofTranslation("imbued", "npcs_hub.erihann.dialogue.imbued")
        };
    }
}
