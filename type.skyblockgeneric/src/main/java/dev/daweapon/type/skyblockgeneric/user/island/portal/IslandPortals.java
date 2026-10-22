package dev.daweapon.type.skyblockgeneric.user.island.portal;

import dev.daweapon.type.generic.entity.hologram.ServerHolograms;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IslandPortals {
    public static final String DATABASE_KEY = "portals";
    public static final PlacedPortal LEGACY_HUB_PORTAL = new PlacedPortal(PortalCatalog.HUB_ITEM_ID, 6, 100, 43, 0);

    private final List<PlacedPortal> portals = new ArrayList<>();
    private final Map<PlacedPortal, ServerHolograms.ExternalHologram> holograms = new HashMap<>();

    public static IslandPortals deserialize(@Nullable String raw) {
        IslandPortals loaded = new IslandPortals();
        if (raw == null) {
            loaded.portals.add(LEGACY_HUB_PORTAL);
            return loaded;
        }
        if (raw.isBlank()) return loaded;
        for (String entry : raw.split(";")) {
            PlacedPortal portal = PlacedPortal.deserialize(entry);
            if (PortalCatalog.get(portal.itemId()) != null) loaded.portals.add(portal);
        }
        return loaded;
    }

    public String serialize() {
        return portals.stream().map(PlacedPortal::serialize).collect(Collectors.joining(";"));
    }

    public List<PlacedPortal> all() {
        return List.copyOf(portals);
    }

    public void spawnAll(Instance instance) {
        portals.forEach(portal -> spawn(instance, portal));
    }

    public void add(Instance instance, PlacedPortal portal) {
        portals.add(portal);
        spawn(instance, portal);
    }

    public void remove(Instance instance, PlacedPortal portal) {
        if (!portals.remove(portal)) return;
        PortalShape.clear(instance, portal);
        ServerHolograms.ExternalHologram hologram = holograms.remove(portal);
        if (hologram != null) ServerHolograms.removeExternalHologram(hologram);
    }

    public void despawnHolograms() {
        holograms.values().forEach(ServerHolograms::removeExternalHologram);
        holograms.clear();
    }

    public @Nullable PlacedPortal portalAt(int x, int y, int z) {
        return portals.stream().filter(portal -> PortalShape.isPartOf(portal, x, y, z)).findFirst().orElse(null);
    }

    public @Nullable PlacedPortal activePortalAt(int x, int y, int z) {
        return portals.stream().filter(portal -> PortalShape.isPortalPlane(portal, x, y, z)).findFirst().orElse(null);
    }

    private void spawn(Instance instance, PlacedPortal portal) {
        PortalShape.build(instance, portal);
        PortalDestination destination = PortalCatalog.get(portal.itemId());
        ServerHolograms.ExternalHologram hologram = portal.hologram(instance, destination);
        ServerHolograms.addExternalHologram(hologram);
        holograms.put(portal, hologram);
    }
}
