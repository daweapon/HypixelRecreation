package dev.daweapon.type.skyblockgeneric.minion.actions;

import lombok.Getter;
import net.minestom.server.instance.Instance;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class MinionFishingAction extends MinionAction {
    private record Catch(ItemType item, double weight) {
    }

    private static final List<Catch> CATCHES = List.of(
            new Catch(ItemType.RAW_FISH, 0.54),
            new Catch(ItemType.RAW_SALMON, 0.225),
            new Catch(ItemType.PUFFERFISH, 0.117),
            new Catch(ItemType.TROPICAL_FISH, 0.036),
            new Catch(ItemType.PRISMARINE_CRYSTALS, 0.02734),
            new Catch(ItemType.PRISMARINE_SHARD, 0.02734),
            new Catch(ItemType.SPONGE, 0.02734));


    @Override
    public @NotNull List<SkyBlockItem> onAction(MinionActionEvent event, IslandMinionData.IslandMinion minion, Instance island) {
        return null;
    }

    @Override
    public List<SkyBlockItem> offlineYield(IslandMinionData.IslandMinion minion) {
        Catch caught = LootRoll.pick(null, CATCHES, Catch::item, Catch::weight, ThreadLocalRandom.current());
        return List.of(new SkyBlockItem(caught.item()));
    }

    @Override
    public int actionsPerItem() {
        return 1;
    }

    @Override
    public boolean checkMaterials(IslandMinionData.IslandMinion minion, Instance island) {
        return false;
    }
}
