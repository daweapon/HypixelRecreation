package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.auctions.AuctionAdminBypassProtocol;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "adminbypass jumptonextevent skiptoevent",
        description = "Skips the timer to the next occurrence of a SkyBlock event",
        usage = "/adminbypass <event>|auction [uuid]|bin_grace [uuid]",
        permission = Rank.STAFF,
        allowsConsole = true)
public class AdminBypassCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString eventArgument = new ArgumentString("event");
        eventArgument.setSuggestionCallback((sender, context, suggestion) -> {
            for (CalendarEvent event : CalendarEvent.getAllEvents()) {
                suggest(suggestion, event.id(), event.getDisplayName(SkyBlockCalendar.getYear()));
            }
        });

        command.addSyntax((sender, context) -> {
            CalendarEvent event = CalendarEvent.fromId(context.get(eventArgument));
            if (event == null) {
                sender.sendMessage("<c>Invalid timer. Available timers:");
                for (CalendarEvent available : CalendarEvent.getAllEvents()) {
                    sender.sendMessage(Text.of("<7>- <f>{} <8>({})", available.id(),
                            available.getDisplayName(SkyBlockCalendar.getYear())));
                }
                return;
            }

            long elapsed = SkyBlockCalendar.getElapsed();
            int year = SkyBlockCalendar.getYear();
            long timeInYear = elapsed % SkyBlockCalendar.YEAR;
            Long nextOccurrence = event.times().stream()
                    .filter(time -> time > timeInYear)
                    .findFirst()
                    .orElse(null);

            if (nextOccurrence == null && !event.times().isEmpty()) {
                nextOccurrence = event.times().getFirst();
                year++;
            }
            if (nextOccurrence == null) {
                sender.sendMessage("<c>That timer has no scheduled occurrence.");
                return;
            }

            long targetElapsed = ((long) (year - 1) * SkyBlockCalendar.YEAR) + nextOccurrence;
            SkyBlockCalendar.setElapsed(targetElapsed - 20);
            sender.sendMessage(Text.of("<a>Skipped the timer to {}!", event.getDisplayName(year)));
            sender.sendMessage(Text.of("<7>New date: {} {}, Year {}", SkyBlockCalendar.getMonthName(),
                    SkyBlockCalendar.getDay(), SkyBlockCalendar.getYear()));
        }, eventArgument);

        ArgumentUUID auctionId = ArgumentType.UUID("auction_id");
        command.addSyntax((sender, context) -> skipAuctionTimer(sender, context.get(auctionId),
                AuctionAdminBypassProtocol.Timer.AUCTION_END), ArgumentType.Literal("auction"), auctionId);
        command.addSyntax((sender, context) -> skipAuctionTimer(sender, context.get(auctionId),
                AuctionAdminBypassProtocol.Timer.BIN_GRACE), ArgumentType.Literal("bin_grace"), auctionId);
        command.addSyntax((sender, context) -> skipNewestOwnedAuction(sender,
                AuctionAdminBypassProtocol.Timer.AUCTION_END), ArgumentType.Literal("auction"));
        command.addSyntax((sender, context) -> skipNewestOwnedAuction(sender,
                AuctionAdminBypassProtocol.Timer.BIN_GRACE), ArgumentType.Literal("bin_grace"));
    }

    private static void skipNewestOwnedAuction(net.minestom.server.command.CommandSender sender,
                                                AuctionAdminBypassProtocol.Timer timer) {
        if (!(sender instanceof SkyBlockPlayer player)) {
            sender.sendMessage("<c>Console use requires an auction UUID.");
            return;
        }
        var owned = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.AUCTION_ACTIVE_OWNED,
                DatapointUUIDList.class).getValue();
        if (owned.isEmpty()) {
            sender.sendMessage("<c>You do not have an active auction to target.");
            return;
        }
        skipAuctionTimer(sender, owned.getLast(), timer);
    }

    private static void skipAuctionTimer(net.minestom.server.command.CommandSender sender, java.util.UUID auctionId,
                                         AuctionAdminBypassProtocol.Timer timer) {
        new ProxyService(ServiceType.AUCTION_HOUSE).handleRequest(new AuctionAdminBypassProtocol(),
                new AuctionAdminBypassProtocol.Request(auctionId, timer)).thenAccept(response -> {
            if (!response.success()) {
                sender.sendMessage(Text.of("<c>{}", response.error()));
                return;
            }
            String timerName = timer == AuctionAdminBypassProtocol.Timer.AUCTION_END
                    ? "auction end" : "BIN purchase grace";
            sender.sendMessage(Text.of("<a>Skipped the {} timer for auction <f>{}<a>.", timerName, auctionId));
        }).exceptionally(error -> {
            sender.sendMessage("<c>The Auction House service did not accept the timer change.");
            return null;
        });
    }
}
