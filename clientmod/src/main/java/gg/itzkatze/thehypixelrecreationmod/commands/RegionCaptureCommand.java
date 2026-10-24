package gg.itzkatze.thehypixelrecreationmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import gg.itzkatze.thehypixelrecreationmod.features.regioncapture.RegionCapture;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public final class RegionCaptureCommand {
    private static final String DEFAULT_SERVER_TYPE = "SKYBLOCK_SPIDERS_DEN";

    private RegionCaptureCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> {
            dispatcher.register(tree());
            RecreationCommand.register(dispatcher, tree());
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> tree() {
        return ClientCommands.literal("regioncapture")
                .executes(RegionCaptureCommand::status)
                .then(ClientCommands.literal("start")
                        .executes(context -> start(context, "regions", DEFAULT_SERVER_TYPE))
                        .then(ClientCommands.argument("name", StringArgumentType.word())
                                .executes(context -> start(context, StringArgumentType.getString(context, "name"), DEFAULT_SERVER_TYPE))
                                .then(ClientCommands.argument("serverType", StringArgumentType.word())
                                        .executes(context -> start(context, StringArgumentType.getString(context, "name"),
                                                StringArgumentType.getString(context, "serverType"))))))
                .then(ClientCommands.literal("stop").executes(context -> finish(context, true)))
                .then(ClientCommands.literal("export").executes(context -> finish(context, false)))
                .then(ClientCommands.literal("status").executes(RegionCaptureCommand::status))
                .then(ClientCommands.literal("mark")
                        .then(ClientCommands.argument("text", StringArgumentType.greedyString())
                                .executes(context -> mark(context, StringArgumentType.getString(context, "text")))));
    }

    private static int start(CommandContext<FabricClientCommandSource> context, String name, String serverType) {
        try {
            RegionCapture.start(name, serverType);
            feedback(context, "§aRegion capture started. Walk the edges of every zone, then run §f/regioncapture stop§a.");
            feedback(context, "§7The zone is read from the sidebar line with the location symbol. §f/regioncapture mark <text>§7 saves your spot.");
            return 1;
        } catch (IllegalStateException exception) {
            feedback(context, "§c" + exception.getMessage());
            return 0;
        }
    }

    private static int finish(CommandContext<FabricClientCommandSource> context, boolean stop) {
        try {
            Path base = stop ? RegionCapture.stop() : RegionCapture.export();
            feedback(context, "§aRegion capture " + (stop ? "stopped" : "saved") + ": §f" + base + ".json §7and §f.yml");
            return 1;
        } catch (Exception exception) {
            feedback(context, "§c" + exception.getMessage());
            return 0;
        }
    }

    private static int status(CommandContext<FabricClientCommandSource> context) {
        if (!RegionCapture.isActive()) {
            feedback(context, "§cNo region capture is running. Start one with §f/regioncapture start <name>§c.");
            return 0;
        }
        feedback(context, RegionCapture.describe());
        return 1;
    }

    private static int mark(CommandContext<FabricClientCommandSource> context, String text) {
        try {
            RegionCapture.note(text);
            feedback(context, "§aMarked: §f" + text);
            return 1;
        } catch (IllegalStateException exception) {
            feedback(context, "§c" + exception.getMessage());
            return 0;
        }
    }

    private static void feedback(CommandContext<FabricClientCommandSource> context, String message) {
        context.getSource().sendFeedback(Component.literal(message));
    }
}
