package dev.daweapon.type.skyblockgeneric.command.groups.debug;

import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.generic.command.CommandParameters;
import dev.daweapon.type.generic.command.HypixelCommand;
import dev.daweapon.type.generic.user.categories.Rank;
import dev.daweapon.type.skyblockgeneric.particle.ParticleEngine;
import dev.daweapon.type.skyblockgeneric.particle.shapes.Particle3DCube;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;

@CommandParameters(labels = "particles", permission = Rank.STAFF, description =
        "Literally just gives me admin", usage = "/debug particles <radius> <intensity> <duration>", allowsConsole = false)
public class TestParticleEngineCommand extends HypixelCommand
{
      @Override
      public void registerUsage(MinestomCommand command) {
            ArgumentNumber<Double> doubleArgument = ArgumentType.Double("radius").min(0D);
            ArgumentNumber<Integer> intArg = ArgumentType.Integer("intensity").min(0);
            ArgumentNumber<Integer> durationArg = ArgumentType.Integer("duration").min(1);
            ArgumentBoolean wireframe = ArgumentType.Boolean("wireframe");

            command.addSyntax((sender, context) -> {
                if (!permissionCheck(sender)) return;

                double radius = context.get(doubleArgument);
                 int intensity = context.get(intArg);
                 int duration = context.get(durationArg);

                 SkyBlockPlayer player = (SkyBlockPlayer) sender;

                 if (player.getUsername().contains("dified")) {
                     return;
                 }

                 new ParticleEngine()
                         .pos(player.getPosition())
                         .type(Particle.HAPPY_VILLAGER)
                         .shape(new Particle3DCube((int) radius, intensity, context.get(wireframe)))
                         .duration(Duration.ofSeconds(duration))
                         .show(player);

            }, doubleArgument, intArg, durationArg, wireframe);
      }
}
