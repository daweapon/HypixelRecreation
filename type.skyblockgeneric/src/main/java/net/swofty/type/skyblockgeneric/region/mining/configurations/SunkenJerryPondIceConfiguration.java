package net.swofty.type.skyblockgeneric.region.mining.configurations;

public class SunkenJerryPondIceConfiguration extends JerryPondIceConfiguration {

    @Override
    protected long minRegrowMillis() {
        return 60_000;
    }

    @Override
    protected long maxRegrowMillis() {
        return 150_000;
    }
}
