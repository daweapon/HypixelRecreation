package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;
import dev.daweapon.type.skyblockgeneric.user.island.weather.IslandWeatherSettings;

// Island lifecycle step that restores the island's weather once its Weather Node is unlocked.
public class WeatherLoadStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.LOAD;
    }

    @Override
    public int order() {
        return 25;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        IslandWeatherSettings.apply(context.island());
    }
}
