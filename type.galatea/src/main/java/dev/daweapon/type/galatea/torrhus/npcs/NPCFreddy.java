package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingHints;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingHintsView;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

import java.util.ArrayList;
import java.util.List;

public class NPCFreddy extends HypixelNPC {
    private static final String MET = "npc.freddy";
    private static final String HINTS = "freddy_hints";

    public NPCFreddy() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<a>Freddy", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdmN2ExOTdlZjI3M2QwNDk3NDlkNTJiY2QwNDZlMDNhZWIyMWJjNWQ1NWY0YjViOWQ5NDRjY2YyZGVmZDU0ZiIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiJiMjI3NzllMWJhYjIyNGEwOTAxMTE1MjQyMjc5ZjA0ZiIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzg1Mzc1MDYxODI4fQ==";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<a>Freddy");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-694.5, 94.5, 200.5, 0, 0);
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
                player.openView(hints());
            });
            return;
        }
        player.openView(hints());
    }

    private HuntingHintsView hints() {
        return new HuntingHintsView("Freddy", HuntingHints.get(HINTS), (player, hint) -> {
            ChapterTriggers.fire(player, "freddy.hint");
            setDialogue(player, HuntingHints.dialogueKey(hint));
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        List<DialogueSet> sets = new ArrayList<>();
        sets.add(DialogueSet.builder().key("first-interaction").lines(
                "My brother <a>Teddy <f>and I are some of the greatest hunters who have ever lived.",
                "This here <6>Torrhus Canyon <f>has become a second home to me over the years.",
                "If you ever need tips on how to hunt any <a>Critter <f>on this here island, I'm yer man.").build());
        for (HuntingHints.Hint hint : HuntingHints.get(HINTS)) {
            sets.add(DialogueSet.builder().key(HuntingHints.dialogueKey(hint)).lines(hint.lines().toArray(String[]::new)).build());
        }
        return sets.toArray(DialogueSet[]::new);
    }
}
