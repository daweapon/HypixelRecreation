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

public class NPCMaxim extends HypixelNPC {
    private static final String MET = "npc.maxim";

    public NPCMaxim() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Maxim", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2VmZTdlZmE4ZTljMzU0MzRjNTUzNDQ4ZGNlZjc3ZDhkZGVmNGMwMjAxNjc0ZjQwODFjMjlmZWMwYmQzYTdlNCIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiJhZTZjNmI4ZjhhMTcyZjYyYTliYTI2MjMzOWI2NGE0NyIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzg1MzczNzQ0NjM2fQ==";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<6>Maxim");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-574.5, 153, 248.5, 0, 0);
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
        return new HuntingNpcCatalogueView("hunting_maxim", "maxim.booster");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first-interaction").lines(
                        "You know them <9>Boosters <f>you can get from the <2>Moonglade Marsh<f>?",
                        "Well, those puny things ain't gonna cut it 'round these parts.",
                        "Luckily for you, I offer <9>Booster Upgrades<f>, for a price.").build()
        ).toArray(DialogueSet[]::new);
    }
}
