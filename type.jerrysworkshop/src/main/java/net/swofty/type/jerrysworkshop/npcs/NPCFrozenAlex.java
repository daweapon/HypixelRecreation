package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.essence.GUIFrozenAlex;

// Frozen Alex, in his igloo by Jerry Pond: Salvage Items and the Ice Essence Shop.
public class NPCFrozenAlex extends HypixelNPC implements NPCAbiphoneTrait {

    public NPCFrozenAlex() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Frozen Alex", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTlhOWViMWZiYTU2Y2Q0NjIxYTVmN2Y2Mjk3OTNjYzFkMzYxODA4N2VlYTM2ZTE4YTUwYjVjOWFkYjNkNmE4NyIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiJlODM4ZmU5YTFiYTIyNGJhYjlmZTMzODE3ZWMwZDI2MyIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzc1NDM3NDc3OTk0fQ==";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<b>Frozen Alex");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-43, 84, -1);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        HypixelPlayer player = event.player();
        if (isInDialogue(player)) return;
        if (player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FROZEN_ALEX)) {
            player.openView(new GUIFrozenAlex());
            return;
        }
        setDialogue(player, "hello").thenRun(() -> {
            player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FROZEN_ALEX, true);
            player.openView(new GUIFrozenAlex());
        });
    }

    @Override
    public String getAbiphoneKey() {
        return "frozen_alex";
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(
                        "Brrrrr, I've been stuck down here ever since I fell into the pond with Steve.",
                        "Here, let me share some of my ice with you."
                ).build()
        };
    }
}
