package dev.daweapon.type.generic.redis.service;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.gui.KickFromGUIPushProtocol;
import dev.daweapon.commons.protocol.objects.gui.KickFromGUIPushProtocol.Request;
import dev.daweapon.commons.protocol.objects.gui.KickFromGUIPushProtocol.Response;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import dev.daweapon.commons.redis.RedisMessageContext;

public class KickFromGUIHandler implements RedisMessageHandler<Request, Response> {

    private static final KickFromGUIPushProtocol PROTOCOL = new KickFromGUIPushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request message, RedisMessageContext context) {
        List<UUID> kickedPlayers = new ArrayList<>();

        for (UUID playerUUID : message.playerUUIDs()) {
            HypixelPlayer player = HypixelGenericLoader.getFromUUID(playerUUID);

            if (player != null && HypixelInventoryGUI.GUI_MAP.containsKey(playerUUID)) {
                HypixelInventoryGUI gui = HypixelInventoryGUI.GUI_MAP.get(playerUUID);

                if (gui.getClass().getSimpleName().toLowerCase().contains(message.guiType().toLowerCase())) {
                    player.closeInventory();
                    player.sendMessage("<c>You have been kicked from the {} GUI due to a data synchronization operation.", message.guiType());
                    kickedPlayers.add(playerUUID);
                }
            }
        }

        return new Response(true, kickedPlayers, kickedPlayers.size(), null);
    }
}
