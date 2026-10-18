package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.AbilityRegistry;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility.AbilityActivation;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility.AbilityManaCost;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredPassiveAbility;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazeDagger;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazeFlares;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.BlazetekkHamRadio;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.HellionAttunement;
import dev.daweapon.type.skyblockgeneric.slayer.inferno.TacticalInsertion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * Abilities of the Blaze Slayer gear: dagger attunement and its two on-hit abilities (which fire from
 * the Hellion Shield), the three flares and the Tactical Insertion.
 */
public final class BlazeSlayerAbilities {

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

        AbilityRegistry.register(new RegisteredPassiveAbility(
                "TELECOMMUNICATIONS",
                "Telecommunications",
                (_, _) -> "<7>If exactly two players within <a>110 <7>blocks are on the same channel, both gain:\n <a>+60 Defense\n <f>+40 True Defense\n <3>+5☯ Combat Wisdom\n <4>+10 Vitality\n<8>Earn half stats while alone!",
                List.of()));

        AbilityRegistry.register(new RegisteredAbility(
                "TUNING_4_DUMMIES",
                "Tuning 4 Dummies",
                (_, item) -> "<7>Switches to the next radio channel.\n\n<7>Channel: <a>" + BlazetekkHamRadio.channelName(item.getAttributeHandler().getRadioChannel()),
                AbilityActivation.RIGHT_CLICK,
                0,
                new RegisteredAbility.NoAbilityCost(),
                (player, item, _, _) -> tuneRadio(player, item)));

        BlazetekkHamRadio.start();

        AbilityRegistry.register(new RegisteredAbility(
                "GORILLA_TACTICS",
                "Gorilla Tactics",
                (_, _) -> "<7>Marks your location and teleport back there after <a>3s<7>. On coming back, set your <c>❤ Health <7>to HALF of what it was and burn enemies within <a>3 <7>blocks for <a>10% <7>of all damage you dealt in the last <a>3s <7>over <a>6s<7>.",
                AbilityActivation.RIGHT_CLICK,
                400,
                new AbilityManaCost(200),
                (player, _, _, _) -> TacticalInsertion.begin(player)));
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
            case ALERT -> lore.append("\n<7>• Grants <b>+50% <7>base mana regen.\n<7>• Gain <4>+15 Vitality<7>.\n<7>• Gain <f>+20 True Defense<7>.\n<7>• Heal <c>40❤ <7>per second while on the <c>Crimson Isle<7>.\n<7>• Gain <c>+10 Ferocity<7>.");
            case SOS -> lore.append("\n<7>• Grants <b>+125% <7>base mana regen.\n<7>• Gain <4>+25 Vitality<7>.\n<7>• Gain <f>+30 True Defense<7>.\n<7>• Heal <c>50❤ <7>per second while on the <c>Crimson Isle<7>.\n<7>• Gain <c>+10 Ferocity<7>.\n<7>• Gain <e>+5 Attack Speed<7>.");
        }
        return lore.append("\n\n<8>Only one deployable buff applies.").toString();
    }

    private static boolean deployFlare(SkyBlockPlayer player, SkyBlockItem item) {
        BlazeFlares.Flare flare = BlazeFlares.Flare.of(item.getAttributeHandler().getPotentialType());
        return flare != null && BlazeFlares.deploy(player, flare);
    }

    private static boolean tuneRadio(SkyBlockPlayer player, SkyBlockItem item) {
        int channel = BlazetekkHamRadio.nextChannel(item.getAttributeHandler().getRadioChannel());
        item.getAttributeHandler().setRadioChannel(channel);
        player.setItemInMainHand(PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
        player.sendMessage("<7>Tuned to <a>" + BlazetekkHamRadio.channelName(channel) + "<7>.");
        return true;
    }
}
