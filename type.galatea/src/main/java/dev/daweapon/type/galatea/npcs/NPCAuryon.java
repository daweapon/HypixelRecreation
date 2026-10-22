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
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingToolClaimView;
import dev.daweapon.type.skyblockgeneric.hunting.npc.HuntingTutorialProgress;
import java.util.concurrent.ThreadLocalRandom;

public class NPCAuryon extends HypixelNPC {

    public NPCAuryon() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Auryon", "<e><l>CLICK"};
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
                return Text.of("<b>Auryon");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-557.5, 111, 48.5, 135, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    private static final String MET = "npc.auryon";
    private static final String CLAIMED = "auryon.lasso";
    private static final String HUNTED = "auryon.hunted";
    private static final String PHANPYRE = "PHANPYRE";

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!HotfService.isUnlocked(player)) {
            player.sendMessage("<e>[NPC] <b>Auryon<f>: <7>{}<f>! Have you talked to <b>Hina <f>at the front of the island to get your hands on a <a>Heart of the Forest<f>?", player.getUsername());
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
        if (!HuntingTutorialProgress.hunted(player, PHANPYRE)) {
            if (player.getAmountInInventory(ItemType.ABYSMAL_LASSO) == 0) {
                player.openView(claimView());
                return;
            }
            setDialogue(player, "before-hunt");
            return;
        }
        if (!ChapterTriggers.done(player, HUNTED)) {
            setDialogue(player, "after-hunt").thenRun(() -> ChapterTriggers.fire(player, HUNTED));
            return;
        }
        if (!ChapterTriggers.done(player, "npc.david")) {
            setDialogue(player, "before-david");
            return;
        }
        setDialogue(player, ThreadLocalRandom.current().nextBoolean() ? "idle-phanpyre" : "idle-lasso");
    }

    private HuntingToolClaimView claimView() {
        return new HuntingToolClaimView("Abysmal Lasso", ItemType.ABYSMAL_LASSO, false, player -> {
            ChapterTriggers.fire(player, CLAIMED);
            setDialogue(player, "after-accept");
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder().key("first-interaction").lines(
                        "Hello!",
                        "I was cutting a tree when a <c>Phanpyre <f>suddenly appeared.",
                        "Hunting <2>Forest <f>creatures is very different from <9>Water <f>or <c>Combat <f>creatures.",
                        "For this one, you need a Lasso.",
                        "I have a spare one here, take it.").build(),
                DialogueSet.builder().key("after-accept").lines(
                        "Great! Now throw the Lasso at the <c>Phanpyre<f>!",
                        "When its stamina bar empties, <a>right-click <f>to reel it in!").build(),
                DialogueSet.builder().key("before-hunt").lines(
                        "Great, now throw the Lasso at the Phanpyre and hold on!",
                        "Once their stamina bar is down, <e><l>RIGHT CLICK <r><f>your Lasso to Reel!").build(),
                DialogueSet.builder().key("after-hunt").lines(
                        "Alright alright alright!",
                        "That's what I'm talking about!",
                        "This Lasso isn't the best, but it gets the job done.",
                        "You should head back to <b>David Hunterborough<f>.").build(),
                DialogueSet.builder().key("before-david").lines("You should go back to <b>David Hunterborough<f>.").build(),
                DialogueSet.builder().key("idle-phanpyre").lines("I keep finding Phanpyres, but what I'm really looking for are Dreadwings.").build(),
                DialogueSet.builder().key("idle-lasso").lines("Have you got a better Lasso yet? I'm still looking for a better one myself.").build()
        ).toArray(DialogueSet[]::new);
    }
}
