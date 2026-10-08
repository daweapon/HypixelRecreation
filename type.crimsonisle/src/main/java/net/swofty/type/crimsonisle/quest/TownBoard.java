package net.swofty.type.crimsonisle.quest;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.crimsonisle.quest.gui.GUITownBoard;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.entity.BlockDisplayEntity;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.generic.entity.hologram.ServerHolograms;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * The two Town Boards, mounted on the boards built into each Community Center: Scarleton's on the
 * west face of the nether-brick framed stone panel, Dragontail's on the east face of the black terracotta
 * wall. Placed from SkyHanni's "Quest Board" waypoints and the world blocks.
 */
public enum TownBoard {
    MAGE(CrimsonFaction.MAGE, new Pos(-119.1, 93, -754.5)),
    BARBARIAN(CrimsonFaction.BARBARIAN, new Pos(-583.9, 100.5, -687.5)),
    ;

    private final CrimsonFaction faction;
    private final Pos position;

    TownBoard(CrimsonFaction faction, Pos position) {
        this.faction = faction;
        this.position = position;
    }

    public static void spawnAll() {
        for (TownBoard board : values()) board.spawn(HypixelConst.getInstanceContainer());
    }

    private void spawn(Instance instance) {
        new BlockDisplayEntity(Block.DARK_OAK_PLANKS, meta -> {
            meta.setScale(new Vec(0.15, 1.6, 2.4));
            meta.setTranslation(new Vec(-0.075, 0.2, -1.2));
        }).setInstance(instance, position);
        new BlockDisplayEntity(Block.WHITE_WOOL, meta -> {
            meta.setScale(new Vec(0.17, 1.2, 2.0));
            meta.setTranslation(new Vec(-0.085, 0.4, -1.0));
        }).setInstance(instance, position);
        new InteractionEntity(1.2f, 2.0f, (player, _) -> open((SkyBlockPlayer) player)).setInstance(instance, position);
        ServerHolograms.addExternalHologram(ServerHolograms.ExternalHologram.builder()
                .instance(instance)
                .pos(position.add(0, 2.1, 0))
                .text(new String[]{faction.getColour() + "<l>Town Board", "<e><l>CLICK"})
                .build());
    }

    private void open(SkyBlockPlayer player) {
        CrimsonFaction joined = CrimsonReputationService.faction(player);
        if (joined == null) {
            player.sendMessage("<c>You need to join a faction before you can take on Town Board requests! <7>Talk to <d>Elle<7>.");
            return;
        }
        if (joined != faction) {
            player.sendMessage("<c>This Town Board only has requests for the " + faction.coloured() + " Faction<c>!");
            return;
        }
        player.openView(new GUITownBoard());
    }
}
