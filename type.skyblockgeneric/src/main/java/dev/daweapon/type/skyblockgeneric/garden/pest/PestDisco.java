package dev.daweapon.type.skyblockgeneric.garden.pest;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.garden.GardenLayout;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public final class PestDisco {
    public static final String HANDLER = "BUZZYBEES_FANTABULOUS_DISCO_DESTINATION";
    public static final double SPAWN_RADIUS = 10;

    private PestDisco() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register(HANDLER, InteractableItemConfig.builder()
                .rightClickHandler(PestDisco::place).build());
    }

    public static double[] spawnPoint(PestData.Disco disco, double angle, double radiusFraction) {
        double radius = SPAWN_RADIUS * Math.sqrt(Math.max(0, Math.min(1, radiusFraction)));
        return new double[]{disco.getX() + Math.cos(angle) * radius, disco.getZ() + Math.sin(angle) * radius};
    }

    public static PestData.Alive spawnNear(PestData.Disco disco, String pestType) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double[] at = spawnPoint(disco, random.nextDouble() * Math.PI * 2, random.nextDouble());
        return PestData.Alive.createAt(pestType, disco.getPlot(), at[0], at[1]);
    }

    public static boolean attracts(PestData data, String pestId) {
        return data.getDisco() != null && data.getDisco().getVinyls().contains(pestId);
    }

    private static void place(SkyBlockPlayer player, SkyBlockItem item) {
        if (!PestVacuum.inGarden(player)) {
            player.sendMessage(Text.of("<c>You can only place this in your Garden!"));
            return;
        }
        GardenProfile profile = Gardens.profile(player);
        PestData data = profile.pests();
        if (data.getDisco() != null) {
            player.sendMessage(Text.of("<c>You can only have one Buzzybee's Fantabulous Disco Destination placed at a time!"));
            return;
        }
        Point target = player.getTargetBlockPosition(6);
        if (target == null) return;
        int plot = GardenLayout.plotAt(target.x(), target.z());
        if (plot == GardenLayout.NONE) {
            player.sendMessage(Text.of("<c>You can't place this here!"));
            return;
        }
        TimedConsumables.consumeHeld(player, item);
        data.setDisco(new PestData.Disco(plot, target.x() + 0.5, target.y() + 1, target.z() + 0.5));
        Gardens.save(player, profile);
        PestWorld.reconcileDisco(player.getInstance(), data);
        player.sendMessage(Text.of("<a>Placed Buzzybee's Fantabulous Disco Destination<a>! Right-click it with a Pest Vinyl to insert it."));
    }

    public static void interact(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        PestData data = profile.pests();
        PestData.Disco disco = data.getDisco();
        if (disco == null) return;
        if (player.isSneaking()) {
            pickUp(player, profile, disco);
            return;
        }
        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        String pest = held.isNA() ? null : PestVinyls.pestOf(held.getAttributeHandler().getTypeAsString());
        if (pest != null) {
            if (!disco.getVinyls().add(pest)) {
                player.sendMessage(Text.of("<c>That Pest Vinyl is already inside!"));
                return;
            }
            TimedConsumables.consumeHeld(player, held);
            Gardens.save(player, profile);
            player.sendMessage(Text.of("<a>Inserted the {}<a>. Offline <2>{} Pests <a>will spawn within {} blocks of it.",
                    held.getDisplayName(), PestCatalogue.get().pest(pest).name(), (int) SPAWN_RADIUS));
            return;
        }
        player.sendMessage(Text.of("<7>Inserted vinyls: {}<7>. <8>Right-click with a Pest Vinyl to insert, sneak-click to pick up.",
                Text.parse(disco.getVinyls().isEmpty() ? "<c>None" : "<a>" + String.join("<7>, <a>", disco.getVinyls().stream()
                        .map(id -> PestCatalogue.get().pest(id).name()).toList()))));
    }

    private static void pickUp(SkyBlockPlayer player, GardenProfile profile, PestData.Disco disco) {
        for (String pest : disco.getVinyls()) {
            ItemType vinyl = ItemType.get(PestVinyls.vinylOf(pest));
            if (vinyl != null) PestRewards.give(player, new SkyBlockItem(vinyl));
        }
        profile.pests().setDisco(null);
        Gardens.save(player, profile);
        PestWorld.reconcileDisco(player.getInstance(), profile.pests());
        player.addAndUpdateItem(new SkyBlockItem(ItemType.BUZZYBEES_FANTABULOUS_DISCO_DESTINATION));
        player.sendMessage(Text.of("<a>You picked up Buzzybee's Fantabulous Disco Destination<a>."));
    }

    public static void emitNotes(Instance instance, PestData data) {
        PestData.Disco disco = data.getDisco();
        if (disco == null || disco.getVinyls().isEmpty()) return;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (Player player : instance.getPlayers()) {
            player.sendPacket(new ParticlePacket(Particle.NOTE, false, false, (float) disco.getX(), (float) (disco.getY() + 1.2),
                    (float) disco.getZ(), 0.4f, 0.3f, 0.4f, 1f, 3 + random.nextInt(3)));
        }
    }
}
