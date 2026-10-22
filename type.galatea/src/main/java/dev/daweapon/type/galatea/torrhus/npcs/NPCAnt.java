package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingNpcCatalogueView;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public class NPCAnt extends HypixelNPC {
    private static final String MET = "npc.ant";

    public NPCAnt() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<2>Ant", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODE1YWUzODI5ZDUwNmE3YTk2NGMyZDFlZTRhYmRiOTg0ZjA3ZWVlOWY0MGIwMGE0YmQxNTczYmIwNzQwOGI5OSIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiIyMDI4OTQ4YTE1MWYyNWJhOGFmNmVjYjM3ZWQyZGQ4YSIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzg0NzkzMzU1ODExfQ==";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<2>Ant");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-615.5, 75, 208.5, 0, 0);
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
        if (!ChapterTriggers.done(player, MET)) {
            setDialogue(player, "first-interaction").thenRun(() -> {
                ChapterTriggers.fire(player, MET);
                player.openView(shop());
            });
            return;
        }
        player.openView(shop());
    }

    private static HuntingNpcCatalogueView shop() {
        return new HuntingNpcCatalogueView("hunting_ant", "ant.stone");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first-interaction").lines(
                        "Howdy there!",
                        "I ain't get many visitors down here. Just me, <2>Ant<f>, and these here <e>ants<f>.",
                        "Say, you wouldn't happen to have any use for these fine <6>stones<f>, would you?",
                        "My ants brought them to me as a peace offering, some time ago.").build()
        ).toArray(DialogueSet[]::new);
    }
}
