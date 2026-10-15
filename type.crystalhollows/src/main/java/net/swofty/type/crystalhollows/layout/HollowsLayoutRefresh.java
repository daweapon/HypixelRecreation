package net.swofty.type.crystalhollows.layout;

import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.schematic.SchematicPaster;
import net.swofty.type.crystalhollows.temple.TempleDoor;
import org.tinylog.Logger;

import java.util.List;
import java.util.Random;

public final class HollowsLayoutRefresh {
    private HollowsLayoutRefresh() {
    }

    public static void run(Instance instance) {
        List<StructureSpec> specs = StructureCatalogue.load();
        CrystalHollowsLayout.generate(specs, new Random().nextLong());
        HollowsZoneRelocator.apply(specs, CrystalHollowsLayout.placements());
        if (instance == null) return;
        try {
            StructurePaster.Report report = StructurePaster.apply(instance, SchematicPaster.CALLER).join();
            report.failures().forEach(failure -> Logger.warn("Crystal Hollows structure paste: {}", failure));
            TempleDoor.reset(instance);
        } catch (RuntimeException e) {
            Logger.error(e, "Crystal Hollows structure paste failed");
        }
    }
}
