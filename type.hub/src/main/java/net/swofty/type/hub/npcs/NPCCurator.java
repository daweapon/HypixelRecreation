package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.museum.GUIYourMuseum;
import net.swofty.type.skyblockgeneric.museum.MuseumService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCCurator extends HypixelNPC {
    private static final int LANDMARK_MILESTONE = 10;
    private static final int NEARLY_COMPLETE_MILESTONE = 30;

    public NPCCurator() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Curator", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "JQaG6dLgLgiEl6c/86bGPlXmXe8Lr2/NV1hPJOVGCHh037TQlcl9OyqOwhn2ofL4ITLS/HTnaX6GiIbDxVaeYPzD+M5hZDTZtgP10QmKLvQirhnRZ84YvyP+eOm7XB5BZj6IfE4yGLAO7ag85zjvUDpOivrna6KPrCMBodWsyafJ4dk8GLf3okyaVTV9y8O7/BwG8R2x2HSZ5nJ9/sFYYynRnx6tDpTN20OLdyZ9wlWOrahoFEQotgfMVhHNm85Ds2EU/Iyo8ngjPgl7shVtRqonD4qDNbS/i9ICNFzKTDqWGW82Wjmi0b5Hd7AG6Pzmlc0lXBwG5wIB53xJjHX5zpucj8rYdTQdV3F+a4uYyWVw0KLmxpapFTVtWhWdOUzK1Le8dKW8dvi8MnP4m0hn30Iad9BqMmg/ko1aGZhkvUv4IhjWSaYvSWMx16rJj8CZZzhjdLCRgyb1XPAZaHr26gZzgSiHnivb97gQIH4ML3MOjtbCGaLTirADSUovALDrlHB8RofFG3mQVzoTF7LSJIXKgpkRi000AJIabrBkW36SiJySTPpaEG/Flme/886bjtU0A31S2vjM19CjgXsXlvPRRbmS94pyLfoLYc23VlmZoaqrAdFDdMkXQYZ354VQeYJ0o82LCPNRoreLiZPa7AQtPrhh+tQVlz4olVqJB10=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYzNTE2OTQwMTIwOSwKICAicHJvZmlsZUlkIiA6ICIxNzhmMTJkYWMzNTQ0ZjRhYjExNzkyZDc1MDkzY2JmYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJzaWxlbnRkZXRydWN0aW9uIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2ZmMjJiMDM0ZDM3MjFkMWZhZDcwODI4ZTM3NTkyNWJhNDFiNWY5NTI4NzNjZDlmYzZkYmJlMWU4MGJlN2JlYjUiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(27.5, 68, 33.5, -180, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;

        int milestone = MuseumService.milestone(player.getMuseumData());
        String line = milestone >= NEARLY_COMPLETE_MILESTONE ? "nearly-complete"
                : milestone >= LANDMARK_MILESTONE ? "landmark" : "starting";
        setDialogue(player, line).thenRun(() -> player.openView(new GUIYourMuseum()));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder().key("starting").lines(
                        "Welcome! The museum is just starting to take shape. Your first few contributions are the seeds of something magnificent!").build(),
                DialogueSet.builder().key("landmark").lines(
                        "Your incredible contributions have transformed the museum into a landmark of cultural heritage.").build(),
                DialogueSet.builder().key("nearly-complete").lines(
                        "The museum is almost complete! Your tireless efforts have brought us to the brink of greatness.").build()
        ).toArray(DialogueSet[]::new);
    }
}
