package dev.daweapon.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.galatea.gui.GUITeddyTips;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import java.util.ArrayList;
import java.util.List;

public class NPCTeddy extends HypixelNPC {
    private static final String MET_TRIGGER = "npc.teddy";
    private static final String OPTIONS = "teddy";


    public NPCTeddy() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<a>Teddy", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "iYftCZb0NhyhgQ5IQbFNTOBiUN45AwrJrxr9W0mukvrW0bH+0xuOnl7Oe0GXjyeE75aHy0dJZnc6aQCqg0Poa7c6OMkuJxP60GT6a1weatQNBMm0Z8uK/o0dGZU5pJG7y98nzAJFpxXwhpgJucKN46RLjoOcqHKV49rap2wINyR2Jf5DbmqOkp1ktBKPbmUFZrc5kcK1qIlB4jX+0ek0GOiLkjv7pukYleg8ZYoJ10bMGQfJtZcjwC9e+4WGKvi+559X9I4il/jyCilYQMTCuopEkabfUuY68p4nG3dSz6MxW8H1jDJEaz9rMquEw2QlS3ctGF7Ij16ip9Dd1YATfezARvdmvhMMmwRnEZP1eASs6/blQua+bUKwp5GpH3hN+IL0m9fkVhgBmZSF+ILeiQpPxG/nny1YFgIZcOxXH2c7HrBBRgb+hrbm6dyHkosTYSxXcVWXCfS2vxrp+tNs93Ki4flzi3P+flEEJnwG/Eo6gj2GWbAgN/QOuxL96hs4sQf4krQu0Hhvz0eq2Dm29S1xEa320OC6HB1r514ehaSTDhM1ayZLrHTffp5rYCzvS3o7fpWwP7DfhNBcPUsXggh5oT+nkmSPZSVKVLH+ZtxWEtn+gNDfLqGBtNaW05YrcemnWGvY5Qg8t98LFYU4ZLrBbdGZmgful8BjLPWJkk4=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxOTMyODk0OTI0MiwKICAicHJvZmlsZUlkIiA6ICI0MzFhMmRlYTQ4YTE0NTMxYjEyZDU5MzY0NDUxNmIyNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJpQ2FwdGFpbk5lbW8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTA3ZjE2NTdlYmZlZGQwYzM0NzMwYmU3ZDg2M2M1YWFiNjRmYWE0Mjk4MDhhYTEwZTk1MmYwNGZlYjZhMjdiOCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<a>Teddy");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-656.5, 115, -39.5, -101, 0);
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
        if (ChapterTriggers.done(player, MET_TRIGGER)) {
            player.openView(new GUITeddyTips(this));
            return;
        }
        setDialogue(player, "first-interaction").thenRun(() -> NPCOption.sendOption(player, OPTIONS, true, List.of(
                new NPCOption.Option("thanks", NamedTextColor.GREEN, true, "Wow, thanks!", chosen -> {
                    if (isInDialogue(chosen)) return;
                    setDialogue(chosen, "price").thenRun(() -> {
                        ChapterTriggers.fire(player, MET_TRIGGER);
                        player.openView(new GUITeddyTips(this));
                    });
                }))));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        List<DialogueSet> sets = new ArrayList<>();
        sets.add(DialogueSet.builder().key("first-interaction").lines(
                "I am Teddy, the <o>legendary</o> hunter. Yes, yes, I know what you're thinking... no need to bow.",
                "I've walked this earth for 152 years, seen every creature, caught every beast... except... anyways.",
                "I can teach you how to catch anything that walks, swims, or slithers on this island...").build());
        sets.add(DialogueSet.builder().key("price").lines("...For a price, of course. Nothing in life is free, kiddo.").build());
        sets.add(DialogueSet.builder().key("tip-mossybit").lines(
                "Struggling with Frogs? Really?",
                "Back in my day, hunters would catch 'em blindfolded.",
                "Hunters these days... no patience. Go to the lake and look for Mossybits.",
                "Just poke at 'em, and they'll jump like they've seen a ghost. Eventually, they'll tire out.",
                "Easy peasy.").build());
        sets.add(DialogueSet.builder().key("tip-mudworm").lines(
                "Mudworms? You've gotta watch the ground closely... and I mean closely.",
                "Ever seen a faint glow in the dirt? Could be treasure... or a Mudworm.",
                "Either way, you're digging!").build());
        sets.add(DialogueSet.builder().key("tip-coralot").lines(
                "Ah, the Axolotl, tricky little devils. But here's a secret...",
                "Their eyesight is rubbish, they only detect movements. So if you stand still long enough near them, you're golden.").build());
        sets.add(DialogueSet.builder().key("tip-hideonleaf").lines(
                "Shulkers are sneaky. They're like shadows that play tricks on you.",
                "There are Hideonleafs on the island.",
                "Step on them by mistake, and, well, they can get quite angry.",
                "When they attack, smack their projectiles right back at 'em. That'll teach 'em.").build());
        sets.add(DialogueSet.builder().key("tip-lumisquid").lines(
                "Lumisquids hide in awkward places… but that's half the fun, right?",
                "Use your net on them until they shrink down.",
                "They'll poof away like magic, but they never go too far.").build());
        sets.add(DialogueSet.builder().key("tip-joydive").lines(
                "Dolphins... smart and slippery. They'll flee to their dolphin holes if you're not quick enough.",
                "Get that net ready, and don't stop until you catch up to them. But hurry, they won't wait around for a chat.").build());
        sets.add(DialogueSet.builder().key("tip-cod").lines(
                "Cod? Oh, please. The most boring of fish, hands down.",
                "Spin 'em with a fishing net; they'll twirl like a clumsy dancer.",
                "Spin 'em too much, though, and... well, you'll see.").build());
        sets.add(DialogueSet.builder().key("tip-pandas").lines(
                "Pandas? Oh, now we're talking real game!",
                "They're tough to find, but this old hunter knows a trick or two.",
                "Feed 'em bamboo, and they might just love you for a second.",
                "But finding them?",
                "That's a secret only a 165-year-old like me can tell you. I won't though.").build());
        sets.add(DialogueSet.builder().key("tip-salmon").lines(
                "Salmon? Fast as lightning, those slippery devils.",
                "Hit 'em with the net to slow them down. Just don't blink, or you'll be left with water in your boots.").build());
        sets.add(DialogueSet.builder().key("tip-invisibug").lines(
                "Ah, the Invisibug, nearly impossible to see... but not to me, of course.",
                "Look for the glow, throw a lasso, and hope your aim's as sharp as mine.").build());
        sets.add(DialogueSet.builder().key("tip-shellwise").lines(
                "Shellwise, the ancient ones.",
                "Well, ancient compared to you, not me, I'm only 178, after all.",
                "No real tricks here. Just chuck your net until they give up.",
                "It's not pretty, but it works.").build());
        sets.add(DialogueSet.builder().key("tip-spike").lines(
                "Pufferfish... Have you tried looking in the Kelpwoven Tunnels?",
                "I've seen a few, Spikes they are called.",
                "You'll need a special Fishing Net with a long range and stand at a distance from them.").build());
        return sets.toArray(DialogueSet[]::new);
    }
}
