package dev.daweapon.type.skyblockgeneric.miningevent;

import dev.daweapon.type.skyblockgeneric.commission.EventType;

import java.util.Set;

public enum MiningEventType {
    DOUBLE_POWDER("2x Powder", "<b>", MiningEventKind.PASSIVE, EventType.DOUBLE_POWDER,
            Set.of(MiningEventZone.DWARVEN_MINES, MiningEventZone.CRYSTAL_HOLLOWS)),
    BETTER_TOGETHER("Better Together", "<d>", MiningEventKind.PASSIVE, EventType.NONE,
            Set.of(MiningEventZone.DWARVEN_MINES, MiningEventZone.CRYSTAL_HOLLOWS)),
    MITHRIL_GOURMAND("Mithril Gourmand", "<b>", MiningEventKind.ACTIVE, EventType.NONE,
            Set.of(MiningEventZone.DWARVEN_MINES)),
    GOBLIN_RAID("Goblin Raid", "<c>", MiningEventKind.ACTIVE, EventType.GOBLIN_RAID,
            Set.of(MiningEventZone.DWARVEN_MINES)),
    RAFFLE("Raffle", "<6>", MiningEventKind.ACTIVE, EventType.RAFFLE,
            Set.of(MiningEventZone.DWARVEN_MINES));

    private final String displayName;
    private final String color;
    private final MiningEventKind kind;
    private final EventType commissionEvent;
    private final Set<MiningEventZone> zones;

    MiningEventType(String displayName, String color, MiningEventKind kind, EventType commissionEvent, Set<MiningEventZone> zones) {
        this.displayName = displayName;
        this.color = color;
        this.kind = kind;
        this.commissionEvent = commissionEvent;
        this.zones = zones;
    }

    public String displayName() {
        return displayName;
    }

    public String color() {
        return color;
    }

    public MiningEventKind kind() {
        return kind;
    }

    public EventType commissionEvent() {
        return commissionEvent;
    }

    public boolean runsIn(MiningEventZone zone) {
        return zones.contains(zone);
    }

    public String coloredName() {
        return color + displayName;
    }
}
