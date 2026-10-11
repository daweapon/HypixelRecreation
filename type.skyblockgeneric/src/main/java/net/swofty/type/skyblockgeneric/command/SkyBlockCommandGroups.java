package net.swofty.type.skyblockgeneric.command;

import net.swofty.type.generic.command.CommandGroup;
import net.swofty.type.skyblockgeneric.command.groups.debug.AnvilGUICommand;
import net.swofty.type.skyblockgeneric.command.groups.debug.DebugDisplayDICommand;
import net.swofty.type.skyblockgeneric.command.groups.debug.HowManyTasksCommand;
import net.swofty.type.skyblockgeneric.command.groups.debug.LagProfileCommand;
import net.swofty.type.skyblockgeneric.command.groups.debug.MiningDebugCommand;
import net.swofty.type.skyblockgeneric.command.groups.debug.PlayMusicCommand;
import net.swofty.type.skyblockgeneric.command.groups.debug.PrintConfigIdsCommand;
import net.swofty.type.skyblockgeneric.command.groups.debug.RecipeDebugCommand;
import net.swofty.type.skyblockgeneric.command.groups.debug.TestHeadRotation;
import net.swofty.type.skyblockgeneric.command.groups.debug.TestMutexCommand;
import net.swofty.type.skyblockgeneric.command.groups.debug.TestParticleEngineCommand;
import net.swofty.type.skyblockgeneric.command.groups.world.AddCrystalCommand;
import net.swofty.type.skyblockgeneric.command.groups.world.GenerateTreeCommand;
import net.swofty.type.skyblockgeneric.command.groups.world.MinionGenerationCommand;
import net.swofty.type.skyblockgeneric.command.groups.world.PlaceStructureCommand;
import net.swofty.type.skyblockgeneric.command.groups.world.RemoveOrbCommand;
import net.swofty.type.skyblockgeneric.command.groups.world.RespawnTreesInAreaCommand;
import net.swofty.type.skyblockgeneric.command.groups.world.SpawnHologramCommand;

import java.util.List;
import java.util.Map;

public final class SkyBlockCommandGroups {
    private SkyBlockCommandGroups() {}

    public static void register() {
        CommandGroup.extend("debug",
                List.of(new PrintConfigIdsCommand(),
                new DebugDisplayDICommand(),
                new LagProfileCommand(),
                new MiningDebugCommand(),
                new RecipeDebugCommand(),
                new HowManyTasksCommand(),
                new TestHeadRotation(),
                new TestMutexCommand(),
                new TestParticleEngineCommand(),
                new AnvilGUICommand(),
                new PlayMusicCommand()),
                Map.ofEntries(Map.entry("displayconfigids", "debug configids"),
                Map.entry("dmgindicdisplaydebugcmd", "debug damageindicator"),
                Map.entry("lagprofile", "debug lag"),
                Map.entry("mininginfo", "debug mining"),
                Map.entry("recipedebug", "debug recipes"),
                Map.entry("taskcount", "debug tasks"),
                Map.entry("testheadrotation", "debug headrotation"),
                Map.entry("testheadrot", "debug headrotation"),
                Map.entry("testmutex", "debug mutex"),
                Map.entry("testpe", "debug particles"),
                Map.entry("testparticleengine", "debug particles"),
                Map.entry("anvilgui", "debug anvilgui"),
                Map.entry("anvilgraphicaluserinterface", "debug anvilgui"),
                Map.entry("playmusic", "debug music"),
                Map.entry("playsong", "debug music")));
        CommandGroup.extend("world",
                List.of(new PlaceStructureCommand(),
                new GenerateTreeCommand(),
                new RespawnTreesInAreaCommand(),
                new SpawnHologramCommand(),
                new AddCrystalCommand(),
                new RemoveOrbCommand(),
                new MinionGenerationCommand()),
                Map.ofEntries(Map.entry("structure", "world structure"),
                Map.entry("tree", "world tree"),
                Map.entry("regrowtrees", "world regrow"),
                Map.entry("spawnholo", "world hologram"),
                Map.entry("spawnhologram", "world hologram"),
                Map.entry("spawncrystal", "world crystal"),
                Map.entry("addcrystal", "world crystal"),
                Map.entry("deleteorb", "world deleteorb"),
                Map.entry("handleminionspeed", "world minionspeed")));
    }
}
