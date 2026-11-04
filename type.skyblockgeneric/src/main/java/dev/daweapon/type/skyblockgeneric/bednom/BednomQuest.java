package dev.daweapon.type.skyblockgeneric.bednom;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.greatspook.GreatSpookEvent;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.armor.WornArmor;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.timer.TaskSchedule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class BednomQuest {
    public static final String SPEAKER = "<e>[NPC] <d>Bednom<f>: ";
    public static final int TREASURE_X = -12;
    public static final int TREASURE_Y = 127;
    public static final int TREASURE_Z = 57;
    public static final long MITHRIL_POWDER = 500;
    public static final String ACHIEVEMENT = "skyblock.nightmare";
    public static final int TREASURE_FOUND = 4;
    private static final int LINE_DELAY_TICKS = 30;
    private static final String STAGE = "stage";
    private static final String LAST_TALK = "last_talk";
    private static final String COFFEE_AT = "coffee_at";
    private static final String NULLIFIER = "nullifier";
    private static final Set<String> WEAK_COFFEES = Set.of("CHEAP_COFFEE", "DECENT_COFFEE");
    private static final String BLACK_COFFEE = "BLACK_COFFEE";

    private static final List<List<String>> DAYS = List.of(
            List.of("I just had a very very strange dream...",
                    "In my dream I flew out of my body, went through some kind of screen barrier.",
                    "I turned and I could see myself standing behind that screen, right here.",
                    "Around me were strange objects everywhere, but the strangest part was the giant.",
                    "A Giant! Sitting in front of that screen, watching me.",
                    "The giant seemed to speak a foreign language, they screamed \"POGGERS!\"",
                    "Then I woke up in a sweat.",
                    "I'm really upside down, I need time to think.",
                    "Thanks for listening."),
            List.of("I had another nightmare, very similar to the previous one.",
                    "I could see myself behind the screen again, it looked like I was talking.",
                    "I couldn't hear it, but the words appeared on the screen.",
                    "The exact words I told you last time.",
                    "So weird, so weird.... I need time to think."),
            List.of("Once again, I had the same recurring nightmare, a little different this time.",
                    "I was talking behind the screen again, I mentioned a treasure.",
                    "I knew the location of a treasure right right close to a giant Dwarf Statue.",
                    "Buried in loose rocks next to a bridge I said.",
                    "But it makes no sense, I've never even heard of such a treasure!!"));
    private static final List<String> WAITING = List.of(
            "I wonder if I'll have that nightmare again tonight...",
            "Wow that dream was weird.",
            "I'm still thinking about that dream I had.");
    private static final String BEFORE_TREASURE = "Dwarf Statue, bridge,, loose rocks, treasure...? What a strange dream...";
    private static final String AFTER_TREASURE = "The dreams stopped, it'll forever remain a mystery.";
    private static final List<String> OFFERED = List.of("What's this? A brew?", "I need to drink this?",
            "Okay well, I trust you.", "<d><o>gulp");
    private static final String WEAK_RESULT = "Meh, I doubt this will do anything...";
    private static final List<String> BLACK_RESULT = List.of("Holy crap this tastes terrible!!!", "What's the point!!!!",
            "I guess it was rhetorical, uh?");
    private static final List<String> REWARD = List.of("WOW!!!!", "I REALLY CAN'T SLEEP AT ALL!!!!",
            "THAT'S FANTASTIC!!!!", "NO MORE NIGHTMARES!!!!", "HERE TAKE THIS!!!!");
    private static final List<String> TOO_LATE = List.of("That <e>drink <f>you gave me the other day...",
            "It was amazing! I couldn't have nightmares that night!",
            "You should give me another and visit in the morning!", AFTER_TREASURE);
    private static final List<String> AWAKE = List.of("AM I TOO LOUD??!!",
            "I HAD NO IDEA I COULD JUST STAY AWAKE, IT'S GENIUS!!!",
            "WHY SLEEP WHEN YOU CAN JUST STAY AWAKE FOREVER!!!!");

    private BednomQuest() {
    }

    public static void talk(SkyBlockPlayer player) {
        long now = SkyBlockCalendar.getElapsed();
        int stage = (int) get(player, STAGE);
        if (stage < TREASURE_FOUND) {
            talkBeforeTreasure(player, stage, now);
            return;
        }
        if (get(player, NULLIFIER) > 0) {
            say(player, List.of(AWAKE.get(ThreadLocalRandom.current().nextInt(AWAKE.size()))));
            return;
        }
        long coffeeAt = get(player, COFFEE_AT);
        if (coffeeAt > 0) {
            talkAfterCoffee(player, now, coffeeAt);
            return;
        }
        if (GreatSpookEvent.isActive() && offerCoffee(player, now)) return;
        say(player, List.of(AFTER_TREASURE));
    }

    private static void talkBeforeTreasure(SkyBlockPlayer player, int stage, long now) {
        boolean dayPassed = stage == 0 || now - get(player, LAST_TALK) >= SkyBlockCalendar.DAY;
        if (stage < DAYS.size() && dayPassed) {
            put(player, STAGE, stage + 1);
            put(player, LAST_TALK, now);
            say(player, DAYS.get(stage));
            return;
        }
        if (stage >= DAYS.size()) {
            say(player, List.of(BEFORE_TREASURE));
            return;
        }
        say(player, List.of(WAITING.get(ThreadLocalRandom.current().nextInt(WAITING.size()))));
    }

    private static boolean offerCoffee(SkyBlockPlayer player, long now) {
        String held = WornArmor.id(player.getItemInMainHand());
        boolean black = BLACK_COFFEE.equals(held);
        if (!black && !WEAK_COFFEES.contains(held)) return false;
        ItemType type = ItemType.get(held);
        if (type == null) return false;
        player.removeItemFromPlayer(type, 1);
        List<String> lines = new java.util.ArrayList<>(OFFERED);
        if (black) {
            put(player, COFFEE_AT, now);
            lines.addAll(BLACK_RESULT);
        } else {
            lines.add(WEAK_RESULT);
        }
        say(player, lines);
        return true;
    }

    private static void talkAfterCoffee(SkyBlockPlayer player, long now, long coffeeAt) {
        long waited = now - coffeeAt;
        if (waited < SkyBlockCalendar.DAY) {
            say(player, List.of(AFTER_TREASURE));
            return;
        }
        put(player, COFFEE_AT, 0);
        if (waited >= 2L * SkyBlockCalendar.DAY || !GreatSpookEvent.isActive()) {
            say(player, TOO_LATE);
            return;
        }
        put(player, NULLIFIER, 1);
        say(player, REWARD);
        schedule(REWARD.size() * LINE_DELAY_TICKS, () -> {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.NIGHTMARE_NULLIFIER));
            player.sendMessage("<a>You received <a>1x Nightmare Nullifier<a>!");
        });
    }

    public static boolean isTreasure(Point block) {
        return block.blockX() == TREASURE_X && block.blockY() == TREASURE_Y && block.blockZ() == TREASURE_Z;
    }

    public static boolean digTreasure(SkyBlockPlayer player) {
        if (get(player, STAGE) != DAYS.size()) return false;
        put(player, STAGE, TREASURE_FOUND);
        long powder = HotmService.gainPowder(player, HotmPowder.MITHRIL, MITHRIL_POWDER);
        for (Map.Entry<ItemType, Integer> loot : treasure().entrySet()) {
            player.addAndUpdateItem(new SkyBlockItem(loot.getKey(), loot.getValue()));
        }
        player.playSound(Sound.sound(Key.key("block.chest.open"), Sound.Source.BLOCK, 1f, 1f));
        player.sendMessage("<6><l>TREASURE! <r><e>You dug up Bednom's buried treasure chest!");
        player.sendMessage("<7>+ <2>" + String.format("%,d", powder) + " Mithril Powder");
        player.getAchievementHandler().completeAchievement(ACHIEVEMENT);
        return true;
    }

    private static Map<ItemType, Integer> treasure() {
        Map<ItemType, Integer> loot = new java.util.LinkedHashMap<>();
        loot.put(ItemType.ENCHANTED_TITANIUM, 3);
        loot.put(ItemType.ENCHANTED_GOLD_INGOT, 7);
        loot.put(ItemType.ENCHANTED_DIAMOND, 3);
        loot.put(ItemType.ENCHANTED_MITHRIL, 10);
        return loot;
    }

    private static void say(SkyBlockPlayer player, List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = SPEAKER + lines.get(i);
            if (i == 0) player.sendMessage(line);
            else schedule(i * LINE_DELAY_TICKS, () -> player.sendMessage(line));
        }
    }

    private static void schedule(int ticks, Runnable task) {
        MinecraftServer.getSchedulerManager().buildTask(task).delay(TaskSchedule.tick(ticks)).schedule();
    }

    private static DatapointMapStringLong datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BEDNOM, DatapointMapStringLong.class);
    }

    private static long get(SkyBlockPlayer player, String key) {
        return datapoint(player).getValue().getOrDefault(key, 0L);
    }

    private static void put(SkyBlockPlayer player, String key, long value) {
        DatapointMapStringLong datapoint = datapoint(player);
        Map<String, Long> updated = new HashMap<>(datapoint.getValue());
        updated.put(key, value);
        datapoint.setValue(updated);
    }
}
