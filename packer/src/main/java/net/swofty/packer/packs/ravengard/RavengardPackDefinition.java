package net.swofty.packer.packs.ravengard;

import net.swofty.packer.PackDefinition;

// PackDefinition for the Ravengard resource pack, sourced from the configuration/resourcepacks/ravengard directory.
public class RavengardPackDefinition implements PackDefinition {
    public static final RavengardPackDefinition INSTANCE = new RavengardPackDefinition();

    @Override
    public String getPackName() {
        return "ravengard";
    }

    @Override
    public String getPackDirectory() {
        return "configuration/resourcepacks/ravengard";
    }
}
