package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.party.FullParty;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CommandParameters(labels = "p party",
        description = "Party management commands",
        usage = "/party <subcommand>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
// Command exposing party management subcommands, proxied to the party service.
public class PartyCommand extends HypixelCommand {
    // A plain (non-thread-safe) collection here previously meant any uncaught exception in a
    // syntax callback below would leave a player's UUID stuck forever, silently no-oping every
    // future /party command for them (the remove() call never ran). Set + try/finally fixes both
    // the concurrency hazard and the stuck-forever failure mode.
    public Set<UUID> pendingCommands = ConcurrentHashMap.newKeySet();

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString subcommand = ArgumentType.String("subcommand");
        ArgumentString playerName = ArgumentType.String("player");
        ProxyService partyService = new ProxyService(ServiceType.PARTY);

        command.addSyntax((sender, context) -> {
            showHelp((HypixelPlayer) sender);
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            if (pendingCommands.contains(player.getUuid())) {
                return;
            }

            pendingCommands.add(player.getUuid());
            try {
            if (!partyService.isOnline().join()) {
                player.sendMessage(Text.key("commands.common.service_offline_party"));
                return;
            }

            String sub = context.get(subcommand);

            switch (sub.toLowerCase()) {
                case "list" -> {
                    if (!PartyManager.isInParty(player)) {
                        player.sendMessage(Text.key("commands.common.separator"));
                        player.sendMessage(Text.key("commands.party.not_in_party"));
                        player.sendMessage(Text.key("commands.common.separator"));
                        return;
                    }
                    FullParty party = PartyManager.getPartyFromPlayer(player);

                    int partySize = party.getMembers().size();
                    player.sendMessage(Text.key("commands.common.separator"));
                    player.sendMessage(Text.key("commands.party.list_header", partySize));
                    player.sendMessage(Text.key("commands.common.empty_line"));

                    FullParty.Member leader = party.getLeader();
                    player.sendMessage(Text.key("commands.party.list_leader", HypixelPlayer.getDisplayName(leader.getUuid())));

                    boolean hasMods = false;
                    for (FullParty.Member member : party.getMembers()) {
                        if (member.getRole() == FullParty.Role.MODERATOR) {
                            hasMods = true;
                            break;
                        }
                    }

                    if (hasMods) {
                        player.sendMessage(Text.key("commands.common.empty_line"));
                        Text modList = Text.join(Text.of(", "), party.getMembers().stream()
                                .filter(member -> member.getRole() == FullParty.Role.MODERATOR)
                                .map(member -> HypixelPlayer.getDisplayName(member.getUuid()))
                                .toList());
                        player.sendMessage(Text.key("commands.party.list_moderators", modList));
                    }

                    boolean hasMembers = false;
                    for (FullParty.Member member : party.getMembers()) {
                        if (member.getRole() != FullParty.Role.LEADER && member.getRole() != FullParty.Role.MODERATOR) {
                            hasMembers = true;
                            break;
                        }
                    }

                    Text memberList = Text.join(Text.of(", "), party.getMembers().stream()
                            .filter(member -> member.getRole() != FullParty.Role.LEADER && member.getRole() != FullParty.Role.MODERATOR)
                            .map(member -> HypixelPlayer.getDisplayName(member.getUuid()))
                            .toList());
                    if (hasMembers) {
                        player.sendMessage(Text.key("commands.common.empty_line"));
                        player.sendMessage(Text.key("commands.party.list_members", memberList));
                    }
                    player.sendMessage(Text.key("commands.common.separator"));
                }
                case "leave" -> PartyManager.leaveParty(player);
                case "disband" -> PartyManager.disbandParty(player);
                case "warp" -> PartyManager.warpParty(player);
                case "mute" -> PartyManager.toggleMute(player);
                case "kickoffline" -> PartyManager.kickOfflineMembers(player);
                default -> PartyManager.invitePlayer(player, sub);
            }
            } catch (Throwable t) {
                org.tinylog.Logger.error(t, "[PARTY-TRACE] 1-arg /p syntax threw for {}", player.getUsername());
            } finally {
                pendingCommands.remove(player.getUuid());
            }
        }, subcommand);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            if (pendingCommands.contains(player.getUuid())) {
                return;
            }

            pendingCommands.add(player.getUuid());
            try {
            if (!partyService.isOnline().join()) {
                player.sendMessage(Text.key("commands.common.service_offline_party"));
                return;
            }

            String sub = context.get(subcommand);
            String target = context.get(playerName);

            switch (sub.toLowerCase()) {
                case "invite" -> PartyManager.invitePlayer(player, target);
                case "accept" -> PartyManager.acceptInvite(player, target);
                case "kick" -> PartyManager.kickPlayer(player, target);
                case "transfer" -> PartyManager.transferLeadership(player, target);
                case "promote" -> PartyManager.promotePlayer(player, target);
                case "demote" -> PartyManager.demotePlayer(player, target);
                case "hijack" -> PartyManager.hijackParty(player, target);
                case "chat" -> PartyManager.sendChat(player, target);
                case "settings" -> {
                    switch (target.toLowerCase()) {
                        case "allinvite" -> PartyManager.toggleAllInvite(player);
                        default -> player.sendMessage(Text.key("commands.common.unknown_command_use_help", "party"));
                    }
                }
                case "movetoserver" -> {
                    UUID targetServer = UUID.fromString(target);
                    player.asProxyPlayer().transferToWithIndication(targetServer);
                }
                default -> player.sendMessage(Text.key("commands.common.unknown_command_use_help", "party"));
            }
            } catch (Throwable t) {
                org.tinylog.Logger.error(t, "[PARTY-TRACE] 2-arg /p syntax threw for {}", player.getUsername());
            } finally {
                pendingCommands.remove(player.getUuid());
            }
        }, subcommand, playerName);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            if (pendingCommands.contains(player.getUuid())) {
                return;
            }

            pendingCommands.add(player.getUuid());
            try {
            if (!partyService.isOnline().join()) {
                player.sendMessage(Text.key("commands.common.service_offline_party_alt"));
                return;
            }

            String sub = context.get(subcommand);
            String message = String.join(" ", context.get(new ArgumentStringArray("message")));

            switch (sub.toLowerCase()) {
                case "chat" -> PartyManager.sendChat(player, message);
                default -> player.sendMessage(Text.key("commands.common.unknown_command_use_help", "party"));
            }
            } finally {
                pendingCommands.remove(player.getUuid());
            }
        }, subcommand, new ArgumentStringArray("message"));

        ArgumentString settingArg = ArgumentType.String("setting");
        ArgumentString valueArg = ArgumentType.String("value");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            if (pendingCommands.contains(player.getUuid())) {
                return;
            }

            pendingCommands.add(player.getUuid());
            try {
            if (!partyService.isOnline().join()) {
                player.sendMessage(Text.key("commands.common.service_offline_party"));
                return;
            }

            String sub = context.get(subcommand);
            String setting = context.get(settingArg);
            String value = context.get(valueArg);

            if (sub.equalsIgnoreCase("settings") && setting.equalsIgnoreCase("size")) {
                PartyManager.changeSize(player, value);
            } else {
                player.sendMessage(Text.key("commands.common.unknown_command_use_help", "party"));
            }
            } finally {
                pendingCommands.remove(player.getUuid());
            }
        }, subcommand, settingArg, valueArg);
    }

    private void showHelp(HypixelPlayer player) {
        player.sendMessage(Text.key("commands.common.separator"));
        player.sendMessage(Text.key("commands.party.help_header"));
        player.sendMessage(Text.key("commands.party.help_accept"));
        player.sendMessage(Text.key("commands.party.help_invite"));
        player.sendMessage(Text.key("commands.party.help_list"));
        player.sendMessage(Text.key("commands.party.help_leave"));
        player.sendMessage(Text.key("commands.party.help_warp"));
        player.sendMessage(Text.key("commands.party.help_disband"));
        player.sendMessage(Text.key("commands.party.help_transfer"));
        player.sendMessage(Text.key("commands.party.help_kick"));
        player.sendMessage(Text.key("commands.party.help_promote"));
        player.sendMessage(Text.key("commands.party.help_demote"));
        player.sendMessage(Text.key("commands.party.help_chat"));
        player.sendMessage(Text.key("commands.party.help_settings_size"));
        player.sendMessage(Text.key("commands.party.help_settings_allinvite"));
        player.sendMessage(Text.key("commands.party.help_mute"));
        player.sendMessage(Text.key("commands.party.help_kickoffline"));
        if (player.getRank().isEqualOrHigherThan(Rank.STAFF)) {
            player.sendMessage(Text.key("commands.party.help_hijack"));
        }
        player.sendMessage(Text.key("commands.common.separator"));
    }
}
