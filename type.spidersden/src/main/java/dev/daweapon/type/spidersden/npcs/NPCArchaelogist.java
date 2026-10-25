package dev.daweapon.type.spidersden.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenRelics;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

// The Archaeologist starts the Relic hunt, hands out the Archaeologist's Compass and pays for finding all 28.
public class NPCArchaelogist extends HypixelNPC {
    private static final int HINTS = 28;

    public NPCArchaelogist() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<b>Archaeologist",
                        "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "T7z9Er9geL6Y9+z7o0u7BUc5kTlbrWw+w6jNDYecdJeTX+u7XfB7C0bgYD4e2MQawQxvI8otTTA8GWrvlgjtfP/xiQD70baSd1u4/bVRKDcquG6Knn7ALmLz34WDlIjZT7N3yVPVuAnauFeYKySCe77pphoaBJCySOfDjr78zeYvTsoWNmq+AJ2BlpsMfErhzKgiuZbRFNEIrjVyUcm/OxWFiqi8nODA85St4Ka4dG9q7cUAjZbI9mfz0cUWPyGe9k1S6Zv2MXiWh/9jNGXXPzjAIPK+uXynm5ihhxJPUJfvB0HE/UX2y3clHwqLlSiG93ju/TE8EBwCXxbPkzlFny2K1aAYlTfhxoTu4J5S6vDleFBhIBTxslneEyALgsYarpS37e0cta6cNx4dwvENaZfR/vekqLfAR6G5FV4P+6q9FbfSsI5rhfpnbgsluY9hkt1J5GCzPVyH1+Lfxse8310yhQBQ0HCDvX7SG5cioXxzFs1Sr/25/bC3/BpfG1Z6PjrdKuTymz5DPbu/pkjXkbti+1yGYmBDbSFz+zurgKxerkzWSumtApuIGGX7nc43XyKBoklfq/5sEZ4IuIijlIlD1pDWtxrpxkSdhFRFibY71PAV/18bez+ABFpr1lbydPCvo8O8yiq0NTAAc8hw66kL3z6cpagvq7SiQOWdPf0=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxMjgwOTU0ODAzMCwKICAicHJvZmlsZUlkIiA6ICIzOTg5OGFiODFmMjU0NmQxOGIyY2ExMTE1MDRkZGU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNeVV1aWRJcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82ODVlODNhMDk5NTFhMmMyZGI3ODM2MGJjOTU0NmJiMGQ0NTQ5NzViNWU1YTM0NGNhMWRhZmY0MDhhNjdlOWYxIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-360.5, 111, -290, -41, 0);
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
        if (!ChapterTriggers.done(player, SpiderDenRelics.COMPASS_FLAG)) {
            ChapterTriggers.fire(player, SpiderDenRelics.COMPASS_FLAG);
            setDialogue(player, "intro").thenRun(() -> player.addAndUpdateItem(new SkyBlockItem(ItemType.ARCHAEOLOGIST_COMPASS)));
            return;
        }
        if (SpiderDenRelics.foundAll(player)) {
            if (SpiderDenRelics.claimCompletionReward(player)) {
                setDialogue(player, "complete").thenRun(() -> player.sendMessage("<6>+" + SpiderDenRelics.COMPLETION_COINS + " coins"));
            } else {
                setDialogue(player, "complete-again");
            }
            return;
        }
        setDialogue(player, "hint-" + (1 + ThreadLocalRandom.current().nextInt(HINTS)));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "Hey, are you an adventurer?",
                        "There are <b>Relics <f>I am searching for all around this island!",
                        "But I am not strong enough to find them.",
                        "Take my <b>compass <f>to assist you in your journey!").build(),
                DialogueSet.builder().key("complete").lines(
                        "Wow! You found all the Relics.",
                        "Take this as a reward, you've earned it.").build(),
                DialogueSet.builder().key("complete-again").line("Thank you again for finding all the Relics.").build(),
                DialogueSet.builder().key("hint-1").line("Some spider eggs look a bit different, check behind those.").build(),
                DialogueSet.builder().key("hint-2").line("Are you sure you looked through every cobweb?").build(),
                DialogueSet.builder().key("hint-3").line("Down there, multiple rooms are blocked by cobwebs.").build(),
                DialogueSet.builder().key("hint-4").line("The spiders are hiding some of the relics behind cobwebs.").build(),
                DialogueSet.builder().key("hint-5").line("Look around spider eggs too, and cobwebs!").build(),
                DialogueSet.builder().key("hint-6").line("Dig through gravel and you should be able to find a relic.").build(),
                DialogueSet.builder().key("hint-7").line("I'm fairly sure there is a Relic nearby the dragon's tail.").build(),
                DialogueSet.builder().key("hint-8").line("Don't forget to look down those caves, there are plenty down there lying around.").build(),
                DialogueSet.builder().key("hint-9").line("I managed to find one behind a spider egg, but once again I just couldn't reach it...").build(),
                DialogueSet.builder().key("hint-10").line("When in doubt, check behind cobwebs!").build(),
                DialogueSet.builder().key("hint-11").line("Look up around the caves, some spiders have their nest behind some cobwebs up there.").build(),
                DialogueSet.builder().key("hint-12").line("I won't lie, some of those I could find myself, but I hate cobwebs.").build(),
                DialogueSet.builder().key("hint-13").line("Spiders often dig up mushrooms and create tiny rooms below, make sure to check those!").build(),
                DialogueSet.builder().key("hint-14").line("There might be some below mushrooms too!").build(),
                DialogueSet.builder().key("hint-15").line("One of the Relics is inside a tiny hole inside the caves.").build(),
                DialogueSet.builder().key("hint-16").line("There are some relics up there hidden on the Spider's Den.").build(),
                DialogueSet.builder().key("hint-17").line("Always check the roof in the caves, there are holes behind the cobwebs.").build(),
                DialogueSet.builder().key("hint-18").line("You know that breakable wall in the caves? Look around it.").build(),
                DialogueSet.builder().key("hint-19").line("Walk around the edges, you will probably find some cave entrances.").build(),
                DialogueSet.builder().key("hint-20").line("I know a spot down in the caves but I just have no idea how to get into that room, the entrance is tiny.").build(),
                DialogueSet.builder().key("hint-21").line("There is a relic a little below a Fairy Soul in the caves.").build(),
                DialogueSet.builder().key("hint-22").line("Make sure you check all the mushrooms, sometimes there are tiny holes behind them.").build(),
                DialogueSet.builder().key("hint-23").line("Sometimes look behind leaves too, there are many hidden caves on this island!").build(),
                DialogueSet.builder().key("hint-24").line("I believe there is one down in the spider cave, behind a wall of cobwebs.").build(),
                DialogueSet.builder().key("hint-25").line("I heard rumors that there is a man-made hole inside the cave, with a Fairy Soul even!").build(),
                DialogueSet.builder().key("hint-26").line("Are you sure you checked every mushroom?").build(),
                DialogueSet.builder().key("hint-27").line("I found one below the house too but I really can't access it, I'm not sure how to get there...").build(),
                DialogueSet.builder().key("hint-28").line("Pretty sure there is one nearby the Dragon skull.").build(),
        };
    }
}
