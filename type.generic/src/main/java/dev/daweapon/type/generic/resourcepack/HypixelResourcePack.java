package dev.daweapon.type.generic.resourcepack;

import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.commons.config.Settings;
import dev.daweapon.type.generic.user.HypixelPlayer;

public interface HypixelResourcePack {
    String getPackName();
    String getPackUrl();
    String getPackHash();

    boolean isRequired();

    void initialize();
    void onPlayerJoin(HypixelPlayer player);
    void onPlayerQuit(HypixelPlayer player);

    static Settings.ResourcePackSettings getConfigFor(String packName) {
        return ConfigProvider.settings().getResourcePacks()
                .getOrDefault(packName, new Settings.ResourcePackSettings());
    }
}
