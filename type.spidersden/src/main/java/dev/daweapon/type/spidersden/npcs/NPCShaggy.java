package dev.daweapon.type.spidersden.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.gui.inventories.spidersden.GUIShaggy;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneFight;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

// Shaggy, on the top floor of Grandma's House, sells Arachne Crystals once the player has beaten Arachne.
public class NPCShaggy extends HypixelNPC {
    private static final String MET = "spiders_den.shaggy.met";
    private static final String OFFERED = "spiders_den.shaggy.offered";

    public NPCShaggy() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<b>Shaggy",
                        "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "HtvR7Hudc9cMplF4TVFyGq8/lAarZEtOLRylnuRu3Gc54dLPvb6x7uOXlhYbSpD3he3kpMR8PEdsvyxYjwrIXdRsgHTjMVPFAPK+zcA3api5UlLSS0VNzMDZDRsbZiFx99OSvmwKPQHSGwSD4gLl8afy/JLTaeu+CCQ6SgnFYcDbegroTuDbEhKJYzw9p3MDksJ8e6iXTlbdEWEzMXvQVVvh3m9fZr1K9AZKHKcKP+I1JQjiQBYGjdUvhXQErCmMoBIxqfRgW7xw3RGAeE6c6w/9kC04PgVG3Vwhcyu4FRWTndRuIp5XkxYdupTwA3m7wgiW638hc9m13kscfXpe8cVjf2j2lrxZGgw5Qo8hVd4nDWLGKimD7Ul1jMX0AzXkJccaVxs6Jg2gdnXyQr2xNviCU45V0DA7+L88B/haTvfWEKipRzgLaXokLk5stUqFtW7PWQa87n6Zlvju9fDYvbtHL24kvB0VMc7gagD+7L46i4heDxDs9hbDmzA9gdE/nZgN45rcnU6Ab6yNs7GVnBICwuHmZpIc7Laki1XbQd+h0GXQYCbfAOh0bDmlY3fHdqxs7SEovwC8l0+vDJHeLHZN8N2+CwxQC72EhVG5j8yzBBqpUfRKBMCJjaG8ud/S1qd3KWBa7Ba0mMEBCvVu/NhyBblo45FodGONMwTtvlo=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxMzA5NTQ2OTc1NSwKICAicHJvZmlsZUlkIiA6ICJhYTZhNDA5NjU4YTk0MDIwYmU3OGQwN2JkMzVlNTg5MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiejE0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzI1OGY4OGRjYmQyYWYxMTBjNzdjZmZhYTAwZWFiN2E0OTljMDAxMzNlNjE1NzU1OTlkOWUwNmU2MWI4YTI0YTYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-278.5, 121, -182.5, 156, -1);
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
        if (!ChapterTriggers.done(player, MET)) {
            ChapterTriggers.fire(player, MET);
            setDialogue(player, "intro");
            return;
        }
        if (!ArachneFight.hasDefeatedEither(player)) {
            setDialogue(player, "hint-" + (1 + ThreadLocalRandom.current().nextInt(2)));
            return;
        }
        if (!ChapterTriggers.done(player, OFFERED)) {
            ChapterTriggers.fire(player, OFFERED);
            setDialogue(player, "offer").thenRun(() -> player.openView(new GUIShaggy(() -> crafted(player))));
            return;
        }
        player.openView(new GUIShaggy(() -> crafted(player)));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "I used to think this world was peaceful, but there are some beings that are stronger than I could ever hope to handle...",
                        "This spider, <4>Arachne<f> - it's way out of my league...",
                        "I can barely defeat her <5>Keepers<f>...").build(),
                DialogueSet.builder().key("hint-1").line(
                        "With enough <9>Arachne's Calling<f>, you can summon her at the <5>Altar<f>.").build(),
                DialogueSet.builder().key("hint-2").line(
                        "You can find <c>Arachne's Keepers <f>in <4>Arachne's Sanctuary<f>.").build(),
                DialogueSet.builder().key("offer").lines(
                        "Wow, you actually challenged that old witch <4>Arachne<f>! You have some potential kid...",
                        "Unfortunately, she has fled to bide her time and grow in power.",
                        "In order to prevent her from growing too strong, we must keep pulling her into this world and weakening her.",
                        "If you're up for the challenge, I can trade you some <5>Arachne Crystals<f>.",
                        "When you place one of these crystals at the <5>Altar <f>you'll pull a stronger version of <4>Arachne <f>into this world.",
                        "While she may drop some rarer items upon defeat, she is a lot stronger.",
                        "Here, take a look.").build(),
                DialogueSet.builder().key("crafted").lines(
                        "Great, this is everything I need! With this I can make the crystal.",
                        "* Hammer and drilling sounds *",
                        "* Glass breaking *",
                        "Here ya go, if you put this on the alter, it will drag that retched creature to this world.",
                        "But be warned, she is strong, and I have it set up to drag her back if she is not defeated in 10 minutes, for the safety of this whole world.",
                        "I wish you good luck!").build(),
        };
    }

    public void crafted(SkyBlockPlayer player) {
        setDialogue(player, "crafted");
    }
}
