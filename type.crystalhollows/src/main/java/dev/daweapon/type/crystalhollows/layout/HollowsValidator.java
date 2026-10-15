package dev.daweapon.type.crystalhollows.layout;

import dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusCrystal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HollowsValidator {
    public enum Level {
        ERROR, WARNING
    }

    public record Issue(Level level, String message) {
    }

    public record Input(List<StructureSpec> structures, List<HollowsAnchors.Ref> references, Set<NucleusCrystal> statues,
                        Map<String, HollowsRegions.Box> regions, HollowsWorld world, Set<String> placed) {
    }

    private HollowsValidator() {
    }

    public static List<Issue> validateSchematics(List<StructureSpec> structures, Path directory) {
        List<Issue> issues = new ArrayList<>();
        for (StructureSpec spec : structures) {
            StructureSite site = spec.site();
            if (site != null && spec.emptyPatch() == null) {
                issues.add(new Issue(Level.WARNING, "Structure " + spec.id() + " has a site but no empty_patch, so the captured building stays when it is placed elsewhere"));
            }
            if (spec.emptyPatch() != null && site == null) {
                issues.add(new Issue(Level.ERROR, "Structure " + spec.id() + " has an empty_patch but no site to paste it over"));
            }
            for (int rotation : spec.rotations()) {
                if (rotation % 90 != 0) issues.add(new Issue(Level.ERROR, "Structure " + spec.id() + " has an invalid rotation " + rotation));
            }
            if (!spec.rotations().isEmpty() && !spec.anchors().isEmpty() && spec.rotations().stream().anyMatch(r -> Math.floorMod(r, 360) != 0)) {
                issues.add(new Issue(Level.WARNING, "Structure " + spec.id() + " rotates its schematic but its anchors are not rotated"));
            }
            if (spec.schematic() != null) check(issues, spec, "schematic", spec.schematic(), directory, null);
            if (spec.emptyPatch() != null) check(issues, spec, "empty_patch", spec.emptyPatch(), directory, site);
        }
        return issues;
    }

    private static void check(List<Issue> issues, StructureSpec spec, String role, String file, Path directory, StructureSite site) {
        SchematicFiles.Probe probe = SchematicFiles.probe(directory, file);
        String label = "Structure " + spec.id() + " " + role + " " + file;
        if (!probe.exists()) {
            issues.add(new Issue(Level.ERROR, label + ": " + probe.error()));
        } else if (!probe.readable()) {
            issues.add(new Issue(Level.ERROR, label + " is unreadable: " + probe.error()));
        } else {
            if (probe.schematic().unknownBlocks() > 0) {
                issues.add(new Issue(Level.WARNING, label + " has " + probe.schematic().unknownBlocks() + " unknown blocks (skipped when pasting)"));
            }
            if (site != null && (probe.schematic().width() != site.width() || probe.schematic().height() != site.height()
                    || probe.schematic().length() != site.length())) {
                issues.add(new Issue(Level.WARNING, label + " is " + probe.schematic().width() + "x" + probe.schematic().height() + "x"
                        + probe.schematic().length() + " but the site is " + site.width() + "x" + site.height() + "x" + site.length()));
            }
        }
    }

    public static List<Issue> validate(Input input) {
        List<Issue> issues = new ArrayList<>();
        Map<String, StructureSpec> byId = new java.util.LinkedHashMap<>();
        for (StructureSpec spec : input.structures()) {
            byId.put(spec.id(), spec);
            if (spec.candidates().isEmpty()) {
                issues.add(new Issue(spec.optional() ? Level.WARNING : Level.ERROR,
                        "Structure " + spec.id() + (spec.optional() ? " (optional)" : "") + " has no candidates"));
            } else if (!input.placed().contains(spec.id()) && !spec.optional()) {
                issues.add(new Issue(Level.ERROR, "Structure " + spec.id() + " has candidates but was not placed (clearance clash)"));
            }
        }
        Set<String> reported = new LinkedHashSet<>();
        for (HollowsAnchors.Ref ref : input.references()) {
            StructureSpec spec = byId.get(ref.structure());
            String key = ref.structure() + "/" + ref.anchor();
            if (!reported.add(key)) continue;
            if (spec == null) {
                issues.add(new Issue(Level.ERROR, "Structure " + ref.structure() + " is missing from structures.yml (needed by " + ref.user() + ")"));
            } else if (!spec.anchors().containsKey(ref.anchor())) {
                issues.add(new Issue(ref.fallsBack() ? Level.WARNING : Level.ERROR,
                        "Anchor " + key + " is not set (needed by " + ref.user() + ")"));
            }
        }
        for (NucleusCrystal crystal : NucleusCrystal.values()) {
            if (!input.statues().contains(crystal)) {
                issues.add(new Issue(Level.ERROR, "Nucleus statue for " + crystal.name() + " is not set"));
            }
        }
        HollowsRegions.zones().keySet().forEach(id -> {
            HollowsRegions.Box box = input.regions().get(id);
            if (box == null) {
                issues.add(new Issue(Level.WARNING, "Region " + id + " is not defined"));
            } else if (HollowsRegions.isPlaceholder(id, box)) {
                issues.add(new Issue(Level.WARNING, "Region " + id + " is still the placeholder box"));
            }
        });
        if (input.world().spawn() == null) {
            issues.add(new Issue(Level.ERROR, "Spawn is unset (using the placeholder " + HollowsWorld.PLACEHOLDER_SPAWN + ")"));
        }
        if (input.world().bounds() == null) {
            issues.add(new Issue(Level.WARNING, "World bounds are unset"));
        }
        return issues;
    }
}
