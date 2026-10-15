package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.commons.Songs;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.SkyBlockTypeLoader;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import dev.daweapon.type.skyblockgeneric.noteblock.SkyBlockSong;
import dev.daweapon.type.skyblockgeneric.noteblock.SkyBlockSongsHandler;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Plays a region's ambient noteblock song when a player enters it
public class ActionRegionPlaySong implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, isAsync = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerRegionChangeEvent event) {
        SkyBlockPlayer player = event.getPlayer();
        SkyBlockSongsHandler songHandler = player.getSongHandler();
        if (!SkyBlockSongsHandler.isEnabled) return;

        RegionType to = event.getTo();
        RegionType from = event.getFrom();
        if (to == null || (from != null && to.equals(from))) return;

        Songs songToPlay = null;
        List<Songs> regionSongs = to.getSongs();
        if (regionSongs != null && !regionSongs.isEmpty()) {
            songToPlay = regionSongs.get((int) (Math.random() * regionSongs.size()));
        } else if (HypixelConst.getTypeLoader() instanceof SkyBlockTypeLoader skyBlockTypeLoader) {
            songToPlay = skyBlockTypeLoader.getIslandSong();
        }

        SkyBlockSongsHandler.PlayerSong activeSong = songHandler.getPlayerSong();
        if (songToPlay == null) {
            if (activeSong != null) songHandler.stopPlayerSong();
            return;
        }

        if (activeSong != null && activeSong.song().getSong() == songToPlay) {
            return;
        }

        songHandler.setPlayerSong(new SkyBlockSong(songToPlay));

        SkyBlockActionBar.getFor(player).addReplacement(
            SkyBlockActionBar.BarSection.MANA,
            Text.of("<2>{}", StringUtility.toNormalCase(songToPlay.name())),
            20,
            2
        );
    }
}
