package dev.daweapon.type.skyblockgeneric.minion.actions;

import lombok.Getter;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.minion.IslandMinionData;
import dev.daweapon.type.skyblockgeneric.minion.MinionAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class MinionFishingAction extends MinionAction {

    @Override
    public @NotNull List<SkyBlockItem> onAction(MinionActionEvent event, IslandMinionData.IslandMinion minion, Instance island) {
        return null;
    }

    @Override
    public boolean checkMaterials(IslandMinionData.IslandMinion minion, Instance island) {
        return false;
    }
}
