package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.hotm.HotmAbilities;
import dev.daweapon.type.skyblockgeneric.hotm.HotmAbility;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Set;

public final class PickaxeAbilityWidget implements TabWidget {

    @Override
    public String id() {
        return "pickaxe_ability";
    }

    @Override
    public String displayName() {
        return "Pickaxe Ability";
    }

    @Override
    public Material icon() {
        return Material.DIAMOND_PICKAXE;
    }

    @Override
    public int defaultPriority() {
        return 130;
    }

    @Override
    public Set<TabLocation> locations() {
        return Set.of(TabLocation.DWARVEN_MINES, TabLocation.CRYSTAL_HOLLOWS, TabLocation.GOLD_MINE, TabLocation.DEEP_CAVERNS);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        HotmAbility ability = HotmAbilities.selected(player);
        if (ability == null) return List.of();
        long remaining = HotmAbilities.cooldownRemainingMillis(player, ability);
        Text status = remaining <= 0 ? Text.of("<a>Ready") : Text.of("<e>{}", TabTime.format(remaining));
        return List.of(Text.of("<6><l>Pickaxe Ability:"), Text.of(" <f>{}: {}", ability.getDisplayName(), status));
    }
}
