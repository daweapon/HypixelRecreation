package dev.daweapon.type.galatea.npcs;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.gui.inventories.starlyn.GUIStarlyn;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynDialogue;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.List;

public class NPCAgatha extends HypixelNPC implements NPCAbiphoneTrait {
    private static final String INTRO = "first-interaction";

    public NPCAgatha() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Agatha", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjUwYjEzNjE2OGYzNzA4YzBjY2E2ZjQyZDE0MDQ2NWMzYTFhNDQ3YTJlNDI1ZWM0NWU3ODhjNWM3OTIwZDM2NSIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiJiMTU0NTgzMDUwMDcyY2ZlYWJjMGMyYTJiOGQwMWJiMCIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzg1OTQxMDY5NzA1fQ==";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<b>Agatha");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-648.5, 89, -3.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
        StarlynContests.start();
    }

    @Override
    public String getAbiphoneKey() {
        return StarlynSister.AGATHA.key();
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        StarlynContests.Gate gate = StarlynContests.gate(player);
        if (gate != StarlynContests.Gate.READY) {
            setDialogue(player, gate.name());
            return;
        }
        if (!StarlynContests.data(player, StarlynSister.AGATHA).isMet()) {
            setDialogue(player, INTRO).thenRun(() -> {
                StarlynContests.markMet(player, StarlynSister.AGATHA);
                player.openView(new GUIStarlyn(StarlynSister.AGATHA));
            });
            return;
        }
        player.openView(new GUIStarlyn(StarlynSister.AGATHA));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        List<DialogueSet> sets = new ArrayList<>();
        for (StarlynContests.Gate gate : StarlynContests.Gate.values()) {
            if (gate == StarlynContests.Gate.READY) continue;
            sets.add(DialogueSet.builder().key(gate.name()).lines(StarlynDialogue.gateLines(gate, player.getUsername())).build());
        }
        sets.add(DialogueSet.builder().key(INTRO).lines(StarlynDialogue.agathaIntro()).build());
        return sets.toArray(DialogueSet[]::new);
    }
}
