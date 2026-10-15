package dev.daweapon.type.crimsonisle.faction;

import net.kyori.adventure.text.format.NamedTextColor;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.function.Consumer;

/** The clickable chat prompts quest NPCs use ("Select an option: [NO] [YES]", "[CLICK TO SHOW]"). */
public final class CrimsonChoices {
    private CrimsonChoices() {
    }

    public static void noYes(SkyBlockPlayer player, String id, Runnable no, Runnable yes) {
        NPCOption.sendOption(player, id, true, List.of(
                option("no", NamedTextColor.RED, "NO", no),
                option("yes", NamedTextColor.GREEN, "YES", yes)));
    }

    public static void yesNo(SkyBlockPlayer player, String id, Runnable yes, Runnable no) {
        NPCOption.sendOption(player, id, true, List.of(
                option("yes", NamedTextColor.GREEN, "YES", yes),
                option("no", NamedTextColor.RED, "NO", no)));
    }

    public static void pick(SkyBlockPlayer player, String id, String firstLabel, NamedTextColor firstColour, Runnable first,
                            String secondLabel, NamedTextColor secondColour, Runnable second) {
        NPCOption.sendOption(player, id, true, List.of(
                option("first", firstColour, firstLabel, first),
                option("second", secondColour, secondLabel, second)));
    }

    /** "[YOU] I have something to show you: [CLICK TO SHOW]". */
    public static void show(SkyBlockPlayer player, String id, String prompt, Runnable action) {
        player.sendMessage("<a>[YOU] <f>" + prompt);
        NPCOption.sendOption(player, id, false, List.of(option("show", NamedTextColor.AQUA, "CLICK TO SHOW", action)));
    }

    private static NPCOption.Option option(String key, NamedTextColor colour, String label, Runnable action) {
        Consumer<dev.daweapon.type.generic.user.HypixelPlayer> consumer = _ -> action.run();
        return new NPCOption.Option(key, colour, true, label, consumer);
    }
}
