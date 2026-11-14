package dev.daweapon.type.skywarsgame.events;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.Material;
import dev.daweapon.commons.skywars.SkywarsLeaderboardMode;
import dev.daweapon.commons.skywars.SkywarsModeStats;
import dev.daweapon.type.generic.data.datapoints.DatapointSkywarsKitStats;
import dev.daweapon.type.generic.data.datapoints.DatapointSkywarsModeStats;
import dev.daweapon.type.generic.data.handlers.SkywarsDataHandler;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.game.SkywarsGameStatus;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;

public class ActionArrowStats implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onBowUse(PlayerUseItemEvent event) {
        if (!(event.getPlayer() instanceof SkywarsPlayer player)) return;
        if (event.getItemStack().material() != Material.BOW) return;

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game == null || game.getGameStatus() != SkywarsGameStatus.IN_PROGRESS) return;
        if (player.isEliminated()) return;

        player.addArrowShot();
        recordArrowShot(player, game);
    }

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onArrowHit(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        DamageType damageType = event.getDamage().getType().asValue();
        if (damageType != DamageType.ARROW.asValue()) return;

        Entity attacker = event.getDamage().getAttacker();
        if (!(attacker instanceof SkywarsPlayer shooter)) return;

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(shooter);
        if (game == null || game.getGameStatus() != SkywarsGameStatus.IN_PROGRESS) return;
        if (shooter.isEliminated()) return;

        shooter.addArrowHit();
        recordArrowHit(shooter, game, event.getEntity());
    }

    private void recordArrowShot(SkywarsPlayer player, SkywarsGame game) {
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
        if (handler == null) return;

        SkywarsLeaderboardMode mode = SkywarsLeaderboardMode.fromGameType(game.getGameType());

        DatapointSkywarsModeStats statsDP = handler.get(
                SkywarsDataHandler.Data.MODE_STATS,
                DatapointSkywarsModeStats.class);
        SkywarsModeStats stats = statsDP.getValue();
        stats.recordArrowShot(mode);

        DatapointSkywarsKitStats kitStatsDP = handler.get(
                SkywarsDataHandler.Data.KIT_STATS,
                DatapointSkywarsKitStats.class);
        DatapointSkywarsKitStats.SkywarsKitStats kitStats = kitStatsDP.getValue();
        kitStats.getStatsForKit(player.getSelectedKit()).addArrowShot();
    }

    private void recordArrowHit(SkywarsPlayer shooter, SkywarsGame game, Entity target) {
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(shooter);
        if (handler == null) return;

        SkywarsLeaderboardMode mode = SkywarsLeaderboardMode.fromGameType(game.getGameType());

        DatapointSkywarsModeStats statsDP = handler.get(
                SkywarsDataHandler.Data.MODE_STATS,
                DatapointSkywarsModeStats.class);
        SkywarsModeStats stats = statsDP.getValue();
        stats.recordArrowHit(mode);

        DatapointSkywarsKitStats kitStatsDP = handler.get(
                SkywarsDataHandler.Data.KIT_STATS,
                DatapointSkywarsKitStats.class);
        DatapointSkywarsKitStats.SkywarsKitStats kitStats = kitStatsDP.getValue();
        DatapointSkywarsKitStats.KitStatistics currentKitStats = kitStats.getStatsForKit(shooter.getSelectedKit());
        currentKitStats.addArrowHit();

        int distance = (int) shooter.getPosition().distance(target.getPosition());
        currentKitStats.setLongestBowShot(distance);
    }
}
