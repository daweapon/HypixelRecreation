package dev.daweapon.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import java.util.List;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingToolClaimView;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingTutorialProgress;

public class NPCJaeger extends HypixelNPC {

    public NPCJaeger() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Jaeger", "<e><l>CLICK"};
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
                return Text.of("<b>Jaeger");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-651.5, 89, -24.5, 149, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    private static final String MET = "npc.jaeger";
    private static final String CLAIMED = "jaeger.net";
    private static final String USED = "jaeger.used";

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!HotfService.isUnlocked(player)) {
            player.sendMessage("<e>[NPC] <b>Jaeger<f>: <7>{}<f>! Have you talked to <b>Hina <f>at the front of the island to get your hands on a <a>Heart of the Forest<f>?", player.getUsername());
            return;
        }
        if (!ChapterTriggers.done(player, MET)) {
            setDialogue(player, "first-interaction").thenRun(() -> {
                ChapterTriggers.fire(player, MET);
                player.openView(claimView());
            });
            return;
        }
        if (!ChapterTriggers.done(player, CLAIMED)) {
            player.openView(claimView());
            return;
        }
        if (!HuntingTutorialProgress.huntedWith(player, CreatureSpec.Capture.FISHING_NET)) {
            if (player.getAmountInInventory(ItemType.BASIC_FISHING_NET) == 0) {
                player.openView(claimView());
                return;
            }
            setDialogue(player, "before-use");
            return;
        }
        if (!ChapterTriggers.done(player, USED)) {
            setDialogue(player, "after-use").thenRun(() -> ChapterTriggers.fire(player, USED));
            return;
        }
        if (!ChapterTriggers.done(player, "npc.david")) {
            setDialogue(player, "before-david");
            return;
        }
        setDialogue(player, "idle");
    }

    private HuntingToolClaimView claimView() {
        return new HuntingToolClaimView("Basic Fishing Net", ItemType.BASIC_FISHING_NET, true, player -> {
            ChapterTriggers.fire(player, CLAIMED);
            setDialogue(player, "after-accept");
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first-interaction").lines(
                        "Hey, are you <b>David Hunterborough<f>'s new apprentice?",
                        "I've been hunting fish all day long, why don't you give it a try?",
                        "But first grab this <9>fishing net<f>.").build(),
                DialogueSet.builder().key("after-accept").lines(
                        "I'm guessing you have never used a fishing net before?",
                        "All you have to do is throw it at fish you see in the water.",
                        "Try it on this fish right here.").build(),
                DialogueSet.builder().key("before-use").lines(
                        "All you have to do is throw it at any fish you see in the water!",
                        "Try it on this fish right there.").build(),
                DialogueSet.builder().key("after-use").lines(
                        "Well done!",
                        "You've successfully ended the peaceful existence of a fish!",
                        "That's what all hunters live for!",
                        "<b>David Hunterborough<f>'s going to be over the moon about this.",
                        "Make sure to share every gripping detail of your epic conquest with him next.").build(),
                DialogueSet.builder().key("before-david").lines("You should go talk to <b>David Hunterborough <f>next.").build(),
                DialogueSet.builder().key("idle").lines(
                        "Those tiny fish are super cute, that's mostly why I hunt them.",
                        "The creatures deep down this loch are quite scary.").build()
        ).toArray(DialogueSet[]::new);
    }
}
