package gg.itzkatze.thehypixelrecreationmod.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import gg.itzkatze.thehypixelrecreationmod.features.schematic.SchematicExporter;
import gg.itzkatze.thehypixelrecreationmod.features.schematic.SchematicSelection;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public final class SchematicCommand {
    private SchematicCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> {
            dispatcher.register(build());
            RecreationCommand.register(dispatcher, build());
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> build() {
        return ClientCommands.literal("schematic")
            .then(corner("pos1", true))
            .then(corner("pos2", false))
            .then(ClientCommands.literal("box").executes(context -> {
                boolean enabled = SchematicSelection.togglePreview();
                context.getSource().sendFeedback(Component.literal(enabled
                    ? "§aSelection preview enabled."
                    : "§eSelection preview disabled."));
                return 1;
            }))
            .then(ClientCommands.literal("status").executes(SchematicCommand::status))
            .then(ClientCommands.literal("clear").executes(context -> {
                SchematicSelection.clear();
                context.getSource().sendFeedback(Component.literal("§eSchematic selection cleared."));
                return 1;
            }))
            .then(ClientCommands.literal("save")
                .then(ClientCommands.argument("name", StringArgumentType.word())
                    .executes(context -> save(context, StringArgumentType.getString(context, "name")))));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> corner(String name, boolean first) {
        return ClientCommands.literal(name)
            .executes(context -> {
                Minecraft client = Minecraft.getInstance();
                if (client.player == null || client.level == null) {
                    context.getSource().sendFeedback(Component.literal("§cNo world is currently loaded."));
                    return 0;
                }
                return setCorner(context, first, SchematicSelection.defaultTarget(client));
            })
            .then(ClientCommands.argument("x", IntegerArgumentType.integer())
                .then(ClientCommands.argument("y", IntegerArgumentType.integer())
                    .then(ClientCommands.argument("z", IntegerArgumentType.integer())
                        .executes(context -> setCorner(context, first, new BlockPos(
                            IntegerArgumentType.getInteger(context, "x"),
                            IntegerArgumentType.getInteger(context, "y"),
                            IntegerArgumentType.getInteger(context, "z")
                        ))))));
    }

    private static int setCorner(CommandContext<FabricClientCommandSource> context, boolean first, BlockPos pos) {
        if (first) {
            SchematicSelection.setPos1(pos);
        } else {
            SchematicSelection.setPos2(pos);
        }
        context.getSource().sendFeedback(Component.literal(
            "§aSchematic " + (first ? "pos1" : "pos2") + " set to " + SchematicSelection.describe(pos)
        ));
        return 1;
    }

    private static int status(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        source.sendFeedback(Component.literal("§ePos1: " + SchematicSelection.describe(SchematicSelection.pos1())));
        source.sendFeedback(Component.literal("§ePos2: " + SchematicSelection.describe(SchematicSelection.pos2())));
        if (!SchematicSelection.isComplete()) {
            return 1;
        }

        SchematicExporter.Bounds bounds = SchematicExporter.Bounds.of(SchematicSelection.pos1(), SchematicSelection.pos2());
        source.sendFeedback(Component.literal(
            "§eSize: " + bounds.width() + " x " + bounds.height() + " x " + bounds.length()
                + " = " + bounds.volume() + " blocks (limit " + SchematicExporter.MAX_VOLUME + ")"
        ));
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return 1;
        }
        int unloaded = SchematicExporter.countUnloadedChunks(client.level, bounds);
        source.sendFeedback(Component.literal(
            (unloaded == 0 ? "§a" : "§c") + unloaded + " of " + bounds.chunkCount() + " chunks in the box are not loaded"
        ));
        return 1;
    }

    private static int save(CommandContext<FabricClientCommandSource> context, String name) {
        FabricClientCommandSource source = context.getSource();
        try {
            SchematicExporter.SaveResult result = SchematicExporter.save(name);
            source.sendFeedback(Component.literal(
                "§aSaved " + result.schematicPath().getFileName() + " and " + result.metadataPath().getFileName()
                    + ": " + result.blockCount() + " blocks (" + result.nonAirBlockCount() + " non-air), "
                    + result.blockEntityCount() + " block entities, " + result.entityCount() + " entities, "
                    + result.paletteSize() + " palette entries, " + result.bytes() + " bytes"
            ));
            if (result.unloadedChunkCount() > 0) {
                source.sendFeedback(Component.literal(
                    "§c§lWARNING: " + result.unloadedChunkCount()
                        + " chunk(s) in the box were not loaded and were exported as air."
                ));
            }
            return 1;
        } catch (Exception exception) {
            source.sendFeedback(Component.literal("§cFailed to save schematic: " + exception.getMessage()));
            return 0;
        }
    }
}
