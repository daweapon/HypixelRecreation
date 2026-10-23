package gg.itzkatze.thehypixelrecreationmod.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import gg.itzkatze.thehypixelrecreationmod.features.fullcapture.ModelCollector;
import gg.itzkatze.thehypixelrecreationmod.utils.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;

public final class ModelCaptureCommand {
    private static final double DEFAULT_RADIUS = 24;

    private ModelCaptureCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> RecreationCommand.register(dispatcher,
                ClientCommands.literal("capturemodels")
                        .executes(context -> execute(DEFAULT_RADIUS))
                        .then(ClientCommands.argument("radius", DoubleArgumentType.doubleArg(0))
                                .executes(context -> execute(DoubleArgumentType.getDouble(context, "radius"))))));
    }

    private static int execute(double radius) {
        try {
            ChatUtils.send(net.minecraft.network.chat.Component.literal("Models saved: " + ModelCollector.captureNearby(radius)));
            return 1;
        } catch (java.io.IOException exception) {
            ChatUtils.warn("Model capture failed: " + exception.getMessage());
            return 0;
        }
    }
}
