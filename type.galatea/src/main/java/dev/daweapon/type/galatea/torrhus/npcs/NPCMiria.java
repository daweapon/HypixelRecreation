package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.gui.inventories.starlyn.GUIStarlyn;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynContests;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynSister;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public class NPCMiria extends HypixelNPC implements NPCAbiphoneTrait {
    private static final String OPTIONS = "miria";

    public NPCMiria() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<d>STARLYN CONTESTS", "<b>Miria", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg4YWI5NTE0YzExY2JlMGU4YzM5ZDYzMzg3ZThjYzZjMWU5NDU5NGYxZGMyYmQ2MjI3ODljNzNkNjc5NWQzZCIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiI4NWY4ZGNjNGMzNzAyNGNhYWUxNjU4ODZiZjFkOGFjMSIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzg0Nzg3MzgzNjAzfQ==";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<b>Miria");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-654.5, 98, 220.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
        StarlynContests.start();
    }

    @Override
    public String getAbiphoneKey() {
        return StarlynSister.MIRIA.key();
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (StarlynContests.data(player, StarlynSister.MIRIA).isMet()) {
            player.openView(new GUIStarlyn(StarlynSister.MIRIA));
            return;
        }
        setDialogue(player, "welcome").thenRun(() -> NPCOption.sendOption(player, OPTIONS, true, List.of(
                option("start", NamedTextColor.GREEN, "Let's do it!", this::explain),
                option("later", NamedTextColor.RED, "Maybe later", p -> setDialogue(p, "welcome-later")))));
    }

    private static NPCOption.Option option(String key, NamedTextColor color, String name, java.util.function.Consumer<HypixelPlayer> action) {
        return new NPCOption.Option(key, color, true, name, action);
    }

    private void explain(HypixelPlayer player) {
        if (isInDialogue(player)) return;
        setDialogue(player, "explanation").thenRun(() -> NPCOption.sendOption(player, OPTIONS, true, List.of(
                option("ready", NamedTextColor.GREEN, "I sure am!", this::begin),
                option("explain-later", NamedTextColor.RED, "I'll come back later", p -> setDialogue(p, "explanation-later")))));
    }

    private void begin(HypixelPlayer player) {
        if (isInDialogue(player)) return;
        SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
        setDialogue(player, "ready").thenRun(() -> {
            StarlynContests.markMet(skyBlockPlayer, StarlynSister.MIRIA);
            ChapterTriggers.fire(skyBlockPlayer, "npc.miria");
            skyBlockPlayer.openView(new GUIStarlyn(StarlynSister.MIRIA));
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("welcome").lines(
                        "Well nice to meet ya, <7>" + player.getUsername() + "<f>!",
                        "About time you made your way to <6>Torrhus Canyon<f>, <b>Agatha <f>has told me all about you!",
                        "So, now that you're here, are you ready to take on your next set of <d>Starlyn Contests<f>?").build(),
                DialogueSet.builder().key("welcome-later").lines("Gonna have a look around the island first? No problem! Toodles!").build(),
                DialogueSet.builder().key("explanation").lines(
                        "Great!",
                        "Just so you know, my <d>Starlyn Contests <f>are partnered with the <2>Critter Safari<f>.",
                        "What that means is, whenever you reach the <a><l>UNCOMMON <r><f>bracket or higher in one of my contests, you'll get a free <2>Safari Ticket <f>alongside your usual <6>rewards<f>!",
                        "Show your ticket to the <2>Safari Manager <f>and he'll give you access to the <2>Critter Safari <f>for a limited time!",
                        "I go there quite often - it's a pretty cool place where you can hunt dozens of <a>Critters <f>to your heart's content.",
                        "The better your ticket, the more free stuff you'll get, so aim for the <b>stars<f>!",
                        "<c><l>#AD!",
                        "So, with all that in mind, are you ready to take on my <d>Starlyn Contests<f>?").build(),
                DialogueSet.builder().key("explanation-later").lines("See you around! Come back anytime!").build(),
                DialogueSet.builder().key("ready").lines("Come take a look at what my <d>Starlyn Contests <f>have to offer!").build()
        ).toArray(DialogueSet[]::new);
    }
}
