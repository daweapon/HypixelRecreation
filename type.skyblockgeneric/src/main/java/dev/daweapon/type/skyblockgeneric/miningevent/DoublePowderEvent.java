package dev.daweapon.type.skyblockgeneric.miningevent;

import java.util.List;

final class DoublePowderEvent extends PassiveEvent {
    DoublePowderEvent(MiningEventZone zone) {
        super(MiningEventType.DOUBLE_POWDER, zone);
    }

    @Override
    protected void onStart() {
        bannerWith("2X POWDER STARTED!", List.of("<f>Everybody gets twice as much powder of all types!"));
    }

    @Override
    protected void onEnd(boolean cancelled) {
        MiningEventMessages.banner(type(), "2X POWDER ENDED!", null,
                List.of("<f>Powder amount is back to normal, but there will be another event soon!"));
    }
}
