package dev.daweapon.velocity.viaversion;

import com.viaversion.viaversion.platform.UserConnectionViaVersionPlatform;

import java.io.File;
import java.util.logging.Logger;

// ViaVersion platform implementation identifying this Velocity plugin to the ViaVersion/ViaBackwards/ViaRewind stack.
public final class SkyBlockPlatform extends UserConnectionViaVersionPlatform {
    public SkyBlockPlatform(File dataFolder) {
        super(new File(dataFolder.toURI()));
    }

    @Override
    public Logger createLogger(String name) {
        return Logger.getLogger(name);
    }

    @Override
    public String getPlatformName() {
        return "SkyBlockVelocity";
    }

    @Override
    public String getPlatformVersion() {
        return "1.0";
    }
}
