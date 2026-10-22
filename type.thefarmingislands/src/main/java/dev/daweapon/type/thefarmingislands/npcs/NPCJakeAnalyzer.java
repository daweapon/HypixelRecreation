package dev.daweapon.type.thefarmingislands.npcs;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.farming.lab.LabQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.thefarmingislands.lab.LabScenes;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.farming.lab.CropAnalyzerAccess;

public class NPCJakeAnalyzer extends HypixelNPC {
    public NPCJakeAnalyzer() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Jake", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "pMvZ+g1p1HCldbVKd1w05q4HyluUjAeEJipZQe7omlFQdiF5PIoHo6sY7k1/dwPQwXgUGsytvxnFfIiWAflryAC5FrxLpTLhrghbuf0zgbNtrjhA6s67txvbp1jpAs7k4ydufiQ39Ob/Y7+Ojx8ZiPMb6UK5OrwPmUmyq1KxloEl6N2qzA1odf7sSXuVxrFlbPwT8p9rEsV8ogCUmzmTiPqkIAGUCWJ3ZbRDOFjOKbrD8Ff0/7qQ6g0BDrOLNPWeHl/eJHT8puCOktNIQs1lwYyNF9gllpkicFOQ9XqCJH8END6jyyF0wgfubFAFB1eoQKGRenmuY4ktJQT+D3yZlOzV20zbtGzUvVvkC7ooYyWFLY3L6m5NCEUNwU5XJbc1hK9PQH1zajY+v1Lp09DsVmiib4NjDsUvb6nF6BISH6w3J9ZNLlwWcoawXs5EqLQbpQxDXuNkD1q3FszvuCoRYOgmXSZqverQ+v5L18BnUjDMM1bUm5r/gH/hPp06Xc5jZmzhYsajqKwhCnsyYY8NS3PD3BIM96a3XhheEVoFZRvg7OHDQtQgzAydJE9bnJmFTFyThUR5ZH11OlSPmTMToKdOUaj+TbhEPGUXPolQ6gpHbcpRVD93RUbKqfPYIaQvgnPMiF6rWJlOHaYetFEmgIeAQGHzEG8KzTvKQChZQ14=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5NjA3ODI4NjQ3MywKICAicHJvZmlsZUlkIiA6ICJkODAwZDI4MDlmNTE0ZjkxODk4YTU4MWYzODE0Yzc5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVCTFJ4eCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNTFjYzdjMWQxMWFhMGEwOGE3M2NiYzE4NDU0NzA1MWFlYmQwN2IzYmI1MGQ4NmYxMTg4ZmQ3YTdkYWQyZWQzIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return LabScenes.HIDDEN_SECTION_JAKE;
            }

            @Override
            public boolean dynamicPosition() {
                return false;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return LabQuest.stage((SkyBlockPlayer) player).compareTo(LabQuest.Stage.LAB2_LEVER) >= 0;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        if (!CropAnalyzerAccess.has(player)) {
            sendNPCMessage(player, "Beth... I told you to stay out of this.");
            return;
        }
        sendNPCMessage(player, "Feel free to analyze any mutations you find!");
        CropAnalyzerAccess.open(player);
    }
}
