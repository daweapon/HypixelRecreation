package dev.daweapon.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.text.Text;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.galatea.npcs.oden.OdenChillshotQuest;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import java.util.List;

public class NPCOden extends HypixelNPC {
    private static final List<String> BLACK_HOLES = List.of("SMALL_POCKET_BLACK_HOLE", "MEDIUM_POCKET_BLACK_HOLE");
    private static final String MET_TRIGGER = "npc.oden";
    private final OdenChillshotQuest quest;

    public NPCOden() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Oden", "<e><l>CLICK"};
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
                return Text.of("<b>Oden");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-584.5, 113, -40.5, 49, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
        quest = new OdenChillshotQuest(new Pos(-584.5, 113, -40.5), player -> setDialogue(player, "after-hunt"));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!HotfService.isUnlocked(player)) {
            player.sendMessage("<e>[NPC] <b>Oden<f>: <7>{}<f>! Have you talked to <b>Hina <f>at the front of the island to get your hands on a <a>Heart of the Forest<f>?", player.getUsername());
            return;
        }
        if (!ChapterTriggers.done(player, MET_TRIGGER)) {
            setDialogue(player, "first-interaction").thenRun(() -> {
                ChapterTriggers.fire(player, MET_TRIGGER);
                quest.start(player);
            });
            return;
        }
        if (!ChapterTriggers.done(player, OdenChillshotQuest.DONE_TRIGGER)) {
            setDialogue(player, "before-hunt").thenRun(() -> quest.start(player));
            return;
        }
        setDialogue(player, ownsBlackHole(player) ? "idle" : "black-hole");
    }

    private static boolean ownsBlackHole(SkyBlockPlayer player) {
        for (String id : BLACK_HOLES) {
            ItemType type = ItemType.get(id);
            if (type != null && player.countItem(type) > 0) return true;
        }
        return false;
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("first-interaction").lines(
                        "<b>David Hunterborough <f>is sending you?",
                        "I see.",
                        "Quick, help me deal with this <c>Chillshot<f>!",
                        "<a>Weaken it<f>, but <c>do not kill it<f>! Punch it towards my <5>Black Hole<f>!",
                        "Monsters here only take damage from <a>axes<f>!",
                        "<f>Use your <a>Huntaxe <f>on them!"
                ).build(),
                DialogueSet.builder().key("before-hunt").lines("Quick, help me deal with this Chillshot!").build(),
                DialogueSet.builder().key("after-hunt").lines(
                        "Great work! You handled that <c>Chillshot <f>like a professional hunter!",
                        "As you saw, the best way to deal with monsters here is to use a <5>Black Hole<f>.",
                        "<f>If you'd like one, you can purchase a <5>Pocket Black Hole<f> from <b>Albert<f> over in the village."
                ).build(),
                DialogueSet.builder().key("black-hole").lines(
                        "As you can see, the best way to hunt <c>Combat <f>creatures is with a <5>Pocket Black Hole<f>.",
                        "If you want one, you should go talk to <b>Albert<f>, in the Village."
                ).build(),
                DialogueSet.builder().key("idle").lines("I'm still trying to hone my Hunting Skills.").build()
        };
    }
}
