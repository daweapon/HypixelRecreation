package dev.daweapon.type.hub.npcs;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.century.raffle.CenturyEvent;
import dev.daweapon.type.skyblockgeneric.century.raffle.CenturyRaffle;
import dev.daweapon.type.skyblockgeneric.century.raffle.CenturyTeam;
import dev.daweapon.type.skyblockgeneric.century.raffle.GUIRaffleTeamAssignment;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public class NPCRezzus extends HypixelNPC {

    public NPCRezzus() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<a>Rezzus", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "sBIWVRsarJdZUR/0k9LVGCSUD63y3WyJ7oFNUoicZ4AeEev7NTrsB49zZm/bF25830r4W44MTHzmaH22K76JNFiNrh2SnLV/HTgUJeYlA68EzQVSJUvAYZReZh/sVHczcMBCitCl9jfbIMtdLXumjJEuSnG+iSNq85+IckGW6ADuB/gmanuel9qjTWIo6h6MXm84C2kKnLuRSiqZlr/S9YB9KAOtliX13C+oDSicytx6Cq1GrEbo4UnrOUMb9QWl03WUk3RxGkjCr6QVPi+Q5AO6oH/YgZEF+cJICyxF/m1313WZxmFoWboV9T10OEMXtXAHbIF/lOO8ZIJtAC87jNFkFMwSIEJTN1nqJmSL4qwzizESFkJhV8XdNH7+R4O9iO7h5ZvGEolTBpFVGisDizciWaLSrWxN8owmiXLJa9n2SMPSLQYSBishN7lzO17k1+Rc3ry6IWXqAaXCMrCQ5eSd5yowkA9QB6iSPZ2V6dRKa3f7YZQFrUy9nCAwud6IS36EbRVXqYzXJrw+00veq91QjUANaaGEYUVB2kz6uXaFM6VG0AQQg4DQGJ8Op4Fiuhr10mUBlBt085char/U+djclit2Ku6w8jjy+p7LTpbZl5K8wGRU//IE4hR3Ba7G7ZXAUQyUwMpVcVKZZg/jrY9tQmryeQaeMTXw59UFymw=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc5Mzc4NDgwNTg0MiwKICAicHJvZmlsZUlkIiA6ICI5YjJhMzBlY2Y4YjM0ZGZlYmY0OTljNWMzNjczODNmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZXp6dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjZiZmQ5Y2QzZTE2OWRhZWZhOGQ3MzkxMmU0MWQyZWMxZGY2NmJlMDFmMzZjMjEyOTJlNzM4MTRlN2M1NjNhOSIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-11.5, 76, 3.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return CenturyEvent.latest() != null;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!CenturyEvent.isActive()) {
            setDialogue(player, CenturyEvent.latest() == null ? "upcoming" : "over");
            return;
        }
        CenturyRaffle.load(player).thenAccept(profile -> CenturyRaffle.sync(() -> {
            if (!player.isOnline()) return;
            if (profile != null && profile.team() != null) {
                setDialogue(player, "again").thenRun(() -> sendOptions(player));
                return;
            }
            setDialogue(player, "intro").thenRun(() -> player.openView(
                    new GUIRaffleTeamAssignment((joined, team) -> setDialogue(joined, "joined"),
                            () -> setDialogue(player, "abandoned")),
                    new GUIRaffleTeamAssignment.State(false)));
        }));
    }

    private void sendOptions(SkyBlockPlayer player) {
        NPCOption.sendOption(player, "rezzus", true, List.of(
                new NPCOption.Option("how", NamedTextColor.AQUA, false, "How do I enter the raffles?", _ -> setDialogue(player, "how")),
                new NPCOption.Option("team", NamedTextColor.GOLD, false, "Which team am I on?", _ -> setDialogue(player, "team"))));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        int year = CenturyEvent.currentYear();
        int century = year % CenturyEvent.CENTURY == 0 ? year : (year / CenturyEvent.CENTURY + 1) * CenturyEvent.CENTURY;
        CenturyTeam team = player instanceof SkyBlockPlayer skyBlockPlayer ? CenturyRaffle.team(skyBlockPlayer) : null;
        String teamName = team == null ? "<7><l>NO" : team.display();
        return new DialogueSet[]{
                DialogueSet.builder().key("upcoming").lines(
                        "Hey there! Happy (upcoming) <b>SkyBlock Year " + century + "<f>!",
                        "As part of the <d>Century Celebration<f>, we're organizing a <c><l>huge <r><f>raffle!",
                        "Come talk to me once the event starts!").build(),
                DialogueSet.builder().key("over").lines(
                        "Hi!",
                        "The <d>Century Celebration <f>event is over, but you can still interact with the <6>Raffle Box <f>to claim rewards you might have missed.",
                        "See you next time!").build(),
                DialogueSet.builder().key("intro").lines(
                        "Hey there! Happy <b>SkyBlock Year " + century + "<f>!",
                        "To celebrate this <5><l>EPIC <r><f>milestone, we are organizing a set of <d><l><o>CRAZY <r><f>raffles!",
                        "But first, you need a <b>TEAM<f>!").build(),
                DialogueSet.builder().key("abandoned").lines(
                        "Oh... did you mean to close that?",
                        "It's fine, click me again!").build(),
                DialogueSet.builder().key("joined").lines(
                        "Sweet, the " + teamName + " <r><f>team.",
                        "I hear it might be the best one!",
                        "Anyway... there are <6>3 <f>kinds of raffles going on.",
                        "<6>Speed Raffles <f>are drawn every <9>2 <f>hours.",
                        "<b>Daily Raffles<f>, they happen every <6>24 <f>hours.",
                        "<c><l>The Big One<r><f>, which is going to be drawn at the end of <b>Year " + century + "<f>!",
                        "To participate, collect <b>Raffle Tickets <f>and <d>slices of cake <f>by simply playing <a>SkyBlock <f>during the event.",
                        "All tickets you gain are automatically entered into the next raffle of each type.",
                        "When you receive a <d>slice of cake<f>, it will be a certain <c>c<6>o<e>l<a>o<9>r<f>, which corresponds to a certain team.",
                        "There are slices of <c>Red Velvet<f>, <9>Blueberry<f>, <e>Lemon Cheesecake<f>, <a>Matcha<f>, and <d>Strawberry Shortcake<f>!",
                        "If you take your slice of cake and feed it to a member of that team, you'll both get some <6>rewards<f>, in addition to <9>bonus Raffle Tickets<f>!",
                        "You can get more tickets by eating cake and completing <b>Raffle Tasks<f>. Check out the <6>Raffle Box <f>for more info on those!",
                        "You can click on the <6>Raffle Box <f>next to me or use <a>/century <f>to view your event progress.",
                        "Oh, one last thing! The team that enters the most tickets into <c><l>The Big One <r><f>will receive a special <c>memento <f>at the end of the event!",
                        "Have fun! You can come speak to me again if you have any questions!").build(),
                DialogueSet.builder().key("again").lines(
                        "Hello again! <d>Happy Century Celebration<f>!",
                        "What would you like to know about the event?").build(),
                DialogueSet.builder().key("how").lines(
                        "To enter a <6>raffle<f>, you need to collect <b>Raffle Tickets<f>.",
                        "You will gain tickets just for <e>being online <f>during the event.",
                        "However, you will gain a bit more if you're actively playing, especially from doing <b>Raffle Tasks<f>.",
                        "Tickets are not physical items, so don't worry about losing them!",
                        "Each time you find a <b>ticket<f>, it will be entered into <l>EVERY<r><f> upcoming raffle.").build(),
                DialogueSet.builder().key("team").lines(
                        "For this event, you are on the " + teamName + " <r><f>team!",
                        "You can tell what team someone is by looking at the color of the cake emblem next to their name in chat.",
                        "Or, by looking at the color of the cake emblem next to their name in the TAB list.",
                        "If they don't have a cake emblem, then they haven't gotten involved in the event yet...",
                        "Which means more cake for the rest of us!").build()
        };
    }
}
