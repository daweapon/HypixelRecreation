package dev.daweapon.type.crystalhollows.layout;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.schematic.Schematic;
import dev.daweapon.type.skyblockgeneric.schematic.SchematicPaster;
import org.tinylog.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public final class StructurePaster {
    public record Report(List<String> pasted, List<String> erased, List<String> failures, int unknownBlocks) {
    }

    private record Undo(Point min, Schematic snapshot) {
    }

    private static final List<Undo> APPLIED = new ArrayList<>();

    private StructurePaster() {
    }

    public static CompletableFuture<Report> apply(Instance instance, Executor executor) {
        return apply(instance, executor, StructureCatalogue.load(), CrystalHollowsLayout.placements(), SchematicFiles.DIRECTORY, CrystalHollowsLayout.seed());
    }

    public static synchronized CompletableFuture<Report> apply(Instance instance, Executor executor, List<StructureSpec> specs,
                                                               java.util.Map<String, StructurePlacement> placements, Path directory, long seed) {
        List<String> pasted = new ArrayList<>();
        List<String> erased = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        int[] unknown = {0};
        List<Undo> previous = new ArrayList<>(APPLIED);
        APPLIED.clear();

        List<Supplier<CompletableFuture<?>>> steps = new ArrayList<>();
        for (int i = previous.size() - 1; i >= 0; i--) {
            Undo undo = previous.get(i);
            steps.add(() -> SchematicPaster.paste(instance, undo.min(), undo.snapshot(), SchematicPaster.Options.defaults(), executor)
                    .exceptionally(error -> {
                        failures.add("restore at " + undo.min() + ": " + error.getMessage());
                        return null;
                    }));
        }
        for (StructureSpec spec : specs) {
            StructurePlacement placement = placements.get(spec.id());
            StructureSite site = spec.site();
            if (spec.emptyPatch() != null && site != null && (placement == null || !site.startsAt(placement.origin()))) {
                steps.add(() -> paste(instance, executor, directory, spec.id(), spec.emptyPatch(), site.min(), 0, erased, failures, unknown));
            }
        }
        for (StructureSpec spec : specs) {
            StructurePlacement placement = placements.get(spec.id());
            if (spec.schematic() == null || placement == null) continue;
            Point origin = new Vec(placement.origin().blockX(), placement.origin().blockY(), placement.origin().blockZ());
            int rotation = rotationFor(spec, seed);
            steps.add(() -> paste(instance, executor, directory, spec.id(), spec.schematic(), origin, rotation, pasted, failures, unknown));
        }

        CompletableFuture<?> chain = CompletableFuture.completedFuture(null);
        for (Supplier<CompletableFuture<?>> step : steps) chain = chain.thenCompose(_ -> step.get());
        return chain.thenApply(_ -> {
            Logger.info("Crystal Hollows structures: pasted {}, erased {}, failures {}", pasted, erased, failures);
            return new Report(pasted, erased, failures, unknown[0]);
        });
    }

    public static int rotationFor(StructureSpec spec, long seed) {
        if (spec.rotations().isEmpty()) return 0;
        return spec.rotations().get(new Random(seed * 31 + spec.id().hashCode()).nextInt(spec.rotations().size()));
    }

    public static CompletableFuture<SchematicPaster.Result> pasteFile(Instance instance, Executor executor, Path directory,
                                                                      String file, Point origin, int rotation) {
        SchematicFiles.Probe probe = SchematicFiles.probe(directory, file);
        if (!probe.readable()) {
            return CompletableFuture.failedFuture(new IllegalStateException(file + ": " + probe.error()));
        }
        return SchematicPaster.paste(instance, origin, probe.schematic(), SchematicPaster.Options.defaults().withRotation(rotation), executor);
    }

    private static CompletableFuture<?> paste(Instance instance, Executor executor, Path directory, String structure, String file,
                                              Point origin, int rotation, List<String> done, List<String> failures, int[] unknown) {
        SchematicFiles.Probe probe = SchematicFiles.probe(directory, file);
        if (!probe.readable()) {
            failures.add(structure + " " + file + ": " + probe.error());
            return CompletableFuture.completedFuture(null);
        }
        SchematicPaster.Options options = SchematicPaster.Options.defaults().withRotation(rotation).withUndo(true);
        return SchematicPaster.paste(instance, origin, probe.schematic(), options, executor).handle((result, error) -> {
            if (error != null) {
                failures.add(structure + " " + file + ": " + error.getMessage());
                return null;
            }
            synchronized (StructurePaster.class) {
                APPLIED.add(new Undo(origin, result.undo()));
            }
            unknown[0] += result.unknown();
            done.add(structure);
            return null;
        });
    }
}
