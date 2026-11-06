package dev.daweapon.type.catacombs.npc;

import dev.daweapon.type.catacombs.run.CatacombsMessages;
import dev.daweapon.type.catacombs.run.CatacombsRun;
import dev.daweapon.type.catacombs.run.CatacombsRuns;
import dev.daweapon.type.catacombs.TypeCatacombsLoader;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.dungeons.npc.MortNPC;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

/**
 * Mort in each run's Entrance room, where the captured runs had him: by the exit, facing into the room, labelled
 * {@code Mort} / {@code CLICK} with the same skin as the Hub's Mort. Before the run starts a click readies the player up
 * (on Hypixel that is done through his menu); once it has started he wishes them luck again.
 */
public class CatacombsMortNPC extends HypixelNPC {

    public CatacombsMortNPC() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Mort", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return MortNPC.SIGNATURE;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return MortNPC.TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                CatacombsRun run = runOf(player);
                return run == null ? TypeCatacombsLoader.WAITING_POSITION : run.arena().mortSpot().orElse(TypeCatacombsLoader.WAITING_POSITION);
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                CatacombsRun run = runOf(player);
                return run != null && run.arena().mortSpot().isPresent() && player.getInstance() == run.instance();
            }

            @Override
            public Instance instance(HypixelPlayer player) {
                CatacombsRun run = runOf(player);
                return run == null ? HypixelConst.getEmptyInstance() : run.instance();
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<b>Mort";
            }
        });
    }

    private static @Nullable CatacombsRun runOf(HypixelPlayer player) {
        return CatacombsRuns.of(player.getUuid());
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (!(event.player() instanceof SkyBlockPlayer player)) return;
        CatacombsRun run = runOf(player);
        if (run == null) return;
        if (!run.started()) {
            run.toggleReady(player);
            return;
        }
        player.sendMessage(CatacombsMessages.mortLines().getLast());
    }
}
