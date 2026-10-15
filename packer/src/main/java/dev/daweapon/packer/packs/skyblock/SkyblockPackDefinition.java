package dev.daweapon.packer.packs.skyblock;

import dev.daweapon.packer.PackDefinition;

// PackDefinition for the SkyBlock resource pack, sourced from the configuration/resourcepacks/skyblockpack directory.
public class SkyblockPackDefinition implements PackDefinition {
    public static final SkyblockPackDefinition INSTANCE = new SkyblockPackDefinition();

    @Override
    public String getPackName() {
        return "skyblockpack";
    }

    @Override
    public String getPackDirectory() {
        return "configuration/resourcepacks/skyblockpack";
    }
}
