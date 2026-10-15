package dev.daweapon.type.crimsonisle.faction;

import net.kyori.adventure.text.format.NamedTextColor;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.skyblockgeneric.crimson.questline.BarbarianStep;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.crimson.questline.MageStep;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CompletableFuture;

/**
 * Odger's "Select a topic: [Quest] [Trophy Fishing]" prompt while the player is on Odexar's
 * Volcanic Stonefish errand (Mage line) or Lasea's fish request (Barbarian line).
 */
public final class OdgerQuestTopics {
    public static final String ODEXAR = "odexar";
    public static final long ODEXAR_CARRYING = 1;
    public static final long ODEXAR_REJECTED = 2;
    public static final long ODEXAR_FRAMED = 3;
    private static final String ODGER = "<e>Odger";
    private static final List<String> STONEFISH = List.of("VOLCANIC_STONEFISH_BRONZE", "VOLCANIC_STONEFISH_SILVER",
            "VOLCANIC_STONEFISH_GOLD", "VOLCANIC_STONEFISH_DIAMOND");

    private OdgerQuestTopics() {
    }

    /** Returns true when Odger offered the topic prompt instead of his normal behaviour. */
    public static boolean intercept(SkyBlockPlayer player, Runnable trophyFishing) {
        boolean odexar = CrimsonQuestlines.mage(player) == MageStep.HELP_ODEXAR
                && CrimsonQuestlines.counter(player, ODEXAR) >= ODEXAR_CARRYING
                && CrimsonQuestlines.counter(player, ODEXAR) < ODEXAR_FRAMED;
        boolean lasea = CrimsonQuestlines.barbarian(player) == BarbarianStep.HELP_LASEA
                && CrimsonQuestlines.counter(player, "lasea_asked") > 0;
        if (!odexar && !lasea) return false;
        NPCOption.sendOption(player, "odger", false, List.of(
                new NPCOption.Option("quest", NamedTextColor.AQUA, true, "Quest", _ -> {
                    if (odexar) odexarTopic(player);
                    else laseaTopic(player);
                }),
                new NPCOption.Option("fishing", NamedTextColor.LIGHT_PURPLE, true, "Trophy Fishing", _ -> trophyFishing.run())));
        player.sendMessage("<e>Select a topic above.");
        return true;
    }

    private static void odexarTopic(SkyBlockPlayer player) {
        if (CrimsonQuestlines.counter(player, ODEXAR) == ODEXAR_CARRYING) {
            CrimsonQuestlines.setCounter(player, ODEXAR, ODEXAR_REJECTED);
            odger(player, "This from Odexar?", "Yep. Bad.", "Small. Rare. Damaged.", "Odexar no talent. No Style.",
                    "Find me better Trophy Fish.", "Don't want to work on fish.");
            return;
        }
        ItemType fish = STONEFISH.stream().map(ItemType::get)
                .filter(type -> type != null && player.getAmountInInventory(type) > 0).findFirst().orElse(null);
        if (fish == null) {
            String[] options = {"Find me Volcanic Stonefish.", "Fish smart. Odexar dumb.", "Volcanic. Volcano."};
            odger(player, options[ThreadLocalRandom.current().nextInt(options.length)]);
            return;
        }
        player.removeItemFromPlayer(fish, 1);
        CrimsonQuestlines.setCounter(player, ODEXAR, ODEXAR_FRAMED);
        odger(player, "Fish good. Better. Much.", "You talent. Good fisherman.",
                "Give this to Odexar. Tell Odexar no more. Tell him.")
                .thenRun(() -> player.addAndUpdateItem(ItemType.FRAMED_VOLCANIC_STONEFISH));
    }

    private static void laseaTopic(SkyBlockPlayer player) {
        if (CrimsonQuestlines.counter(player, "odger_lasea_tip") == 0) {
            CrimsonQuestlines.setCounter(player, "odger_lasea_tip", 1);
            odger(player, "Flyfish. Skeleton Fish.", "You need tip?", "Cheating. You are cheater..", "True fisher no tip.",
                    "Oh Lasea need fish? Lasea cute.", "Don't tell Lasea. Maybe tell. No don't tell.", "AHHHHH",
                    "Flyfish need be high above lava. You.", "Skeleton Fish from Burning Desert. Rare.",
                    "What I said. Forget. Please.");
            return;
        }
        if (ThreadLocalRandom.current().nextBoolean()) odger(player, "Flyfish need be <c>high above lava<f>. You.");
        else odger(player, "Skeleton Fish <c>from Burning Desert<f>. Rare.");
    }

    private static CompletableFuture<Void> odger(SkyBlockPlayer player, String... lines) {
        return CrimsonScene.create().say(ODGER, lines).play(player);
    }
}
