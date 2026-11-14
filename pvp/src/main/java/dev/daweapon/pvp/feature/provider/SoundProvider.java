package dev.daweapon.pvp.feature.provider;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import dev.daweapon.pvp.feature.CombatFeature;
import org.jetbrains.annotations.NotNull;

// Combat feature abstracting how sounds are played to an audience, so playback can be customized or intercepted.
public interface SoundProvider extends CombatFeature {
    SoundProvider DEFAULT = Audience::playSound;

    // TODO: support emitters
    void playSound(Audience audience, final @NotNull Sound original, final double x, final double y, final double z);
}
