package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.potion.CookieBuffService;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.Material;

import java.util.List;

public final class CookieBuffWidget implements TabWidget {
    public static final String ID = "cookie_buff";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Cookie Buff";
    }

    @Override
    public Material icon() {
        return Material.COOKIE;
    }

    @Override
    public int defaultPriority() {
        return 60;
    }

    @Override
    public boolean otherSection() {
        return true;
    }

    @Override
    public boolean enabledByDefault(dev.daweapon.type.skyblockgeneric.tab.TabLocation location) {
        return false;
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        return List.of(
                Text.of("<d><l>Cookie Buff:"),
                player.isBoosterCookieActive()
                        ? Text.of(" <7>{}", CookieBuffService.formatRemaining(player))
                        : Text.of(" <7>Not active!"));
    }
}
