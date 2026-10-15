package dev.daweapon.type.murdermysterygame.item.impl;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.protocol.objects.orchestrator.ChooseGameProtocol;
import dev.daweapon.commons.protocol.objects.orchestrator.GetServerForMapProtocol;
import dev.daweapon.proxyapi.ProxyService;
import dev.daweapon.type.murdermysterygame.TypeMurderMysteryGameLoader;
import dev.daweapon.type.murdermysterygame.game.Game;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;
import dev.daweapon.type.murdermysterygame.item.SimpleInteractableItem;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;

public class PlayAgainItem extends SimpleInteractableItem {
    private static final ProxyService PROXY_SERVICE = new ProxyService(ServiceType.ORCHESTRATOR);

    public PlayAgainItem() {
        super("play_again");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.item(Material.PAPER, """
                <a>Play Again <7>(Right Click)
                <7>Right-click to queue for another game!""").build();
    }

    @Override
    public void onItemUse(PlayerUseItemEvent event) {
        MurderMysteryPlayer player = (MurderMysteryPlayer) event.getPlayer();
        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game != null) {
            String gameType = game.getGameType().toString();
            game.leave(player);

            // Queue for new game using ProxyService directly
            GetServerForMapProtocol.GetServerForMapMessage message =
                new GetServerForMapProtocol.GetServerForMapMessage(
                    ServerType.MURDER_MYSTERY_GAME,
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
