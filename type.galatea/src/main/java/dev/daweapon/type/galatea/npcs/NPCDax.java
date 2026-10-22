package dev.daweapon.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.galatea.gui.GUIDax;
import dev.daweapon.type.galatea.npcs.dax.DaxHuntrap;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCDax extends HypixelNPC {
    private static final String MET_TRIGGER = "npc.dax";

    public NPCDax() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Dax", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "VaK3r8cw7g2fNUQp4bnrnGXhHJL4L2FK4x73+DZWBiDHvmd5qeZsWlr7KgjycmZhCAzB5RvlwsDmwJcjH9/MsdUXOSNoj5iHRxlv3vljxXz36jkRgkuvATAjRP3WNXeNSHI2JAZucpm7BtGHTh3ZZQuTd9Y4Suy53frEiv1Z5B0LKifxBDf89tETi+evKUE02cHrQm4evH2D32nAMdVY/PVIUqZc1V90WM25UysUyV/SX+Z5EhVwBmsQO4+0lMhEL723o7U6hAhkK7ut0vrrJ/Z0qLBMyiNxFQSs6scY1ucBT8uRwIMTD/cB6Y/hxQuvNGXbWE+4KchKzwZacNoRFmJTONbGLKeCklQC5pgWCgiVtCEfE2JeHq0hi0Q/Y67Z36Q07JDIP2OZ57FOD50jGkaj9MpXNrD9D3DQdDVY0CP+/r+u8FNrOk9MBn466q0VjCltyYnXTLJATD2Nr4DM9YnLWgBIx0rAhYYyUW5JUSZm0SEfE7Cowal2lceIUqmfDObzAfNFPZgPmGPE96TmrPPzolj3gj+ZM/r7ffc3+hlEc3QCWzucNV5k2MvX2LVB8P2B+8RWGIg2mw6UjsGleNQh5kEWJWKdWDD3VBmByTOMFVoN9bQjtWy38xGwGk+M/5z/SKPrmZm9UiuEfwuZNenJZz7ZbDOI0pOgSPF03XI=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY2MDMxNjk4MDYwNiwKICAicHJvZmlsZUlkIiA6ICIzOTVkZTJlYjVjNjU0ZmRkOWQ2NDAwY2JhNmNmNjFhNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGFyZXN0ZXZlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhkNWM3MzkzNTgyODNkZGIwZWQzMGRmMDdmNzQzOGUxODQyNGIwMTg2YjU5NWRmMjI2NmUwZGJkZTM0NjU1ZjIiCiAgICB9CiAgfQp9";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<b>Dax");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-705.5, 94, 33.5, 45, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
        new DaxHuntrap(new Pos(-705.5, 94, 33.5), MET_TRIGGER);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (ChapterTriggers.done(player, GUIDax.CLAIMED_TRIGGER)) {
            setDialogue(player, "claimed");
            return;
        }
        if (ChapterTriggers.done(player, MET_TRIGGER)) {
            player.openView(new GUIDax());
            return;
        }
        setDialogue(player, "first-interaction").thenRun(() -> {
            ChapterTriggers.fire(player, MET_TRIGGER);
            player.openView(new GUIDax());
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("first-interaction").lines(
                        "Have you got yourself a <c>Huntrap <f>yet?",
                        "I <o>love</o> these things.",
                        "Just set one up on any island, walk away, and when you come back, something's waiting for you!",
                        "You can place them where monsters tend to show up, or even dunk one underwater if you're feeling bold.",
                        "I've caught creatures I'd never be able to snag on my own.",
                        "Ooh! Look! I think I just caught something!",
                        "Anyways, maybe I should give you my spare <c>Huntrap<f>..."
                ).build(),
                DialogueSet.builder().key("claimed").lines(
                        "I've been cutting Fig Trees lately to gather more Tender Wood.",
                        "Once I have enough I will craft myself better Huntraps!"
                ).build()
        };
    }
}
