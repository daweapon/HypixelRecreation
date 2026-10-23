package dev.daweapon.type.thefarmingislands.lab;

import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.farming.lab.CropAnalyzerAccess;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;

public final class LabFixtures {
    private static final List<Pos> COWS = List.of(
            new Pos(175.5, 29, -501.5, 0, 0),
            new Pos(170.5, 29, -501.5, 0, 0),
            new Pos(175.031, 27, -489.875, -146.25f, 0));
    private static final List<EntityCreature> cows = new ArrayList<>();
    private static InteractionEntity analyzer;

    private LabFixtures() {
    }

    public static synchronized void ensure(Instance instance) {
        if (analyzer == null || analyzer.isRemoved() || analyzer.getInstance() != instance) {
            analyzer = new InteractionEntity(1.4f, 1.2f, (player, _) -> CropAnalyzerAccess.open((SkyBlockPlayer) player));
            analyzer.setInstance(instance, LabScenes.CROP_ANALYZER);
        }
        cows.removeIf(cow -> cow.isRemoved() || cow.getInstance() != instance);
        for (int i = cows.size(); i < COWS.size(); i++) {
            EntityCreature cow = new EntityCreature(EntityType.COW);
            cow.setCustomName(Component.text("Dinnerbone"));
            cow.setInvulnerable(true);
            cow.setNoGravity(true);
            cow.setInstance(instance, COWS.get(i));
            cows.add(cow);
        }
    }
}
