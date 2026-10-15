package dev.daweapon.type.generic.friend;

import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.friend.FriendData;
import dev.daweapon.commons.friend.FriendEvent;
import dev.daweapon.commons.friend.FriendSettingType;
import dev.daweapon.commons.friend.PendingFriendRequest;
import dev.daweapon.commons.friend.events.*;
import dev.daweapon.commons.presence.PresenceInfo;
import dev.daweapon.commons.protocol.objects.friend.AreFriendsProtocol;
import dev.daweapon.commons.protocol.objects.friend.GetFriendDataProtocol;
import dev.daweapon.commons.protocol.objects.friend.GetPendingFriendRequestsProtocol;
import dev.daweapon.commons.protocol.objects.friend.SendFriendEventToServiceProtocol;
import dev.daweapon.commons.protocol.objects.presence.GetPresenceBulkProtocol;
import dev.daweapon.commons.text.Text;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FriendManager {
    private static final ProxyService friendService = new ProxyService(ServiceType.FRIEND);

    public static boolean areFriends(HypixelPlayer player, UUID targetUuid) {
        if (!friendService.isOnline().join()) return false;
        AreFriendsProtocol.AreFriendsMessage message = new AreFriendsProtocol.AreFriendsMessage(player.getUuid(), targetUuid);
        return friendService.<AreFriendsProtocol.AreFriendsMessage,
                        AreFriendsProtocol.AreFriendsResponse>handleRequest(message)
                .thenApply(AreFriendsProtocol.AreFriendsResponse::areFriends)
                .join();
    }

    public static @Nullable FriendData getFriendData(HypixelPlayer player) {
        if (!friendService.isOnline().join()) return null;
        GetFriendDataProtocol.GetFriendDataMessage message = new GetFriendDataProtocol.GetFriendDataMessage(player.getUuid());
        return friendService.<GetFriendDataProtocol.GetFriendDataMessage,
                        GetFriendDataProtocol.GetFriendDataResponse>handleRequest(message)
                .thenApply(GetFriendDataProtocol.GetFriendDataResponse::friendData)
                .join();
    }

    public static List<PendingFriendRequest> getPendingRequests(HypixelPlayer player) {
        if (!friendService.isOnline().join()) return List.of();
        GetPendingFriendRequestsProtocol.GetPendingRequestsMessage message =
                new GetPendingFriendRequestsProtocol.GetPendingRequestsMessage(player.getUuid());
        return friendService.<GetPendingFriendRequestsProtocol.GetPendingRequestsMessage,
                        GetPendingFriendRequestsProtocol.GetPendingRequestsResponse>handleRequest(message)
                .thenApply(GetPendingFriendRequestsProtocol.GetPendingRequestsResponse::requests)
                .join();
    }

    public static List<PresenceInfo> getPresenceBulk(List<UUID> uuids) {
        if (!friendService.isOnline().join()) return List.of();
        if (uuids.isEmpty()) return List.of();
        GetPresenceBulkProtocol.GetPresenceBulkMessage message =
                new GetPresenceBulkProtocol.GetPresenceBulkMessage(uuids);
        return friendService.<GetPresenceBulkProtocol.GetPresenceBulkMessage,
                        GetPresenceBulkProtocol.GetPresenceBulkResponse>handleRequest(message)
                .thenApply(GetPresenceBulkProtocol.GetPresenceBulkResponse::presence)
                .join();
    }

    public static void addFriend(HypixelPlayer player, String targetName) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(player, "Couldn't find a player with that name!");
            return;
        }

        if (targetUUID.equals(player.getUuid())) {
            sendError(player, "You cannot add yourself as a friend!");
            return;
        }

        FriendAddRequestEvent event = new FriendAddRequestEvent(player.getUuid(), targetUUID);
        sendEventToService(event);
    }

    public static void acceptRequest(HypixelPlayer player, String senderName) {
        @Nullable UUID senderUUID = HypixelDataHandler.getPotentialUUIDFromName(senderName);
        if (senderUUID == null) {
            sendError(player, "Couldn't find a player with that name!");
            return;
        }

        FriendAcceptRequestEvent event = new FriendAcceptRequestEvent(player.getUuid(), senderUUID);
        sendEventToService(event);
    }

    public static void denyRequest(HypixelPlayer player, String senderName) {
        @Nullable UUID senderUUID = HypixelDataHandler.getPotentialUUIDFromName(senderName);
        if (senderUUID == null) {
            sendError(player, "Couldn't find a player with that name!");
            return;
        }

        FriendDenyRequestEvent event = new FriendDenyRequestEvent(player.getUuid(), senderUUID);
        sendEventToService(event);
    }

    public static void removeFriend(HypixelPlayer player, String targetName) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(player, "Couldn't find a player with that name!");
            return;
        }

        FriendRemoveRequestEvent event = new FriendRemoveRequestEvent(player.getUuid(), targetUUID);
        sendEventToService(event);
    }

    public static void removeAllFriends(HypixelPlayer player) {
        FriendRemoveAllRequestEvent event = new FriendRemoveAllRequestEvent(player.getUuid());
        sendEventToService(event);
    }

    public static void toggleBestFriend(HypixelPlayer player, String targetName) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(player, "Couldn't find a player with that name!");
            return;
        }

        FriendToggleBestRequestEvent event = new FriendToggleBestRequestEvent(player.getUuid(), targetUUID);
        sendEventToService(event);
    }

    public static void setNickname(HypixelPlayer player, String targetName, String nickname) {
        @Nullable UUID targetUUID = HypixelDataHandler.getPotentialUUIDFromName(targetName);
        if (targetUUID == null) {
            sendError(player, "Couldn't find a player with that name!");
            return;
        }

        FriendSetNicknameRequestEvent event = new FriendSetNicknameRequestEvent(player.getUuid(), targetUUID, nickname);
        sendEventToService(event);
    }

    public static void toggleNotifications(HypixelPlayer player) {
        FriendToggleSettingRequestEvent event = new FriendToggleSettingRequestEvent(player.getUuid(), FriendSettingType.JOIN_LEAVE_NOTIFICATIONS);
        sendEventToService(event);
    }

    public static void toggleRequests(HypixelPlayer player) {
        FriendToggleSettingRequestEvent event = new FriendToggleSettingRequestEvent(player.getUuid(), FriendSettingType.ACCEPTING_REQUESTS);
        sendEventToService(event);
    }

    public static void listFriends(HypixelPlayer player, int page, boolean bestOnly) {
        FriendListRequestEvent event = new FriendListRequestEvent(player.getUuid(), page, bestOnly);
        sendEventToService(event);
    }

    public static void listRequests(HypixelPlayer player, int page) {
        FriendRequestsListEvent event = new FriendRequestsListEvent(player.getUuid(), page);
        sendEventToService(event);
    }

    private static void sendEventToService(FriendEvent event) {
        var message = new SendFriendEventToServiceProtocol.SendFriendEventToServiceMessage(event);
        friendService.handleRequest(message);
    }

    private static void sendError(HypixelPlayer player, String message) {
        player.sendMessage("<sep>");
        player.sendMessage("<c>{}", message);
        player.sendMessage("<sep>");
    }

    private static void sendSuccess(HypixelPlayer player, String message) {
        player.sendMessage("<sep>");
        player.sendMessage(message);
        player.sendMessage("<sep>");
    }
}
