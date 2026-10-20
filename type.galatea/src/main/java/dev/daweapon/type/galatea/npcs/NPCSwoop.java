package dev.daweapon.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import java.util.List;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;

// Decorative Swoop NPC, found in Galatea.
public class NPCSwoop extends HypixelNPC {

    public NPCSwoop() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Swoop", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "EF0QFzeMDwxRJH7yv7+yQWZeoSjoePOBDpiBbjR4C+pbaaVpvIdpkVPLCixbRTLbyhE5tms4to/91yehxsbsvbAJz5dr9K58XdcUp9nuzYBgxEmZv8z2wpYOUGPFOp7lQxyY1vH6UcrxrNdUy3d0h/zaPN2YcRgg7DCrtE6S1coNn7v+PB2z+k2QFQnPcJe7c8U4PIQIEsZoIaH4OYaw0d4pWfjNoEfblEjKfywyNpSJwHXaJh2XYUUmlOb8sb8wOnc774sSSMnReQL6x5DEWwK/18W9j/QOwvWduw8K29C6rQET9x4HbzMszj3utaZgt3tXNqUa0I2Kk3ikTkzG71nfWxnstNf52g0CQ0cSIJF1aDdbhGRhabgFKhNwDGJJr4aiKE5B4dRS1XGHtnE2Fp9VWrpN3EdBi+EfOEzcJlcC1luu8uqYDl4phPYXgLwR3wKFPeEouhERGRwhO+yRnyBS1MhHemUG8nCYykZxD2/l1ixOF7wBwGlrABpvtOA+xEYt5M1JLeaAYc4qNGv9Zbn17IH7mm4On2XHBCbXBzn9s3mv5b8G9xA7dIje79lu/C67vWJ0r/89LPAN+ZXkoj5xp6dqn7n0KFrQJk73VVre4Ry2makjZsg8hooiz1EbsepMzZHzb+PyHCqd3X1HCgVwQNdq96knTRuV8JjDjBY=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxOTMyODg5Mjk0OCwKICAicHJvZmlsZUlkIiA6ICIxODA1Y2E2MmM0ZDI0M2NiOWQxYmY4YmM5N2E1YjgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJSdWxsZWQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzc1YzU3ZGNjODVhNGM2MDM3ZjUyYWYwMGZkY2VmNDA0Zjc0NjI3NmMwZDMxYTIyYjJhNTUwYzZhZWQ5MzJlZSIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<b>Swoop");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-579.5, 114, -23.5, 24, 0);
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
        if (!HotfService.isUnlocked(player)) {
            player.sendMessage("<e>[NPC] <b>Swoop<f>: <7>{}<f>! Have you talked to <b>Hina <f>at the front of the island to get your hands on a <a>Heart of the Forest<f>?", player.getUsername());
            return;
        }
        if (!ChapterTriggers.done(player, "npc.swoop")) {
            setDialogue(player, "first-interaction").thenRun(() -> ChapterTriggers.fire(player, "npc.swoop"));
            return;
        }
        player.notImplemented();
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first-interaction")
                        .line("Hello <7>{}<f>!", player.getUsername())
                        .line("<f>Welcome to <2>Galatea<f>!")
                        .line("<f>I hear you've been busy in <a>The Park<f>. Charlie's a good friend.")
                        .line("<f>Friends of my friends are my enemies - whoops, my friends I mean!")
                        .line("<f>You might be wondering why I'm on top of this bush?")
                        .line("<f>I'm camping it to get the <d>berries <f>before someone else.")
                        .line("<f>...")
                        .line("<f>I'm joking! I would never do such thing, haha!")
                        .line("<f>Never.")
                        .build()
        ).toArray(DialogueSet[]::new);
    }
}
