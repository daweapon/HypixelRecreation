package net.swofty.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.ability.AbilityRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility.AbilityActivation;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility.AbilityManaCost;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredPassiveAbility;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.slayer.inferno.BlazeDagger;
import net.swofty.type.skyblockgeneric.slayer.inferno.BlazeFlares;
import net.swofty.type.skyblockgeneric.slayer.inferno.HellionAttunement;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Abilities of the Blaze Slayer gear: dagger attunement and its two on-hit abilities (which fire from
 * the Hellion Shield), the three flares and the Tactical Insertion.
 */
public final class BlazeSlayerAbilities {
    private static final int TACTICAL_RETURN_TICKS = 60;

    private BlazeSlayerAbilities() {
    }

    public static void register() {
        AbilityRegistry.register(new RegisteredAbility(
                "BLAZE_DAGGER_ATTUNEMENT",
                "Attunement",
                (_, item) -> attunementLore(item),
                AbilityActivation.RIGHT_CLICK,
                0,
                new RegisteredAbility.NoAbilityCost(),
                (player, item, _, _) -> swapAttunement(player, item)));

        AbilityRegistry.register(new RegisteredPassiveAbility(
                "BURNING_VENGEANCE",
                "Burning Vengeance",
                (_, _) -> "<7>On hit, <c>repeat <7>the last 8 hits of dealt damage after <a>6s <7>if the Hellion Shield has switched attunement.",
                List.of()));

        AbilityRegistry.register(new RegisteredPassiveAbility(
                "WRAITH_DRAIN",
                "Wraith Drain",
                (_, _) -> "<7>On hit, heal for <c>200 <7>+ <c>4%❤ <7>after <a>6s <7>if the Hellion Shield has switched attunement.",
                List.of()));

        AbilityRegistry.register(new RegisteredAbility(
                "DEPLOY_FLARE",
                "Deploy",
                (_, item) -> flareLore(item),
                AbilityActivation.RIGHT_CLICK,
                400,
                new AbilityManaCost(300),
                (player, item, _, _) -> deployFlare(player, item)));

        AbilityRegistry.register(new RegisteredAbility(
                "DEPLOY_SOS_FLARE",
                "Deploy",
                (_, item) -> flareLore(item),
                AbilityActivation.RIGHT_CLICK,
                200,
                new AbilityManaCost(250),
                (player, item, _, _) -> deployFlare(player, item)));

        AbilityRegistry.register(new RegisteredAbility(
                "GORILLA_TACTICS",
                "Gorilla Tactics",
                (_, _) -> "<7>Marks your location and teleport back there after <a>3s<7>. On coming back, set your <c>❤ Health <7>to HALF of what it was.",
                AbilityActivation.RIGHT_CLICK,
                400,
                new AbilityManaCost(200),
                (player, _, _, _) -> gorillaTactics(player)));
    }

    private static String attunementLore(SkyBlockItem item) {
        BlazeDagger dagger = BlazeDagger.of(item);
        if (dagger == null) return "<7>Swap attunement to attack through Hellion Shields.";
        HellionAttunement current = dagger.attunement(item);
        return "<7>Swap between " + dagger.first().coloured() + " <7>and " + dagger.second().coloured()
                + " <7>mode in order to attack through Hellion Shields.\n\n<7>Attuned: " + current.coloured();
    }

    private static boolean swapAttunement(SkyBlockPlayer player, SkyBlockItem item) {
        BlazeDagger dagger = BlazeDagger.of(item);
        if (dagger == null) return false;
        item.getAttributeHandler().setDaggerAttunement(item.getAttributeHandler().getDaggerAttunement() == 0 ? 1 : 0);
        player.setItemInMainHand(PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
        player.sendMessage("<7>Attuned to " + dagger.attunement(item).coloured() + "<7>.");
        return true;
    }

    private static String flareLore(SkyBlockItem item) {
        String base = "<7>Shoot the flare up in the sky for <a>3m <7>buffing up to <b>8 <7>players within <a>40 <7>blocks.";
        BlazeFlares.Flare flare = BlazeFlares.Flare.of(item.getAttributeHandler().getPotentialType());
        if (flare == null) return base;
        StringBuilder lore = new StringBuilder(base).append("\n\n").append(flare.displayName()).append(" Buff");
        switch (flare) {
            case WARNING -> lore.append("\n<7>• Gain <4>+10 Vitality<7>.\n<7>• Gain <f>+10 True Defense<7>.\n<7>• Heal <c>30❤ <7>per second while on the <c>Crimson Isle<7>.");
            case ALERT -> lore.append("\n<7>• Grants <b>+50% <7>base mana regen. <8>(not active yet)\n<7>• Gain <4>+15 Vitality<7>.\n<7>• Gain <f>+20 True Defense<7>.\n<7>• Heal <c>40❤ <7>per second while on the <c>Crimson Isle<7>.\n<7>• Gain <c>+10 Ferocity<7>.");
            case SOS -> lore.append("\n<7>• Grants <b>+125% <7>base mana regen. <8>(not active yet)\n<7>• Gain <4>+25 Vitality<7>.\n<7>• Gain <f>+30 True Defense<7>.\n<7>• Heal <c>50❤ <7>per second while on the <c>Crimson Isle<7>.\n<7>• Gain <c>+10 Ferocity<7>.\n<7>• Gain <e>+5 Attack Speed<7>.");
        }
        return lore.append("\n\n<8>Only one deployable buff applies.").toString();
    }

    private static boolean deployFlare(SkyBlockPlayer player, SkyBlockItem item) {
        BlazeFlares.Flare flare = BlazeFlares.Flare.of(item.getAttributeHandler().getPotentialType());
        return flare != null && BlazeFlares.deploy(player, flare);
    }

    private static boolean gorillaTactics(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return false;
        Pos mark = player.getPosition();
        player.sendMessage("<6>Gorilla Tactics! <7>Returning in <a>3s<7>.");
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!player.isOnline() || player.getInstance() != instance) return;
            player.teleport(mark);
            player.setHealth(Math.max(1F, player.getHealth() / 2F));
            ParticlePacket packet = new ParticlePacket(Particle.FLAME, true, true, mark.x(), mark.y() + 1, mark.z(),
                    1.5f, 0.5f, 1.5f, 0.02f, 40);
            instance.getPlayers().forEach(viewer -> viewer.sendPacket(packet));
        }).delay(TaskSchedule.tick(TACTICAL_RETURN_TICKS)).schedule();
        return true;
    }
}
