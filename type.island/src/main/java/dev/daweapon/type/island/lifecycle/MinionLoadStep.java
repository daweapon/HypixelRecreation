package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.MinionOfflineProduction;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleContext;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;
import org.bson.Document;

import java.util.Map;

// Island lifecycle step that restores minions on load and simulates offline production since last save.
public class MinionLoadStep implements IslandLifecycleStep {
    @Override
    public IslandLifecyclePhase phase() {
        return IslandLifecyclePhase.LOAD;
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public void run(IslandLifecycleContext context) {
        Document document = context.island().getDatabase().getDocument();
        Map<String, Object> rawData = document == null ? null : (Map<String, Object>) document.get("minions");

        IslandMinionData minionData = rawData == null
                ? new IslandMinionData(context.island())
                : IslandMinionData.deserialize(rawData, context.island());

        context.island().setMinionData(minionData);

        long currentTime = System.currentTimeMillis();

        minionData.getMinions().forEach(data -> {
            MinionOfflineProduction.credit(data, context.island().getIslandInstance(), currentTime);
            data.spawnMinion(context.island().getIslandInstance());
        });
    }
}
