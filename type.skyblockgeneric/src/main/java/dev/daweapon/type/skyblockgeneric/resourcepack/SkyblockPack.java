package dev.daweapon.type.skyblockgeneric.resourcepack;

import dev.daweapon.commons.config.Settings;
import dev.daweapon.packer.HypixelPackBuilder;
import dev.daweapon.packer.packs.skyblock.SkyblockPackDefinition;
import dev.daweapon.type.generic.resourcepack.HypixelResourcePack;
import dev.daweapon.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;
import team.unnamed.creative.BuiltResourcePack;

public class SkyblockPack implements HypixelResourcePack {
    private static final SkyblockPackDefinition DEFINITION = SkyblockPackDefinition.INSTANCE;

    private final String packUrl;
    private final String packHash;

    public SkyblockPack(String serverUrl, String hash) {
        this.packHash = hash;
        this.packUrl = serverUrl + "/" + hash + ".zip";
    }

    public static SkyblockPack fromConfig() {
        Settings.ResourcePackSettings settings = HypixelResourcePack.getConfigFor(DEFINITION.getPackName());

        Logger.info("Building resource pack '{}' from {}...", DEFINITION.getPackName(), DEFINITION.getPackDirectory());
        HypixelPackBuilder builder = new HypixelPackBuilder(DEFINITION);
        BuiltResourcePack built = builder.build();
        Logger.info("Resource pack '{}' built. Hash: {}", DEFINITION.getPackName(), built.hash());

        return new SkyblockPack(settings.getServerUrl(), built.hash());
    }

    @Override
    public String getPackName() {
        return DEFINITION.getPackName();
    }

    @Override
    public String getPackUrl() {
        return packUrl;
    }

    @Override
    public String getPackHash() {
        return packHash;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void onPlayerJoin(HypixelPlayer player) {
    }

    @Override
    public void onPlayerQuit(HypixelPlayer player) {
    }
}
