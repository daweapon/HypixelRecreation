package dev.daweapon.type.skyblockgeneric.command;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.command.commands.DebugCommand;
import dev.daweapon.type.generic.command.commands.TeleportCommand;
import dev.daweapon.type.generic.command.commands.WorldCommand;
import dev.daweapon.type.generic.command.help.HelpCatalog;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.commands.ForceCommand;
import dev.daweapon.type.skyblockgeneric.commands.HandCommand;
import dev.daweapon.type.skyblockgeneric.commands.MaxCommand;
import dev.daweapon.type.skyblockgeneric.commands.MissionCommand;
import dev.daweapon.type.skyblockgeneric.commands.ResetCommand;
import dev.daweapon.type.skyblockgeneric.commands.SetCommand;
import dev.daweapon.type.skyblockgeneric.commands.SkyBlockXPCommand;
import dev.daweapon.type.skyblockgeneric.commands.SpawnCommand;
import dev.daweapon.type.skyblockgeneric.commands.UnlockCommand;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandMergeTest {
    private static final Map<String, String> OLD_LABELS = Map.ofEntries(
            Map.entry("coins", "set coins"),
            Map.entry("bits", "set bits"),
            Map.entry("bit", "set bits"),
            Map.entry("xp", "set xp"),
            Map.entry("experience", "set xp"),
            Map.entry("setessence", "set essence"),
            Map.entry("setreputation", "set reputation"),
            Map.entry("setrep", "set reputation"),
            Map.entry("setpetlevel", "set petlevel"),
            Map.entry("updatepetlevel", "set petlevel"),
            Map.entry("setpetrarity", "set petrarity"),
            Map.entry("updatebestiary", "set bestiary"),
            Map.entry("updatecollection", "set collection"),
            Map.entry("setstat", "set stat"),
            Map.entry("setstatistic", "set stat"),
            Map.entry("forcedrop", "force drop"),
            Map.entry("forcedianamob", "force diana"),
            Map.entry("dianamob", "force diana"),
            Map.entry("forcedarkauction", "force darkauction"),
            Map.entry("forceeruption", "force eruption"),
            Map.entry("unlockwarps", "unlock warps"),
            Map.entry("unlockbestiary", "unlock bestiary"),
            Map.entry("resetchickenrace", "reset chickenrace"),
            Map.entry("resetmystocksnow", "reset stocks"),
            Map.entry("maxprofile", "max profile"),
            Map.entry("missiondata", "mission info"),
            Map.entry("displaymissiondata", "mission data"),
            Map.entry("startmission", "mission start"),
            Map.entry("clearmission", "mission clear"),
            Map.entry("clearmissionset", "mission clearset"),
            Map.entry("completemissionset", "mission clearset"),
            Map.entry("enchant", "hand enchant"),
            Map.entry("ench", "hand enchant"),
            Map.entry("reforge", "hand reforge"),
            Map.entry("reforgeitem", "hand reforge"),
            Map.entry("recomb", "hand recomb"),
            Map.entry("mithrilinfuse", "hand mithrilinfuse"),
            Map.entry("nbt", "hand nbt"),
            Map.entry("iteminfo", "hand nbt"),
            Map.entry("editnbt", "hand setnbt"),
            Map.entry("itemtrackedinformation", "hand tracked"),
            Map.entry("formatname", "debug formatname"),
            Map.entry("getnameformatted", "debug formatname"),
            Map.entry("localedata", "debug localedata"),
            Map.entry("npcpathfinding", "debug npcpathfinding"),
            Map.entry("explodesimulation", "debug explosion"),
            Map.entry("signgui", "debug signgui"),
            Map.entry("signgraphicaluserinterface", "debug signgui"),
            Map.entry("translatablecomponent", "debug component"),
            Map.entry("displayconfigids", "debug configids"),
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
            Map.entry("playsong", "debug music"),
            Map.entry("dragon", "world dragon"),
            Map.entry("setvelocity", "world velocity"),
            Map.entry("structure", "world structure"),
            Map.entry("tree", "world tree"),
            Map.entry("regrowtrees", "world regrow"),
            Map.entry("spawnholo", "world hologram"),
            Map.entry("spawnhologram", "world hologram"),
            Map.entry("spawncrystal", "world crystal"),
            Map.entry("addcrystal", "world crystal"),
            Map.entry("deleteorb", "world deleteorb"),
            Map.entry("handleminionspeed", "world minionspeed"),
            Map.entry("tpnpc", "tp npc"),
            Map.entry("teleportnpc", "tp npc"),
            Map.entry("giveskyblockxp", "skyblockxp cause"),
            Map.entry("awardskyblockxp", "skyblockxp cause"),
            Map.entry("spawnseacreature", "spawn"),
            Map.entry("spawnsc", "spawn"),
            Map.entry("spawncrimsonmob", "spawn"),
            Map.entry("spawncm", "spawn"));

    private static final List<HypixelCommand> TOP_LEVEL = new ArrayList<>();

    @BeforeAll
    static void registerEverything() {
        MinecraftServer.init();
        TOP_LEVEL.addAll(List.of(new DebugCommand(), new WorldCommand(), new TeleportCommand(), new SetCommand(),
                new ForceCommand(), new UnlockCommand(), new ResetCommand(), new MaxCommand(), new MissionCommand(),
                new HandCommand(), new SkyBlockXPCommand(), new SpawnCommand()));
        TOP_LEVEL.forEach(HypixelCommand::register);
        SkyBlockCommandGroups.register();
    }

    @AfterAll
    static void unregisterEverything() {
        var manager = MinecraftServer.getCommandManager();
        TOP_LEVEL.forEach(command -> {
            manager.unregister(command.getCommand());
            command.legacyCommands().forEach(manager::unregister);
        });
    }

    @Test
    void everyOldCommandNameStillResolvesToTheRightNewHandler() {
        var manager = MinecraftServer.getCommandManager();
        OLD_LABELS.forEach((oldLabel, target) -> {
            Command legacy = manager.getCommand(oldLabel);
            assertNotNull(legacy, "/" + oldLabel + " is no longer registered");
            String[] words = target.split(" ");
            Command group = manager.getCommand(words[0]);
            assertNotNull(group, "/" + oldLabel + " forwards to missing /" + words[0]);
            if (words.length > 1) {
                assertTrue(group.getSubcommands().stream().anyMatch(sub -> sub.getName().equals(words[1])),
                        "/" + oldLabel + " forwards to missing subcommand " + target);
            }
        });
    }

    @Test
    void oldNamesKeepTheirPermissionAndStayOutOfHelp() {
        var manager = MinecraftServer.getCommandManager();
        var visible = HelpCatalog.entries(manager.getCommands(), Rank.STAFF, false).stream()
                .flatMap(entry -> entry.labels().stream()).toList();
        OLD_LABELS.forEach((oldLabel, target) -> {
            Command legacy = manager.getCommand(oldLabel);
            Command group = manager.getCommand(target.split(" ")[0]);
            if (legacy != group) {
                assertEquals(group.getCondition(), legacy.getCondition(), "/" + oldLabel + " must share its group's permission");
                assertTrue(!visible.contains(oldLabel), "/" + oldLabel + " should be hidden from /help");
            }
        });
    }
}
