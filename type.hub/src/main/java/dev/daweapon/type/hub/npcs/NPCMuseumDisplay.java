package dev.daweapon.type.hub.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.HypixelDataHandler;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import dev.daweapon.type.skyblockgeneric.gui.inventories.museum.GUIYourMuseum;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

public class NPCMuseumDisplay extends HypixelNPC {
    public NPCMuseumDisplay() {
        super(new MuseumHumanConfiguration());
    }

    private static class MuseumHumanConfiguration extends HumanConfiguration {
        private SkyBlockDataHandler skyblockHandler = null;
        private HypixelDataHandler dataHandler = null;

        @Override
        public String[] holograms(HypixelPlayer p) {
			SkyBlockPlayer player = (SkyBlockPlayer) p;
			DatapointMuseum.MuseumData data = player.getMuseumData();

            UUID currentlyViewing = data.getCurrentlyViewing().playerUuid();
            UUID profileUUID = data.getCurrentlyViewing().profileUuid();
			if (skyblockHandler == null) {
				skyblockHandler = SkyBlockDataHandler.getProfileOfOfflinePlayer(currentlyViewing, profileUUID);
			}
            if (dataHandler == null) {
                dataHandler = HypixelDataHandler.getOfOfflinePlayer(currentlyViewing);
            }
			Component colouredName = p.getColouredName();
			TextColor nameColour = colouredName.color();
			String username = PlainTextComponentSerializer.plainText().serialize(colouredName);
			String profileName = skyblockHandler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue();

			return new String[]{
					Text.of("<color:{}>{}", nameColour, username).serialize(),
					Text.of("<e>Profile: <a>{}", profileName).serialize()
			};
		}

        @Override
        public String signature(HypixelPlayer p) {
            SkyBlockPlayer player = (SkyBlockPlayer) p;
            if (dataHandler == null) {
                DatapointMuseum.MuseumData data = player.getMuseumData();
                UUID currentlyViewing = data.getCurrentlyViewing().playerUuid();

                dataHandler = HypixelDataHandler.getOfOfflinePlayer(currentlyViewing);
            }
            return dataHandler.get(HypixelDataHandler.Data.SKIN_SIGNATURE, DatapointString.class).getValue();
        }

        @Override
        public String texture(HypixelPlayer p) {
            SkyBlockPlayer player = (SkyBlockPlayer) p;
            if (dataHandler == null) {
                DatapointMuseum.MuseumData data = player.getMuseumData();
                UUID currentlyViewing = data.getCurrentlyViewing().playerUuid();

                dataHandler = HypixelDataHandler.getOfOfflinePlayer(currentlyViewing);
            }
            return dataHandler.get(HypixelDataHandler.Data.SKIN_TEXTURE, DatapointString.class).getValue();
        }

        @Override
        public Pos position(HypixelPlayer player) {
            return new Pos(29.5, 70, 35.5, 180, 0);
        }

        @Override
        public boolean looking(HypixelPlayer player) {
            return false;
        }
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().openView(new GUIYourMuseum());
    }
}