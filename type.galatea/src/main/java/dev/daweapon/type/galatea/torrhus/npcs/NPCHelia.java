package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterIsland;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.gui.inventories.chapters.GUIChapters;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public class NPCHelia extends HypixelNPC {
    public NPCHelia() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<2>Helia", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6IHsiU0tJTiI6IHsidXJsIjogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWM5ZGQ3Y2M3ZWM0YWYyM2RlMWVhOTM4YjFkMmJmMjA5NDJhZTlhNjQ5YWEzMDRkNWQ4N2IxZWFlZDBjYjJhZiJ9fX0=";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<2>Helia");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-690.5, 93, 155.5, 0, 0);
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
            player.sendMessage("<c>Talk to Hina in Tangleburg's Path on Galatea to unlock your Heart of the Forest first!");
            return;
        }
        if (ChapterTriggers.done(player, ChapterIsland.TORRHUS_CANYON.gateTrigger())) {
            if (ChapterService.allComplete(player, ChapterIsland.TORRHUS_CANYON)) {
                setDialogue(player, "chapters-complete");
            } else {
                player.openView(new GUIChapters(ChapterIsland.TORRHUS_CANYON));
                setDialogue(player, "future");
            }
            return;
        }
        setDialogue(player, "first-interaction").thenRun(() -> {
            ChapterTriggers.fire(player, ChapterIsland.TORRHUS_CANYON.gateTrigger());
            player.openView(new GUIChapters(ChapterIsland.TORRHUS_CANYON));
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first-interaction").lines(
                        "Heyhey! Welcome to <6>Torrhus Canyon<f>!",
                        "Under the heat of the scorching sun, your journey's just begun!",
                        "I'm in charge of the <6>Torrhus Chapters <f>- complete my tasks and claim your <6>Rewards<f>!",
                        "So, before you go dashing off to find <b>Miria <f>and take part in her <d>Starlyn Contest<f>, why don't you take a look?").build(),
                DialogueSet.builder().key("future").lines(
                        "Have you spoken to <b>Miria <f>yet? She's just down the <6>Spring Path<f>!",
                        "Simply turn left at the crossroads up ahead and keep going until you see her!").build(),
                DialogueSet.builder().key("chapters-complete").lines(
                        "Wow, you actually did it!",
                        "You've completed all of the <6>Torrhus Chapters<f>!",
                        "You're one of a kind - a four-leaf clover among the foliage!",
                        "Great job, " + player.getUsername() + "! I look forward to seeing where your journey takes you next!").build()
        ).toArray(DialogueSet[]::new);
    }
}
