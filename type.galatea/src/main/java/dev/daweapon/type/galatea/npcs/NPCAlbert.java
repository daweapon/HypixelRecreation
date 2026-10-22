package dev.daweapon.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import java.util.List;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingNpcCatalogueView;

public class NPCAlbert extends HypixelNPC {

    public NPCAlbert() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Albert", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "bfmPL4+s1uE93bOGHyLlZahoF+cL/AOsDG3/IQEMwZYz+PxF4J93NOoS+cA2lRlWtKC6GFaR1wmrWPdT9viQlVSsSilFH4v7HNUoIq+s2ejhyo4R5L8cxMlfmXLrhHlE/7g8M+c1+o/G1pA9T/3o7AAS/T1W7I7PIa7/FMgUBg9xztITbFmzvFnbeDX0d39r4yOcTlFMLF/bqZJVfwlxvMhUoKfkEmJgFW3kBTM9arsUIrJmo1D8iwCV+Tggc1Elpad5XL/9ZoTjgqIIkBqkWPoC6gyPyC5ciyoJjxEi8k8YPAoM0f0CKzh8c7HaEp9lwQLkgN53Jh2dOIuRkzbUJc/3eui//4YihXfDkz4AUAMDURPQJAGxflJEiG/6ItcqVzFdewiaaucclRtBAqOkLHzxsb7IIbuTYOhlVSCJkvRUfqVUxte+ybSmIV0mBO/jOElRZuaKFdYe0q70ZZoYOATSUbMZ4KCWSBKDse5nmX5TC4yNOIP1BwAT7l60Zndk8WsnFm2hoUYXI/85KOXyqFVSfeA97GESyB5tXZUvGFX6F4EC0F+gxB1RKpuVdAjn6CePEZPiRDIz+JrqK43w3/pKWZYCbnaR1G63hHQaQWJe/Z7w/nrGLHEHXD+9d8lF9nxabrDaf+vJ9UAlD8U5PzoEFh5Zj/XmwQ0xaktVzyM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5ODk4OTAzNjA5MSwKICAicHJvZmlsZUlkIiA6ICJkODAwZDI4MDlmNTE0ZjkxODk4YTU4MWYzODE0Yzc5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVCTFJ4eCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84NmU3YmNmYzA1NjkwOGNlMjY3MDYyMTQwYzUzNDM1MDNmYjA4YmRlODFkYTk5NjJhMWI2NjQ5YWE5NTkxMDk4IgogICAgfQogIH0KfQ==";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<b>Albert");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-592.5, 113, 8.5, -180, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    private static final String MET = "npc.albert";

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!HotfService.isUnlocked(player)) {
            player.sendMessage("<e>[NPC] <b>Albert<f>: <7>{}<f>! Have you talked to <b>Hina <f>at the front of the island to get your hands on a <a>Heart of the Forest<f>?", player.getUsername());
            return;
        }
        if (!ChapterTriggers.done(player, MET)) {
            setDialogue(player, "first-interaction").thenRun(() -> {
                ChapterTriggers.fire(player, MET);
                player.openView(new HuntingNpcCatalogueView("hunting_albert", null));
            });
            return;
        }
        player.openView(new HuntingNpcCatalogueView("hunting_albert", null));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first-interaction").lines(
                        "Hello!",
                        "This device right next to me is a <d>Gravity Condenser<f>.",
                        "I didn't build it, but I know how to use it.",
                        "If you bring me the right materials, and the proper fee, then I'll craft you a <5>Pocket Black Hole<f>.").build()
        ).toArray(DialogueSet[]::new);
    }
}
