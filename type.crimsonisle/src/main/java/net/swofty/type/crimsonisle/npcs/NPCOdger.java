package net.swofty.type.crimsonisle.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.text.Text;
import net.swofty.type.crimsonisle.gui.odger.GUITrophyFishing;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.fishing.trophy.OdgerRewards;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Odger, the trophy fishing Goblin in his hut on top of the Blazing Volcano (wiki: Odger). Gives a Starter Lava
// Rod the first time a profile talks to him, then opens the Trophy Fishing menu.
public class NPCOdger extends HypixelNPC {

    public NPCOdger() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Odger", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk3M2VjMmFkMzgxZmM4ODMxYThhNmEyMzhmMzhkODAwNmNhNTFkNTY1NDJkZDk5MTllNDk1NWI5YWM4OWFmNGQifX19";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<e>Odger");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-373.5, 207, -810.5);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (player.getTrophyFishData().isOdgerMet()) {
            player.openView(new GUITrophyFishing());
            return;
        }
        setDialogue(player, "first").thenRun(() -> {
            if (OdgerRewards.giveStarterRodOnFirstMeeting(player)) {
                player.sendMessage("<a>You received a <a>Starter Lava Rod<a>!");
            }
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("first").lines(
                        "Crimson Isle fishing. Different.",
                        "Fish trophy like fish. But not same.",
                        "Unique way to fish. All trophy fish.",
                        "Bring trophy fish. To me. Reward.",
                        "Take this. Help you."
                ).build()
        };
    }
}
