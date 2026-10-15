package dev.daweapon.type.bedwarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.VersionConst;
import dev.daweapon.commons.bedwars.BedWarsGameType;
import dev.daweapon.type.bedwarslobby.gui.GUIPlay;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.utility.GameCountCache;

// NPC that shows live 3v3v3v3 queue population and opens the Trios play GUI on click.
public class NPCTrios extends HypixelNPC {
    public NPCTrios() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                int amountOnline = GameCountCache.getPlayerCount(
                    ServerType.BEDWARS_GAME,
                    BedWarsGameType.FOUR_THREE.name()
                );

                String commaified = StringUtility.commaify(amountOnline);
                return new String[]{
                    "<e><l>CLICK TO PLAY",
                    "<b>3v3v3v3 <7>[v" + VersionConst.BED_WARS_VERSION + "]",
                    "<e><l>" + commaified + " Players",
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "qQLrYOBdPK4BONSKJVMn4QoIzvuDAs7FTx0+5JMExNNoTJpbtpB4t1loxNAhcCSBKu6tUMCLMNuAW1VM0cUyKd36PKWUW7rSWmxNfNk38FtsuA/5L0kbLhOfqbIqBybZNWz650XnuCj8w4alX6d4mmUESgxiuHy1EmvT2G/OocKabtP4QftLSimkMQY+f0WI6TwD5+wuINcfdW6LRJnJci0TvF4sru+i+WkOee0W5Bk09cizR2MOHRlGCFv9w2nScdKw5eg7ioL963IvlfL4cMz0Jah9GAdRxlie947Jxe+KGFyae4sikBbdjZCY9xB0/Hs3oCzoSFw0TAhmHQuXjnSqn8IINRR/Lw8nyePPXB5anC5J23MvMJGxaNSELwsiF9FrJHzlT65FrIDyFksf5+0t5nbqMpyxBWrRvUbQ0n1BUEplzXeDqzz0lawhHkxprmamjY7K2dAKSNRbHBOsCN4PoHkX6B/YGqYrYJQtsPRD5QV24kqFFSQipJwqQ0GZBvEmNOvhTEg6iz0Hz/aEVsGu2Ez1vKJ3Rld9Z5pmGWy/tJHvCOuspN134U4a5tKX/MDqN8Ny+qgdK8leAhImWB8Hovz1Fmwc7MJdAp6hEIVbjp6MUxP8I++2g/DMIDuSzH3uJwToZj0AYpKRqWyp5jaNJX2Gw8jNmVfrIczmyyw=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNjg2ODI4NTY4MCwKICAicHJvZmlsZUlkIiA6ICI2MWVhMDkyM2FhNDQ0OTEwYmNlZjViZmQ2ZDNjMGQ1NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVEYXJ0aEZhdGhlciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xMmJhZjVhYzRiMTc0NjUwM2Y0NmMwMTA4NTU0ZGFhZTkwMjA2ZjJjNjFjZGFhZTZlZTA4YmEyY2QwNmMxMjVjIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-1.5, 68, -7.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().openView(new GUIPlay(BedWarsGameType.FOUR_THREE));
    }
}
