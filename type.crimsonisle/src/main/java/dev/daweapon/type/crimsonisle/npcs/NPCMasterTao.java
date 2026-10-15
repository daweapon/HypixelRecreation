package dev.daweapon.type.crimsonisle.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.crimsonisle.dojo.DojoBeltRewards;
import dev.daweapon.type.crimsonisle.dojo.DojoService;
import dev.daweapon.type.crimsonisle.dojo.MasterTao;
import dev.daweapon.type.crimsonisle.dojo.gui.GUIDojoChallenges;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Master Tao, at the Dojo: hands out the White Belt, then runs the seven Dojo tests. */
public class NPCMasterTao extends HypixelNPC {

    public NPCMasterTao() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<e>" + MasterTao.NAME, "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZhODI2YjU5YzQzZmYwMjA2NzRlZmFkOTc5M2E0NDhiMGIwMTA3ODY2NTJkMzYxMTRlNjE2NmYzYWNiYmU2OGIifX19";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<e>" + MasterTao.NAME);
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-233.5, 108, -601.5, 45, 0);
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
        if (DojoService.isInChallenge(player) || DojoService.isWaiting(player)) {
            MasterTao.say(player, DojoService.WAITING);
            return;
        }
        if (!DojoProgress.hasMetTao(player)) {
            setDialogue(player, "first").thenRun(() -> {
                DojoProgress.setMetTao(player);
                DojoBeltRewards.giftWhiteBelt(player);
            });
            return;
        }
        player.openView(new GUIDojoChallenges());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("first").lines(
                        "Ah, who are you?",
                        "Hmmm well, are you powerful? I enjoy watching entities display their power... or die in a pit of fire trying!",
                        "Talk to me again, and I'll give you some challenges to entertain me... I mean, prove yourself.",
                        "In fact, here's a little gift to encourage you to give it a try."
                ).build()
        };
    }
}
