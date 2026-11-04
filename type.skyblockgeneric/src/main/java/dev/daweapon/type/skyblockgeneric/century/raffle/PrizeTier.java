package dev.daweapon.type.skyblockgeneric.century.raffle;

public enum PrizeTier {
    MAJOR("<c><l>MAJOR"),
    MEDIUM("<5><l>MEDIUM"),
    MINOR("<e><l>MINOR");

    private final String display;

    PrizeTier(String display) {
        this.display = display;
    }

    public String display() {
        return display;
    }
}
