package dev.daweapon.type.skyblockgeneric.century.raffle;

import java.time.Duration;

public enum RaffleType {
    SPEED("Speed Raffle", "<6>", Duration.ofHours(2), 10),
    DAILY("Daily Raffle", "<3>", Duration.ofHours(24), 25),
    BIG_ONE("The Big One", "<c><l>", null, 0);

    public static final int TICKETS_PER_SLICE = 5;
    public static final int BIG_ONE_CAKE_TICKETS = 50;

    private final String displayName;
    private final String colour;
    private final Duration period;
    private final int consolationTicketCap;

    RaffleType(String displayName, String colour, Duration period, int consolationTicketCap) {
        this.displayName = displayName;
        this.colour = colour;
        this.period = period;
        this.consolationTicketCap = consolationTicketCap;
    }

    public String displayName() {
        return displayName;
    }

    public String colour() {
        return colour;
    }

    public boolean periodic() {
        return period != null;
    }

    public long periodMillis() {
        return period == null ? 0 : period.toMillis();
    }

    public int consolationSlices(long ticketsEntered) {
        if (!periodic()) return 0;
        return (int) (Math.min(ticketsEntered, consolationTicketCap) / TICKETS_PER_SLICE);
    }

    public String label(int number) {
        return periodic() ? displayName + " #" + number : displayName;
    }

    public String key(int index) {
        return periodic() ? name().toLowerCase() + ":" + index : name().toLowerCase();
    }
}
