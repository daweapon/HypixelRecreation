package dev.daweapon.type.generic.chat;

import dev.daweapon.type.generic.data.datapoints.DatapointChatType;
import dev.daweapon.type.generic.party.PartyManager;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.Optional;

public final class ChatChannelRouter {
    private static final String NON_ASCII = "[^\\x00-\\x7F]";

    public enum Route {
        STAFF_SEND,
        STAFF_DENIED,
        PARTY_SEND,
        PARTY_DENIED,
        BROADCAST
    }

    private ChatChannelRouter() {
    }

    public static Optional<String> route(HypixelPlayer player, String rawMessage) {
        boolean staff = player.getRank().isStaff();
        String message = sanitize(rawMessage, staff);
        DatapointChatType.Chats chatType = player.getChatType().currentChatType;

        switch (decide(staff, chatType, chatType == DatapointChatType.Chats.PARTY && PartyManager.isInParty(player))) {
            case STAFF_DENIED -> {
                player.sendMessage("<c>Unknown chat type.");
                player.getChatType().switchTo(DatapointChatType.Chats.ALL);
                return Optional.empty();
            }
            case STAFF_SEND -> {
                StaffChat.sendMessage(player, message);
                return Optional.empty();
            }
            case PARTY_DENIED -> {
                player.sendMessage("<c>You are not in a party and were moved to the ALL channel.");
                player.getChatType().switchTo(DatapointChatType.Chats.ALL);
                return Optional.empty();
            }
            case PARTY_SEND -> {
                PartyManager.sendChat(player, message);
                return Optional.empty();
            }
            default -> {
                return Optional.of(message);
            }
        }
    }

    public static String sanitize(String message, boolean staff) {
        return staff ? message : message.replaceAll(NON_ASCII, "");
    }

    public static Route decide(boolean staff, DatapointChatType.Chats chatType, boolean inParty) {
        return switch (chatType) {
            case STAFF -> staff ? Route.STAFF_SEND : Route.STAFF_DENIED;
            case PARTY -> inParty ? Route.PARTY_SEND : Route.PARTY_DENIED;
            case ALL -> Route.BROADCAST;
        };
    }
}
