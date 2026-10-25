package dev.daweapon.type.galatea.torrhus.npcs;

import dev.daweapon.type.galatea.gui.GUIHoney;
import dev.daweapon.type.galatea.support.TalkNPC;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public class NPCHoney extends TalkNPC {
    private static final String MET = "npc.honey";
    private static final String OPTION_ID = "honey";

    public NPCHoney() {
        super(new Spec("Honey", "6",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOThiYjlmOTMxZjcwNWRlOWZlZGFkYWMyNTJkZjY1MTllZWY2NmE1ZjFjY2YxZGYwMTgxNjBiYzY2NGM0ZDFmNSIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiIxZjdlOGYxOTc0MzAyZGQ3YTUzYmJiYTFkOTc4ZDE4NiIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzg0Nzg3NTI4NDIzfQ==",
                null,
                new Pos(-627.5, 99, 226.5, 0f, 0)));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (ChapterTriggers.done(player, MET)) {
            player.openView(new GUIHoney());
            return;
        }
        setDialogue(player, "intro").thenRun(() -> NPCOption.sendOption(player, OPTION_ID, true, List.of(
                option("yes", NamedTextColor.GREEN, "Yes", p -> setDialogue(p, "yes").thenRun(() -> deface(p))),
                option("no", NamedTextColor.RED, "No", p -> setDialogue(p, "no").thenRun(() -> deface(p))))));
    }

    private void deface(HypixelPlayer player) {
        NPCOption.sendOption(player, OPTION_ID, true, List.of(
                option("deface", NamedTextColor.RED, "Deface them?!", p -> setDialogue(p, "deface").thenRun(() -> {
                    ChapterTriggers.fire((SkyBlockPlayer) p, MET);
                    ((SkyBlockPlayer) p).openView(new GUIHoney());
                }))));
    }

    private static NPCOption.Option option(String key, NamedTextColor colour, String label,
                                           java.util.function.Consumer<HypixelPlayer> action) {
        return new NPCOption.Option(key, colour, false, label, action);
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        String[] agency = {
                "Well, they're an evil group that is all about protecting the <a>Trees<f>!",
                "Any poor tree with a <c>Tree Protection Order <f>on it, you simply can't cut!",
                "Though, while we can't cut them, what we can do is deface them a little..."
        };
        String[] yes = new String[agency.length + 2];
        yes[0] = "Well I'm gonna tell you anyways, because you don't look like someone who knows a lot.";
        yes[1] = "So, about the <c>Tree Protection Agency<f>...";
        System.arraycopy(agency, 0, yes, 2, agency.length);
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines("Psst, have you heard of the <c>Tree Protection Agency<f>?").build(),
                DialogueSet.builder().key("no").lines(agency).build(),
                DialogueSet.builder().key("yes").lines(yes).build(),
                DialogueSet.builder().key("deface").lines(
                        "Yeah, if you progress through the <a>Honeycomb Collection<f>, you'll notice that there's a <a>Fun-Sized Pot of Honeycomb<f>.",
                        "If you make one of those, you can go right up to a <c>Protected Tree <f>and smear <6>Honeycomb <f>all over it!",
                        "Then, if you check back after a while, a <a> Critter <f>will have come along and started eating all the <6>Honeycomb<f>!",
                        "Eh, why don't I just show you a visual?").build()
        };
    }
}
