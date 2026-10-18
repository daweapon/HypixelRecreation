package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.commission.Commission;
import dev.daweapon.type.skyblockgeneric.commission.CommissionZone;
import dev.daweapon.type.skyblockgeneric.commission.Commissions;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCommissions;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CommissionsWidget implements TabWidget {

    @Override
    public String id() {
        return "commissions";
    }

    @Override
    public String displayName() {
        return "Commissions";
    }

    @Override
    public Material icon() {
        return Material.WRITABLE_BOOK;
    }

    @Override
    public int defaultPriority() {
        return 120;
    }

    @Override
    public Set<TabLocation> locations() {
        return Set.of(TabLocation.DWARVEN_MINES, TabLocation.CRYSTAL_HOLLOWS);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        CommissionZone zone = context.location() == TabLocation.CRYSTAL_HOLLOWS
                ? CommissionZone.CRYSTAL_HOLLOWS : CommissionZone.DWARVEN_MINES;
        List<DatapointCommissions.ActiveCommission> active = zone.active(Commissions.getCommissionData(player));
        if (active.isEmpty()) return List.of();
        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<6><l>Commissions:"));
        for (DatapointCommissions.ActiveCommission entry : active) {
            Commission commission = Commissions.getCommissionByName(entry.getCommissionName());
            if (entry.isCompleted() || commission == null) {
                lines.add(Text.of(" <f>{}: <a>DONE", entry.getCommissionName()));
                continue;
            }
            double percent = Math.min(100, entry.getProgress() * 100.0 / Math.max(1, commission.objective.amount));
            lines.add(Text.of(" <f>{}: <e>{}%", entry.getCommissionName(), String.format("%.1f", percent)));
        }
        return lines;
    }
}
