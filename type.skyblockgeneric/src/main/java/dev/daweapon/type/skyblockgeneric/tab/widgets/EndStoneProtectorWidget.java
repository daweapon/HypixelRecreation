package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.theend.ProtectorProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

final class EndStoneProtectorWidget extends AreaWidget {
    EndStoneProtectorWidget() {
        super("end_stone_protector", "End Stone Protector", Material.END_STONE, 50, TabLocation.THE_END);
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        return List.of(
                Text.parse("<d><l>End Stone Protector: " + ProtectorProgress.stage().display()),
                Text.parse(" Zealots Killed: <a>" + StringUtility.commaify(ProtectorProgress.kills())
                        + "<7>/" + StringUtility.commaify(ProtectorProgress.KILLS_TO_SUMMON)));
    }
}
