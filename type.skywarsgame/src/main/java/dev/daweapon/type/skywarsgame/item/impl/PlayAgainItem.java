package dev.daweapon.type.skywarsgame.item.impl;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.protocol.objects.orchestrator.ChooseGameProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.GetServerForMapProtocol;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;
import dev.daweapon.type.skywarsgame.item.SimpleInteractableItem;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;

public class PlayAgainItem extends SimpleInteractableItem {
    private static final ProxyService PROXY_SERVICE = new ProxyService(ServiceType.ORCHESTRATOR);

    public PlayAgainItem() {
        super("play_again");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.item(Material.PAPER, 1, """
                <a>Play Again <7>(Right Click)
                <7>Right-click to queue for another game!""").build();
    }

    @Override
    public void onItemUse(PlayerUseItemEvent event) {
        SkywarsPlayer player = (SkywarsPlayer) event.getPlayer();
        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game != null) {
            String gameType = game.getGameType().name();
            game.leave(player);

            GetServerForMapProtocol.GetServerForMapMessage message =
                new GetServerForMapProtocol.GetServerForMapMessage(
                    ServerType.SKYWARS_GAME,
                    null,
                    gameType,
                    1
                );

            PROXY_SERVICE.handleRequest(message).thenAccept(response -> {
                if (response instanceof GetServerForMapProtocol.GetServerForMapResponse resp) {
                    if (resp.server() != null) {
                        ChooseGameProtocol.ChooseGameMessage chooseMessage =
                            new ChooseGameProtocol.ChooseGameMessage(
                                player.getUuid(),
                                resp.server(),
                                resp.gameId()
                            );

                        PROXY_SERVICE.handleRequest(chooseMessage).thenRun(() -> {
                            player.asProxyPlayer().transferToWithIndication(resp.server().uuid());
                        });
                    }
                }
            });
        }
    }
}
