package dev.daweapon.type.galatea.npcs;

import dev.daweapon.type.galatea.support.TalkNPC;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class NPCPounce extends TalkNPC {
    private static final String MET = "npc.pounce";
    private static final String OPTION_ID = "pounce";
    private static final List<String[]> JOKES = List.of(
            new String[]{"Why did the elephant quit the circus?", "He was tired of working for peanuts."},
            new String[]{"What do you call a pig that does karate?", "A pork chop!"},
            new String[]{"Why did the cow go to outer space?", "To see the moooon."},
            new String[]{"Why don't ants get sick?", "Because they have tiny ant-bodies."},
            new String[]{"Why don't oysters give to charity?", "Because they're shellfish."},
            new String[]{"What do you call a fish wearing a bowtie?", "Sofishticated."},
            new String[]{"What do you call a bear with no teeth?", "A gummy bear."},
            new String[]{"Why did the elephant wear green sneakers?", "To hide in the grass!"},
            new String[]{"Why do cows have hooves instead of feet?", "Because they lactose."},
            new String[]{"Why did the dog sit in the shade?", "Because he didn't want to be a hot dog!"});

    public NPCPounce() {
        super(new Spec("Pounce", "b",
                "ewogICJ0aW1lc3RhbXAiIDogMTc2NTM4Mzk4NTM4MSwKICAicHJvZmlsZUlkIiA6ICI4YmM3MjdlYThjZjA0YWUzYTI4MDVhY2YzNjRjMmQyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJub3RpbnZlbnRpdmUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODlkNzI3NDk3YjgxMTdmNGVmMDUwMzVlYzdhZjk1MGQzOTVmYWNhZDI0MzI3ZTk0MGUyYzM1NzdkYmM3OGMzMyIKICAgIH0KICB9Cn0=",
                "s1z9Y7nV+MnWANFSM/UPE6hHYeKGbHWoMXZe4gzKNbHYq3kJ2Ird2tvdR1IuIwHpJR0Qb67ifsii6PeBkRBLkX53N+xqiY7NVGAK2aIuIZ7PnELcaOIi3eXhW26mUXP6rbpW4v/wkwfdtfb0B9e1DYd8iRq85CYvbKXE1Jk/+wbid6VoohP53K7ai7Ox+2m+lD3FwvwcX9K/BgNjC9qhunjdwK9wl9SJCa2m8+DEIiketKP36wIQjgwvDVNNsX9IL+VqEeGXCWE1UqHRnoH7HZqhC5JpO2DQ542CElT8JLgrVbhP+zg0+oMuW9rXZzoNnSA5SduZlGPTDkknHuKciyyw4EXFv2VwgxHtaJI8qui/WrGPrIXd/BcLCzHxI8XMdOJwBRd8lAxGozyY/9fUAObCjTsDD9F/u5iqso5j2EsYHbiM16YgDZK8WOPIT4WQxq/vNOWSAiPm/B8BCCKz/N0NAJ9jNS9E4ch8A9t50k8KrW4ZuaaDQaNOMJdJdHfHAt7Er4jdcAM9cyz6O/YQxAa1Ft7vreJpm/6WT/dS97w+V5OemPUzLjixi2WjtDVeO+6httW1bV1hO3flOgj2TxamzTZpTdnXCmMkTKXJGdlXOi//+N0RzTHbgU5Wbh57YciqtT938iBc4wnK4LS5OpPtvzF9vcLY6/JWJuwU4wM=",
                new Pos(-598.5, 127, -20.5, -90f, 0)));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (ChapterTriggers.done(player, MET)) {
            setDialogue(player, "joke-" + ThreadLocalRandom.current().nextInt(JOKES.size()));
            return;
        }
        setDialogue(player, "intro").thenRun(() -> ask(player, "fault", "That's not your fault.", "pets",
                () -> ask(player, "well", "Well...", "chameleon",
                        () -> ask(player, "dots", "...", "outro", () -> ChapterTriggers.fire(player, MET)))));
    }

    private void ask(HypixelPlayer player, String key, String label, String dialogueKey, Runnable next) {
        Consumer<HypixelPlayer> action = p -> setDialogue(p, dialogueKey).thenRun(next);
        NPCOption.sendOption(player, OPTION_ID, true,
                List.of(new NPCOption.Option(key, NamedTextColor.GREEN, false, label, action)));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        DialogueSet[] story = {
                DialogueSet.builder().key("intro").lines("Hi!", "I'm David's assistant.", "I love nature and animals.",
                        "But I'm not sure I'm very talented.", "I saved a bee from a pool once.",
                        "But the bee still stung me.", "And died...").build(),
                DialogueSet.builder().key("pets").lines("I tried getting pets too but that didn't work very well.",
                        "I bought a bird pet.", "And then it flew away.").build(),
                DialogueSet.builder().key("chameleon").lines("So the next time I tried with a chameleon.",
                        "I brought it back home but it camouflaged itself.", "And now I can't find it.").build(),
                DialogueSet.builder().key("outro").lines("Anyways, in my free time I tell jokes.",
                        "I'll tell you all my jokes if you want.").build()
        };
        DialogueSet[] sets = new DialogueSet[story.length + JOKES.size()];
        System.arraycopy(story, 0, sets, 0, story.length);
        for (int i = 0; i < JOKES.size(); i++) {
            sets[story.length + i] = DialogueSet.builder().key("joke-" + i).lines(JOKES.get(i)).build();
        }
        return sets;
    }
}
