package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.auctions.AuctionAdminBypassProtocol;
import net.swofty.commons.protocol.objects.calendar.ResetCalendarProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointUUIDList;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.gui.inventories.GUIEnchantmentTable;
import net.swofty.type.skyblockgeneric.gui.inventories.auction.GUIAuctionHouse;
import net.swofty.type.skyblockgeneric.gui.inventories.banker.GUIBanker;
import net.swofty.type.skyblockgeneric.gui.inventories.bazaar.GUIBazaar;
import net.swofty.type.skyblockgeneric.server.attribute.SkyBlockServerAttributes;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "adminbypass jumptonextevent skiptoevent",
        description = "Skips the timer to the next occurrence of a SkyBlock event",
        usage = "/adminbypass <event>|auction [uuid]|bin_grace [uuid]|auctionhouse|bazaar|bank|etable|skiptospecialmayor|resetyear",
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
        command.addSyntax((sender, context) -> openCookieMenu(sender, "Auction House"), ArgumentType.Literal("auctionhouse"));
        command.addSyntax((sender, context) -> openCookieMenu(sender, "Bazaar"), ArgumentType.Literal("bazaar"));
        command.addSyntax((sender, context) -> openCookieMenu(sender, "Bank"), ArgumentType.Literal("bank"));
        command.addSyntax((sender, context) -> openCookieMenu(sender, "Enchantment Table"), ArgumentType.Literal("etable"));
        command.addSyntax((sender, context) -> forceElectSpecialMayor(sender), ArgumentType.Literal("skiptospecialmayor"));
        command.addSyntax((sender, context) -> resetYear(sender), ArgumentType.Literal("resetyear"));
    }

    private static void resetYear(net.minestom.server.command.CommandSender sender) {
        // A local-only reset would just get overwritten back up within a few seconds by
        // SkyBlockServerAttributes' cross-server "highest elapsed wins" reconciliation once any
        // other still-running server saves its own (higher) value again - so this resets locally
        // immediately, then broadcasts the same reset to every other connected game server so they
        // all land on Year 1 together instead of one racing the others back up.
        SkyBlockCalendar.resetToYearOne();
        SkyBlockServerAttributes.forceSave();
        sender.sendMessage("<a>Reset the SkyBlock Calendar to Year 1, Early Spring, Day 1.");

        RedisClient.requestAllServersFromService(new ResetCalendarProtocol(),
                new ResetCalendarProtocol.ResetMessage(), 5000)
            .thenAccept(responses -> sender.sendMessage(
                    Text.of("<7>Synced the reset across <f>{} <7>connected game server(s).", responses.size())))
            .exceptionally(error -> {
                sender.sendMessage("<c>Reset this server, but failed to sync the reset to other game servers.");
                return null;
            });
    }

    private static void forceElectSpecialMayor(net.minestom.server.command.CommandSender sender) {
        net.swofty.type.skyblockgeneric.elections.SkyBlockMayor elected =
                net.swofty.type.skyblockgeneric.elections.ElectionManager.forceElectSpecialMayor();
        if (elected == null) {
            sender.sendMessage("<c>No special mayors are registered.");
            return;
        }
        sender.sendMessage(Text.parseLenient("<a>Force-elected special mayor <f>" + elected.getDisplayName() + "<a>!"));
        sender.sendMessage(Text.parseLenient("<7>Active perks: <f>" + String.join("<7>, <f>",
                java.util.Arrays.stream(elected.getAllPerks())
                        .map(net.swofty.type.skyblockgeneric.elections.SkyBlockMayor.Perk::getDisplayName)
                        .toList())));
    }

    private static void openCookieMenu(net.minestom.server.command.CommandSender sender, String menu) {
        if (!(sender instanceof SkyBlockPlayer player)) {
            sender.sendMessage("<c>This admin bypass requires an in-game player.");
            return;
        }
        switch (menu) {
            case "Auction House" -> new GUIAuctionHouse().open(player);
            case "Bazaar" -> new GUIBazaar(BazaarCategories.FARMING).open(player);
            case "Bank" -> new GUIBanker().open(player);
            case "Enchantment Table" -> new GUIEnchantmentTable().open(player);
            default -> throw new IllegalArgumentException("Unknown Cookie-gated menu: " + menu);
        }
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
