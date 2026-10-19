package dev.daweapon.type.skyblockgeneric.miningevent;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

abstract class PassiveEvent extends MiningEvent {
    PassiveEvent(MiningEventType type, MiningEventZone zone) {
        super(type, zone);
    }

    @Override
    public void tick(long now) {
        for (SkyBlockPlayer player : MiningEvents.players()) markParticipant(player);
    }

    protected final void bannerWith(String title, List<String> lines) {
        MiningEventMessages.banner(type(), title, "Passive Active Event", lines);
    }
}
