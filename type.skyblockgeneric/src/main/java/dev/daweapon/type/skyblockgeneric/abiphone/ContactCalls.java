package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ContactCalls {
    public static final long SIRIUS_WARNING_TICKS = 2 * SkyBlockCalendar.HOUR;
    private static final String VINCENT_YEAR = "vincent:year:";
    private static final String ST_JERRY_GIFT = "st_jerry:gift:";

    private ContactCalls() {
    }

    public static void registerHooks() {
        AbiphoneIncomingHooks.register("sirius", new AbiphoneIncomingHooks.Hook(ContactCalls::darkAuctionSoon, ContactCalls::siriusReminder));
        AbiphoneContactListeners.onAdded("vincent", ContactCalls::markVincentYear);
    }

    public static boolean darkAuctionSoon(SkyBlockPlayer player) {
        long until = SkyBlockCalendar.ticksUntilEvent(CalendarEvent.DARK_AUCTION);
        return until > 0 && until <= SIRIUS_WARNING_TICKS;
    }

    private static void siriusReminder(SkyBlockPlayer player) {
        AbiphoneIncomingCalls.speak(player, "sirius",
                "Hello. This is an automated message being sent to all of my closest contacts.",
                "The next Dark Auction will be taking place very soon. Same time, same place as usual.",
                "If you are interested in making a purchase, please begin to make your way over.");
    }

    public static void sirius(HypixelPlayer player) {
        player.sendMessage("<c>✆ No answer. He's a busy man, after all.");
    }

    public static void stJerry(HypixelPlayer caller) {
        if (!(caller instanceof SkyBlockPlayer player)) return;
        String key = ST_JERRY_GIFT + SkyBlockCalendar.getYear();
        if (AbiphoneCallMemory.has(player, key)) {
            AbiphoneIncomingCalls.speak(player, "st_jerry",
                    "You've already recieved a gift this year!",
                    "But hey, merry season of the Jerry to you anyway!",
                    "Call me again next year!");
            return;
        }
        AbiphoneCallMemory.removeIf(player, entry -> entry.startsWith(ST_JERRY_GIFT));
        AbiphoneCallMemory.add(player, key);
        player.addAndUpdateItem(ItemType.WHITE_GIFT, 1);
        AbiphoneIncomingCalls.speak(player, "st_jerry", "I sent a gift to your inventory! With love, St. Jerry.");
    }

    public static void professorWynd(HypixelPlayer caller) {
        if (!(caller instanceof SkyBlockPlayer player)) return;
        switch (ThreadLocalRandom.current().nextInt(3)) {
            case 0 -> AbiphoneIncomingCalls.speak(player, "professor_wynd", "Did you see that storm last night? Thought my roof was going to collapse!");
            case 1 -> {
                player.sendMessage("<e>[NPC] Sunny<f>: <b>✆ <f>Meow!");
                AbiphoneIncomingCalls.speak(player, "professor_wynd", "Oops, sorry about that! My kitty got to the phone before I could. How may I help?");
            }
            default -> AbiphoneIncomingCalls.speak(player, "professor_wynd", "What can I help you with today?");
        }
    }

    public static boolean vincentHasNews(SkyBlockPlayer player) {
        return !AbiphoneCallMemory.has(player, VINCENT_YEAR + SkyBlockCalendar.getYear());
    }

    public static void vincentNewYear(SkyBlockPlayer player, List<String> dyeNames) {
        markVincentYear(player);
        int year = SkyBlockCalendar.getYear();
        String[] lines = new String[3 + dyeNames.size()];
        lines[0] = "Hello <8>" + player.getUsername() + "<f>! <b>Vincent <f>here! Happy new year!";
        lines[1] = "I just wanted to let you know that I've had a strike of inspiration and have started working on my latest masterpiece!";
        lines[2] = "Here are the <c>d<6>y<e>e<a>s <f>that I will be using throughout <d>SkyBlock Year " + year + "<f>!";
        for (int i = 0; i < dyeNames.size(); i++) lines[3 + i] = dyeNames.get(i);
        AbiphoneIncomingCalls.speak(player, "vincent", lines);
    }

    private static void markVincentYear(SkyBlockPlayer player) {
        AbiphoneCallMemory.removeIf(player, entry -> entry.startsWith(VINCENT_YEAR));
        AbiphoneCallMemory.add(player, VINCENT_YEAR + SkyBlockCalendar.getYear());
    }
}
