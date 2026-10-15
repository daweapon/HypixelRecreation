package dev.daweapon.type.bedwarsgame.gui;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsTeam;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.bedwarsgame.util.BedWarsInventoryManipulator;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// GUI for purchasing a compass tracker targeting a specific enemy team.
public class GUIPurchaseEnemyTracker extends StatelessView {

    private static final int TRACKER_PRICE = 2;
    private static final int[] TEAM_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Purchase Enemy Tracker", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.back(layout, 31, ctx);

        if (!(ctx.player() instanceof BedWarsPlayer player)) {
            return;
        }

        BedWarsGame game = player.getGame();
        TeamKey ownTeam = GUIQuickCommunications.resolveTeamKey(player);
        if (game == null || ownTeam == null) {
            layout.slot(13, ItemStacks.item(Material.BARRIER, """
                <c>Unavailable
                <7>You must be in an active game
                <7>to purchase an enemy tracker."""));
            return;
        }

        List<TeamKey> enemyTeams = getEnemyTeams(game, ownTeam);
        if (enemyTeams.isEmpty()) {
            layout.slot(13, ItemStacks.item(Material.LIME_STAINED_GLASS_PANE, """
                <a>No enemies left
                <7>There are no enemy teams
                <7>left to track."""));
            return;
        }

        boolean trackersUnlocked = enemyTeams.stream().noneMatch(game::isBedAlive);
        int optionCount = Math.min(enemyTeams.size(), TEAM_SLOTS.length);
        for (int i = 0; i < optionCount; i++) {
            TeamKey targetTeam = enemyTeams.get(i);
            int slot = TEAM_SLOTS[i];

            layout.slot(slot,
                (s, c) -> buildTrackerItem(player, targetTeam, trackersUnlocked),
                (click, context) -> {
                    if (!(click.player() instanceof BedWarsPlayer clickPlayer)) {
                        return;
                    }

                    handleTrackerPurchase(clickPlayer, game, targetTeam, trackersUnlocked, context);
                }
            );
        }
    }

    private ItemStack.Builder buildTrackerItem(BedWarsPlayer player, TeamKey targetTeam, boolean trackersUnlocked) {
        boolean trackingThisTeam = isTrackingTeam(player, targetTeam);
        boolean canAfford = BedWarsInventoryManipulator.hasEnoughMaterial(player, Material.EMERALD, TRACKER_PRICE);

        TextColor nameColor = trackersUnlocked && (canAfford || trackingThisTeam) ? NamedTextColor.GREEN : NamedTextColor.RED;

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Purchase tracking upgrade for your"));
        lore.add(Text.of("<7>compass which will track each player"));
        lore.add(Text.of("<7>on a specific team until you die."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Cost: <2>{} Emeralds", TRACKER_PRICE));
        lore.add(Text.empty());

        if (!trackersUnlocked) {
            lore.add(Text.of("<c>Unlocks when all enemy beds are destroyed!"));
        } else if (trackingThisTeam) {
            lore.add(Text.of("<a>You are already tracking this team!"));
        } else if (canAfford) {
            lore.add(Text.of("<e>Click to purchase!"));
        } else {
            lore.add(Text.of("<c>You don't have enough Emeralds!"));
        }

        return ItemStacks.item(
            targetTeam.bedMaterial(),
            1,
            Text.of("<color:{}>Track Team {}", nameColor, Text.of("<color:{}>{}", targetTeam.chatColor(), targetTeam.getName())),
            lore
        );
    }

    private void handleTrackerPurchase(BedWarsPlayer player,
                                       BedWarsGame game,
                                       TeamKey targetTeam,
                                       boolean trackersUnlocked,
                                       ViewContext ctx) {
        if (!trackersUnlocked) {
            player.sendMessage("<c>Unlocks when all enemy beds are destroyed!");
            GUIQuickCommunications.playClickSound(player);
            return;
        }

        if (isTrackingTeam(player, targetTeam)) {
            player.sendMessage("<c>You are already tracking this team!");
            GUIQuickCommunications.playClickSound(player);
            return;
        }

        if (!BedWarsInventoryManipulator.hasEnoughMaterial(player, Material.EMERALD, TRACKER_PRICE)) {
            player.sendMessage("<c>You don't have enough Emeralds!");
            GUIQuickCommunications.playClickSound(player);
            return;
        }

        BedWarsInventoryManipulator.removeItems(player, Material.EMERALD, TRACKER_PRICE);
        game.getTrackers().put(player.getUuid(), targetTeam);
        player.sendMessage("<a>Your compass is now tracking {}<a>!",
            Text.of("<color:{}>Team {}", targetTeam.chatColor(), targetTeam.getName()));

        GUIQuickCommunications.playBuySound(player);
        ctx.session(DefaultState.class).refresh();
    }

    private boolean isTrackingTeam(BedWarsPlayer player, TeamKey teamKey) {
        BedWarsGame game = player.getGame();
        if (game == null) {
            return false;
        }

        TeamKey trackedTeam = game.getTrackers().get(player.getUuid());
        return trackedTeam == teamKey;
    }

    private List<TeamKey> getEnemyTeams(BedWarsGame game, TeamKey ownTeam) {
        return game.getTeams().stream()
            .filter(BedWarsTeam::hasPlayers)
            .map(BedWarsTeam::getTeamKey)
            .filter(team -> team != ownTeam)
            .sorted(Comparator.comparingInt(Enum::ordinal))
            .toList();
    }

}
