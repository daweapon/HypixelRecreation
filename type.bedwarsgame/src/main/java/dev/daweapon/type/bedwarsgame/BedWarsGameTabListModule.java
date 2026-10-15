package dev.daweapon.type.bedwarsgame;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.PlayerSkin;
import dev.daweapon.commons.party.FullParty;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.generic.party.PartyManager;
import dev.daweapon.type.generic.tab.CustomTablistSkin;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkin;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import dev.daweapon.commons.text.Text;

// Builds the in-game Bedwars tab list, including per-player skins and party obfuscation.
public class BedWarsGameTabListModule extends TablistModule {

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer p) {
        BedWarsPlayer player = (BedWarsPlayer) p;
        BedWarsGame game = player.getGame();
        if (game == null) {
            return List.of();
        }

        Collection<BedWarsPlayer> players = game.getPlayers();
        if (game.getState().isWaiting()) {
            TablistEntry[] entries = new TablistEntry[players.size()];
            int index = 0;
            for (BedWarsPlayer bedWarsPlayer : players) {
                boolean shouldObfuscate = bedWarsPlayer.getUuid().compareTo(player.getUuid()) != 0;
                FullParty party = PartyManager.getPartyFromPlayer(player);
                if (party != null && !shouldObfuscate) {
                    shouldObfuscate = !party.getParticipants().contains(p.getUuid());
                }

                TablistSkin skin;
                PlayerSkin playerSkin = bedWarsPlayer.getSkin();

                if (shouldObfuscate) {
                    skin = TablistSkinRegistry.GRAY;
                } else if (playerSkin == null) {
                    skin = TablistSkinRegistry.GRAY;
                } else {
                    skin = new CustomTablistSkin(playerSkin);
                }

                Component displayName = shouldObfuscate
                        ? Component.text(UUID.randomUUID().toString().replaceAll("-", "").substring(0, new Random().nextInt(10) + 4), Style.style(TextDecoration.OBFUSCATED))
                        : bedWarsPlayer.getColouredName();
                entries[index++] = new TablistEntry(Text.of("{}", displayName), skin);
            }
            return List.of(entries);
        } else if (game.getState().isInProgress()) {
            TablistEntry[] entries = new TablistEntry[players.size()];
            int index = 0;
            for (BedWarsPlayer bedWarsPlayer : players) {
                Component displayName = bedWarsPlayer.getDisplayName();
                if (displayName == null) {
                    displayName = Component.text(bedWarsPlayer.getUsername());
                }
                TablistSkin skin;
                PlayerSkin playerSkin = bedWarsPlayer.getSkin();
                if (playerSkin == null) {
                    skin = TablistSkinRegistry.GRAY;
                } else {
                    skin = new CustomTablistSkin(playerSkin);
                }
                entries[index++] = new TablistEntry(Text.of("{}", displayName), skin);
            }
            return List.of(entries);
        }

        return List.of();
    }
}
