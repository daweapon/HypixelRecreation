package dev.daweapon.type.catacombs.blessing;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import dev.daweapon.type.catacombs.engine.DungeonHost;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The party's Dungeon Buffs: every blessing found stacks into the run's blessing set, and each player in the run
 * carries one temporary stat boost for the whole set until the run ends.
 */
public final class PartyBlessings {
    private static final long LIFETIME_MILLIS = 3 * 60 * 60 * 1000L;

    private final DungeonHost host;
    private final double effectiveness;
    private final Map<SkyBlockPlayer, TemporaryStatistic> applied = Collections.synchronizedMap(new IdentityHashMap<>());
    private volatile ItemStatistics current = ItemStatistics.empty();

    public PartyBlessings(DungeonHost host, double effectiveness) {
        this.host = host;
        this.effectiveness = effectiveness;
    }

    public void grant(BlessingType type, int level, @Nullable SkyBlockPlayer finder) {
        synchronized (this) {
            host.state().addBlessing(type, level);
            current = BlessingStats.of(host.state().blessings(), effectiveness);
            stripAll();
        }
        announce(type, level, finder);
        apply();
    }

    private void announce(BlessingType type, int level, @Nullable SkyBlockPlayer finder) {
        List<String> granted = BlessingMessages.granted(type, level, effectiveness);
        for (SkyBlockPlayer member : members()) {
            if (finder == null) member.sendMessage(BlessingMessages.foundInRoom(type, level));
            else if (member == finder) member.sendMessage(BlessingMessages.foundByYou(type, level));
            else member.sendMessage(Text.of(BlessingMessages.foundBy(type, level), finder.getColouredName()));
            granted.forEach(member::sendMessage);
            member.playSound(Sound.sound(Key.key("entity.player.levelup"), Sound.Source.PLAYER, 1f, 1.5f));
        }
    }

    public synchronized void apply() {
        if (host.state().blessings().levels().isEmpty()) return;
        for (SkyBlockPlayer member : members()) {
            if (applied.containsKey(member)) continue;
            TemporaryStatistic boost = new TemporaryStatistic(current, System.currentTimeMillis() + LIFETIME_MILLIS);
            applied.put(member, boost);
            member.getStatistics().boostStatistic(boost);
        }
    }

    private synchronized void stripAll() {
        applied.forEach((member, boost) -> member.getStatistics().removeTemporaryStatistics(each -> each == boost));
        applied.clear();
    }

    public Map<BlessingType, Integer> levels() {
        return host.state().blessings().levels();
    }

    public void dispose() {
        stripAll();
    }

    private List<SkyBlockPlayer> members() {
        return host.instance().getPlayers().stream()
                .filter(Player::isOnline)
                .filter(SkyBlockPlayer.class::isInstance)
                .map(SkyBlockPlayer.class::cast)
                .toList();
    }
}
