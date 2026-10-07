package net.swofty.type.hub.npcs;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.vincent.GUIVincent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCVincent extends HypixelNPC {

    public NPCVincent() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Vincent", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "l9pYvAwAs6liRRBKySCecGDU8zERatdYPFOpx2s/kc9daZ67hwUe6MIHojxyu0+dtyhwAPf18pBEvR58HvZjpD1OWfkDQtPrVJscyzAiJdZVfze7kxsUHOIA9I9x6Dd3mUSukyZLDpmH6nNVetKn2W82nwOPBxT8wXpVIYizZGSGFrl6AXW1UUiWr6sJI0+7yGhph5q+QtKFGrfp+FNd7IwahSOAuk41xfdu4cze5CLk35QMWvKW3V2ItX7KOkJv1kooAnzYXWfMoiM0FT+liY+OOWUI8eAgPngCMmQN7+W6W7gu5Aea2TM2mDKufG/52H7CiagrSDKk/jlxlP9/1QeR09uchAjbOibSFxU1OFqGmcIyfUN1cq19WV0C5YQd+6AbyC3Hblde+QzjsyNLe7R3KvMpB5zlYWyb84uoRi2ogWuifE6rswoxBWJgtUCMBf/MKu5XzW6eoOpwNGLt5qHtr9QyxeqPp9XbBgn6syfAlTwqHx7utf+gu3SSBZbNe6cf2OE/ngxsPxDri3Tt3M9NTdS485Ja4Ic9JZkpz6I7GsaG+3nOClRWX16eseGTDgXGpcJ3OUZz25obuHWEQScqq7oI1XH8KnVENPhzy8VZ4vhZVUZBPgjnsVcE7K5q9Trbk75C1zJAaplM+XrQs2Cr1Ig2HnUn3Vy2ixWkRGY=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxNzYyMDA2OTUyOSwKICAicHJvZmlsZUlkIiA6ICIwMzQ2N2E0Yzc5ZGU0ZGM5YTQ4NzU5MGY5NmEwODFmMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTaGFrYVlhbWkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJhM2M0ZjU4MmZjODc0OWUzZDhlMzVlZjFlNjMyODBjNDc1MmU0YWM4M2FhYjY5ZjY3NGRhYzI0YTM3NTgwMyIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(79.5, 74, 53.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<b>Vincent";
            }
        });
    }

    private static final int REQUIRED_SKYBLOCK_LEVEL = 10;

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;

        if (player.getSkyBlockExperience().getLevel().asInt() < REQUIRED_SKYBLOCK_LEVEL) {
            setDialogue(player, "low-level");
            return;
        }
        if (player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_VINCENT)) {
            setDialogue(player, "open-" + (1 + (int) (Math.random() * 3)));
            player.openView(new GUIVincent());
            return;
        }

        setDialogue(player, "introduction").thenRun(() -> NPCOption.sendOption(player, "vincent", true, List.of(
            new NPCOption.Option("yes", NamedTextColor.GREEN, false, "Yes...", p -> explain(p, "knows-dyes")),
            new NPCOption.Option("no", NamedTextColor.RED, false, "No, what are they?", p -> explain(p, "explain-dyes"))
        )));
    }

    private void explain(HypixelPlayer player, String dialogueKey) {
        setDialogue(player, dialogueKey)
            .thenCompose(ignored -> setDialogue(player, "painting"))
            .thenRun(() -> player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_VINCENT, true));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
            DialogueSet.ofTranslation("low-level", "npcs_hub.vincent.dialogue.low_level"),
            DialogueSet.ofTranslation("introduction", "npcs_hub.vincent.dialogue.introduction"),
            DialogueSet.ofTranslation("knows-dyes", "npcs_hub.vincent.dialogue.knows_dyes"),
            DialogueSet.ofTranslation("explain-dyes", "npcs_hub.vincent.dialogue.explain_dyes"),
            DialogueSet.ofTranslation("painting", "npcs_hub.vincent.dialogue.painting"),
            DialogueSet.ofTranslation("open-1", "npcs_hub.vincent.dialogue.open_1"),
            DialogueSet.ofTranslation("open-2", "npcs_hub.vincent.dialogue.open_2"),
            DialogueSet.ofTranslation("open-3", "npcs_hub.vincent.dialogue.open_3")
        };
    }
}
