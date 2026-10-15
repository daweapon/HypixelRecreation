package dev.daweapon.type.crystalhollows.command;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crystalhollows.chest.StructureChests;
import dev.daweapon.type.crystalhollows.claim.ClaimSpots;
import dev.daweapon.type.crystalhollows.claim.CrystalDisplays;
import dev.daweapon.type.crystalhollows.layout.CrystalHollowsLayout;
import dev.daweapon.type.crystalhollows.layout.HollowsAnchors;
import dev.daweapon.type.crystalhollows.layout.HollowsWorld;
import dev.daweapon.type.crystalhollows.layout.StructureCatalogue;
import dev.daweapon.type.crystalhollows.layout.StructureFile;
import dev.daweapon.type.crystalhollows.layout.StructureSpec;
import dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusConfig;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class HollowsSupport {
    private HollowsSupport() {
    }

    public static ArgumentWord suggesting(String name, Supplier<List<String>> options) {
        ArgumentWord argument = ArgumentType.Word(name);
        argument.setSuggestionCallback((_, context, suggestion) -> {
            String typed = context.getInput().substring(context.getInput().lastIndexOf(' ') + 1).toLowerCase(Locale.ROOT);
            for (String option : options.get()) {
                if (option.toLowerCase(Locale.ROOT).startsWith(typed)) suggestion.addEntry(new SuggestionEntry(option));
            }
        });
        return argument;
    }

    public static ArgumentWord structureArgument(String name) {
        return suggesting(name, () -> StructureCatalogue.load().stream().map(StructureSpec::id).toList());
    }

    public static List<HollowsAnchors.Ref> references() {
        List<HollowsAnchors.Ref> refs = new ArrayList<>(HollowsAnchors.all());
        StructureChests.specs().forEach(chest -> refs.add(
                new HollowsAnchors.Ref(chest.structure(), chest.anchor(), "chest " + chest.id(), false)));
        return refs;
    }

    public static void edit(Consumer<StructureFile> change) {
        StructureFile file = StructureFile.read(StructureCatalogue.FILE);
        change.accept(file);
        file.write(StructureCatalogue.FILE);
        reloadLayout(CrystalHollowsLayout.seed());
    }

    public static void reloadLayout(long seed) {
        StructureChests.reload();
        NucleusConfig.reload();
        ClaimSpots.reload();
        CrystalDisplays.reset();
        HollowsWorld.reload();
        CrystalHollowsLayout.generate(StructureCatalogue.load(), seed);
    }

    public static Pos rounded(Pos pos) {
        return new Pos(round(pos.x(), 100), round(pos.y(), 100), round(pos.z(), 100),
                (float) round(pos.yaw(), 10), (float) round(pos.pitch(), 10));
    }

    private static double round(double value, int scale) {
        return Math.round(value * scale) / (double) scale;
    }

    public static Pos offset(Pos origin, Pos position) {
        return rounded(new Pos(position.x() - origin.x(), position.y() - origin.y(), position.z() - origin.z(),
                position.yaw(), position.pitch()));
    }

    public static Point targetBlock(SkyBlockPlayer player) {
        Point target = player.getTargetBlockPosition(8);
        if (target != null) return target;
        Pos feet = player.getPosition();
        return new Pos(feet.blockX(), feet.blockY(), feet.blockZ());
    }
}
