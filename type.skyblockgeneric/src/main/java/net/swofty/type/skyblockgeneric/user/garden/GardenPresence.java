package net.swofty.type.skyblockgeneric.user.garden;

import net.minestom.server.instance.SharedInstance;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public interface GardenPresence {
    GardenPresence NONE = new GardenPresence() {
        @Override
        public void arrived(SkyBlockPlayer player, SharedInstance instance) {
        }

        @Override
        public void departed(SkyBlockPlayer player) {
        }
    };

    void arrived(SkyBlockPlayer player, SharedInstance instance);

    void departed(SkyBlockPlayer player);
}
