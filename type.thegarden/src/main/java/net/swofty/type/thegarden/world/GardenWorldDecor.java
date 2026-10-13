package net.swofty.type.thegarden.world;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.tag.Tag;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.generic.entity.hologram.PlayerHolograms.ExternalPlayerHologram;
import net.swofty.type.skyblockgeneric.garden.GardenLayout;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.GardenProgress;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterConfig;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterEngine;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterService;
import net.swofty.type.skyblockgeneric.garden.composter.ComposterState;
import net.swofty.type.skyblockgeneric.gui.inventories.garden.GUIComposter;
import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorService;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorSlots;
import net.swofty.type.skyblockgeneric.gui.inventories.garden.GUIGardenDesk;
import net.swofty.type.skyblockgeneric.gui.inventories.garden.GUIVisitorLogbook;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.garden.GardenPresence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GardenWorldDecor implements GardenPresence {
    private static final Tag<Boolean> INTERACTIONS_SPAWNED = Tag.Boolean("garden_interactions_spawned");
    private static final Pos DESK = new Pos(-5.5, 71.34, -24.5);
    private static final Pos LOGBOOK = new Pos(7.5, 71.34, -22.5);
    private static final Pos COMPOSTER = new Pos(-11.5, 73.2, -27.3);
    private static final double CLICK_SPACING = 0.38;
    private static final double SIGN_SPACING = 0.375;
    private static final double SIGN_HEIGHT = 71.25;
    private static final String BLANK_LINE = " ";

    private final Map<UUID, List<ExternalPlayerHologram>> holograms = new ConcurrentHashMap<>();

    @Override
    public void arrived(SkyBlockPlayer player, SharedInstance instance) {
        VisitorService.tick(player);
        VisitorSlots.refresh(player.getUuid(), Gardens.profile(player).visitors());
        spawnInteractions(instance);
        departed(player);

        List<ExternalPlayerHologram> spawned = new ArrayList<>();
        spawned.add(clickHologram(player, instance, DESK, "§aDesk"));
        spawned.add(clickHologram(player, instance, LOGBOOK, "§aVisitor's Logbook"));
        spawned.add(composterHologram(player, instance));
        for (GardenLayout.PlotSign sign : GardenLayout.signs()) {
            spawned.add(signHologram(player, instance, sign));
        }
        spawned.forEach(PlayerHolograms::addExternalPlayerHologram);
        holograms.put(player.getUuid(), spawned);
    }

    @Override
    public void departed(SkyBlockPlayer player) {
        List<ExternalPlayerHologram> removed = holograms.remove(player.getUuid());
        if (removed != null) removed.forEach(PlayerHolograms::removeExternalPlayerHologram);
    }

    private static ExternalPlayerHologram clickHologram(SkyBlockPlayer player, Instance instance, Pos pos, String title) {
        return ExternalPlayerHologram.builder()
                .player(player)
                .instance(instance)
                .pos(pos)
                .text(new String[]{title, "§eClick to open!"})
                .spacing(CLICK_SPACING)
                .build();
    }

    private static ExternalPlayerHologram composterHologram(SkyBlockPlayer player, Instance instance) {
        return ExternalPlayerHologram.builder()
                .player(player)
                .instance(instance)
                .pos(COMPOSTER)
                .text(composterLines(player))
                .displayFunction(viewer -> composterLines((SkyBlockPlayer) viewer))
                .spacing(CLICK_SPACING)
                .build();
    }

    static String[] composterLines(SkyBlockPlayer player) {
        if (ComposterService.tutorialLocked(player)) {
            return new String[]{"§a§lCOMPOSTER", "§cProgress in Sam's quest in order to use this!"};
        }
        ComposterState state = ComposterService.state(player);
        ComposterConfig config = ComposterConfig.get();
        if (state.running()) {
            long seconds = (long) Math.ceil(state.getRemainingSeconds());
            return new String[]{"§a§lCOMPOSTER", String.format("§a%02d:%02d:%02d §funtil compost is ready!", seconds / 3600, seconds % 3600 / 60, seconds % 60)};
        }
        double fuelNeeded = ComposterEngine.fuelPerCycle(state, config) - state.getFuel();
        double matterNeeded = ComposterEngine.matterPerCycle(state, config) - state.getOrganicMatter();
        if (fuelNeeded > 0) {
            return new String[]{"§a§lCOMPOSTER", "§a" + StringUtility.commaify((long) Math.ceil(ComposterEngine.fuelPerCycle(state, config))) + " §cFuel required"};
        }
        if (matterNeeded > 0) {
            return new String[]{"§a§lCOMPOSTER", "§a" + StringUtility.commaify((long) Math.ceil(ComposterEngine.matterPerCycle(state, config))) + " §cOrganic matter required"};
        }
        return new String[]{"§a§lCOMPOSTER", "§eClick to open!"};
    }

    private static ExternalPlayerHologram signHologram(SkyBlockPlayer player, Instance instance, GardenLayout.PlotSign sign) {
        return ExternalPlayerHologram.builder()
                .player(player)
                .instance(instance)
                .pos(new Pos(sign.x(), SIGN_HEIGHT, sign.z()))
                .text(signLines(player, sign.plot()))
                .displayFunction(viewer -> signLines((SkyBlockPlayer) viewer, sign.plot()))
                .spacing(SIGN_SPACING)
                .build();
    }

    static String[] signLines(SkyBlockPlayer player, int plot) {
        GardenProfile profile = Gardens.profile(player);
        if (profile.isPlotUnlocked(plot)) {
            return new String[]{"§aPlot §7- §b" + plot, BLANK_LINE};
        }
        GardenProgress.PlotQuote quote = GardenProgress.quotePlot(profile, plot);
        String colour = GardenService.canAffordCompost(player, quote.compostCost()) ? "§a" : "§e";
        return new String[]{colour + "Plot §7- §b" + plot, "§7Cost: §aCompost §8x" + quote.compostCost()};
    }

    private static void placeComposterBlocks(SharedInstance instance) {
        place(instance, -12, 72, -28, net.minestom.server.instance.block.Block.BLAST_FURNACE.withProperty("facing", "south"));
        place(instance, -11, 72, -28, net.minestom.server.instance.block.Block.BLAST_FURNACE.withProperty("facing", "south"));
        place(instance, -12, 72, -27, net.minestom.server.instance.block.Block.CAULDRON);
        place(instance, -11, 72, -27, net.minestom.server.instance.block.Block.CAULDRON);
        for (int y = 73; y <= 75; y++) {
            place(instance, -12, y, -28, net.minestom.server.instance.block.Block.STONE_BRICK_WALL);
        }
    }

    private static void place(SharedInstance instance, int x, int y, int z, net.minestom.server.instance.block.Block block) {
        if (instance.getChunkAt(x, z) != null && instance.getBlock(x, y, z).isAir()) {
            instance.setBlock(x, y, z, block);
        }
    }

    private static void spawnInteractions(SharedInstance instance) {
        synchronized (INTERACTIONS_SPAWNED) {
            if (instance.hasTag(INTERACTIONS_SPAWNED)) return;
            instance.setTag(INTERACTIONS_SPAWNED, true);
        }
        InteractionEntity desk = new InteractionEntity(1.2f, 1.5f,
                (player, _) -> ((SkyBlockPlayer) player).openView(new GUIGardenDesk()));
        desk.setInstance(instance, new Pos(DESK.x(), 71, DESK.z()));

        placeComposterBlocks(instance);
        InteractionEntity composter = new InteractionEntity(1.8f, 2.2f, (player, _) -> {
            SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
            if (ComposterService.tutorialLocked(skyBlockPlayer)) {
                skyBlockPlayer.sendMessage(Text.of("<c>Progress in Sam's quest in order to use this!"));
                return;
            }
            skyBlockPlayer.openView(new GUIComposter());
        });
        composter.setInstance(instance, new Pos(COMPOSTER.x(), 71, COMPOSTER.z()));

        InteractionEntity logbook = new InteractionEntity(1.2f, 1.5f,
                (player, _) -> ((SkyBlockPlayer) player).openView(new GUIVisitorLogbook()));
        logbook.setInstance(instance, new Pos(LOGBOOK.x(), 71, LOGBOOK.z()));
    }
}
