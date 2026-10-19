package dev.daweapon.type.skyblockgeneric.miningevent;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

final class BetterTogetherEvent extends PassiveEvent {
    BetterTogetherEvent(MiningEventZone zone) {
        super(MiningEventType.BETTER_TOGETHER, zone);
    }

    @Override
    protected void onStart() {
        MiningEventConfig config = MiningEventConfig.get();
        bannerWith("BETTER TOGETHER STARTED!", List.of(
                "<f>The Better Together Festival is underway! While this event is active, gain <6>+"
                        + config.integer(250, "better_together", "mining_speed_per_player") + " Mining Speed <f>and <6>+"
                        + config.integer(20, "better_together", "mining_fortune_per_player") + " Mining Fortune <f>for each player in the same zone as you! (Max "
                        + config.integer(5, "better_together", "max_players") + " players)",
                " ",
                "<7><i>You can hear cooperative chattering coming from town...",
                "<7><i>The Better Together festival is active for " + config.durationMillis(type()) / 60_000 + " minutes!"));
    }

    @Override
    protected void onEnd(boolean cancelled) {
        MiningEventMessages.banner(type(), "BETTER TOGETHER ENDED!", null,
                List.of("<f>The festival has ended, but the Dwarven cooperative spirit never dies."));
    }

    static ItemStatistics statistics(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        if (region == null) return ItemStatistics.empty();
        RegionType zone = region.getType();
        MiningEventConfig config = MiningEventConfig.get();
        int others = (int) MiningEvents.players().stream()
                .filter(other -> other != player && other.getRegion() != null && other.getRegion().getType() == zone)
                .count();
        int count = Math.min(others, config.integer(5, "better_together", "max_players"));
        if (count == 0) return ItemStatistics.empty();
        return ItemStatistics.empty()
                .addBase(ItemStatistic.MINING_SPEED, count * config.number(250, "better_together", "mining_speed_per_player"))
                .addBase(ItemStatistic.MINING_FORTUNE, count * config.number(20, "better_together", "mining_fortune_per_player"));
    }
}
