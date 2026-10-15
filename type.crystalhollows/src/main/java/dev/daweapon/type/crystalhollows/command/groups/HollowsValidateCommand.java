package dev.daweapon.type.crystalhollows.command.groups;

import dev.daweapon.type.crystalhollows.command.HollowsSupport;
import dev.daweapon.type.crystalhollows.layout.CrystalHollowsLayout;
import dev.daweapon.type.crystalhollows.layout.HollowsRegions;
import dev.daweapon.type.crystalhollows.layout.HollowsValidator;
import dev.daweapon.type.crystalhollows.layout.HollowsWorld;
import dev.daweapon.type.crystalhollows.layout.SchematicFiles;
import dev.daweapon.type.crystalhollows.layout.StructureCatalogue;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusConfig;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandParameters(description = "Reports everything still missing from the Crystal Hollows configuration",
        labels = "validate",
        usage = "/hollows validate",
        permission = Rank.STAFF,
        allowsConsole = false)
public class HollowsValidateCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            HollowsSupport.reloadLayout(CrystalHollowsLayout.seed());
            List<HollowsValidator.Issue> issues = new java.util.ArrayList<>(HollowsValidator.validate(new HollowsValidator.Input(
                    StructureCatalogue.load(), HollowsSupport.references(), NucleusConfig.get().statues().keySet(),
                    regionBoxes(), HollowsWorld.get(), CrystalHollowsLayout.placements().keySet())));
            issues.addAll(HollowsValidator.validateSchematics(StructureCatalogue.load(), SchematicFiles.DIRECTORY));
            if (issues.isEmpty()) {
                player.sendMessage("<a>Crystal Hollows configuration is complete.");
                return;
            }
            long errors = issues.stream().filter(issue -> issue.level() == HollowsValidator.Level.ERROR).count();
            player.sendMessage("<e>{} errors, {} warnings:", errors, issues.size() - errors);
            for (HollowsValidator.Issue issue : issues) {
                player.sendMessage((issue.level() == HollowsValidator.Level.ERROR ? "<c>ERROR " : "<6>WARN ") + "<7>{}", issue.message());
            }
        });
    }

    private static Map<String, HollowsRegions.Box> regionBoxes() {
        Map<String, HollowsRegions.Box> boxes = new HashMap<>();
        for (String id : HollowsRegions.ids()) {
            SkyBlockRegion region = SkyBlockRegion.getFromID(id);
            if (region == null) continue;
            boxes.put(id, new HollowsRegions.Box(region.getFirstLocation().blockX(), region.getFirstLocation().blockZ(),
                    region.getSecondLocation().blockX(), region.getSecondLocation().blockZ()).normalised());
        }
        return boxes;
    }
}
