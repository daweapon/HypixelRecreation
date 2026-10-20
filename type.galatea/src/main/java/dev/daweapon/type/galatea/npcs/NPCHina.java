package dev.daweapon.type.galatea.npcs;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.skyblockgeneric.gui.inventories.chapters.GUIChapters;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public class NPCHina extends HypixelNPC {
    private static final int REQUIRED_FORAGING_LEVEL = 12;

    public NPCHina() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Hina", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTEzYmU0ZDM3YTM1ODQwMDI0YjI2ZjhkNjIzZmYxNDExOWU3NjE3ODQ1OTJmMWEyY2M0YjAxZmNhOGZiN2IyIiwibWV0YWRhdGEiOnt9fX0sInByb2ZpbGVJZCI6ImY3Nzg0Y2I0N2UzZjJjMjNiMTA1M2YwNTA4MzU2ODkzIiwicHJvZmlsZU5hbWUiOiJuZWE4OSIsImlzUHVibGljIjp0cnVlLCJ0aW1lc3RhbXAiOjE3NTA3MDE0NTI1NDF9";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<b>Hina");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-553.5, 110, -16.5, 0, 0);
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
        if (HotfService.isUnlocked(player)) {
            if (ChapterService.allComplete(player)) setDialogue(player, "chapters-complete");
            else player.openView(new GUIChapters());
            return;
        }
        if (player.getSkills().getCurrentLevel(SkillCategories.FORAGING) < REQUIRED_FORAGING_LEVEL) {
            player.sendMessage("<c>You need Foraging XII to unlock the Heart of the Forest!");
            return;
        }
        setDialogue(player, "first-interaction").thenRun(() -> {
            HotfService.unlock(player);
            player.sendMessage(Text.of("<a><l>HEART OF THE FOREST <r><f>has been unlocked! Open your <a>SkyBlock Menu <f>to find it. <hover:'<e>Click to open!'><click:run:'/hotf'><e><l>[CLICK HERE TO OPEN]</click></hover>"));
            setDialogue(player, "tasks").thenRun(() -> ChapterService.evaluate(player));
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first-interaction").lines(
                        "Hola!",
                        "Welcome to <2>Galatea<f>!",
                        "I'm here to give you your own <a>Heart of the Forest<f>.",
                        "Everyone has one around here!",
                        "The <a>Heart of the Forest <f>can be found in your <a>SkyBlock Menu <f>while on <2>Galatea<f>.",
                        "You can unlock and level up various perks and abilities there.",
                        "You can talk to <b>Agatha <f>near the lake to gain <a>HOTF Exp<f>.",
                        "To level up your perks you will need <3>Forest Whispers<f>. These are obtained by cutting trees!"
                ).build(),
                DialogueSet.builder().key("tasks").lines(
                        "I also have tasks for you to do while on this island.",
                        "For each task I will reward you handsomely!"
                ).build(),
                DialogueSet.builder().key("chapters-complete")
                        .line("You've done what few even try. Each creature you met each tree you felled, it all shaped you. And now, you shape <2>Galatea<7>.")
                        .line("Look at you. Sweeping like a storm, hunting like a whisper. I couldn't be prouder.")
                        .line("You've mastered every path I know.")
                        .line("Congratulations <b>{}<f>.", player.getUsername())
                        .build()
        ).toArray(DialogueSet[]::new);
    }
}
