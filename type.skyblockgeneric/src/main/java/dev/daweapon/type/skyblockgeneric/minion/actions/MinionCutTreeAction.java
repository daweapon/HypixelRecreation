package dev.daweapon.type.skyblockgeneric.minion.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
@Getter
public class MinionCutTreeAction extends MinionAction {
    private static final int OFFLINE_LOGS_PER_TREE = 3;

    private final Block toCut;

    @Override
    public @NotNull List<SkyBlockItem> onAction(MinionActionEvent event, IslandMinionData.IslandMinion minion, Instance island) {
        return null;
    }

    @Override
    public List<SkyBlockItem> offlineYield(IslandMinionData.IslandMinion minion) {
        ItemType log = ItemType.get(toCut.name());
        return log == null ? List.of() : List.of(new SkyBlockItem(log, OFFLINE_LOGS_PER_TREE));
    }

    @Override
    public boolean checkMaterials(IslandMinionData.IslandMinion minion, Instance island) {
        return false;
    }
}
