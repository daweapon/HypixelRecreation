package net.swofty.type.skyblockgeneric.item.handlers.ability.abilities;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class JingleBellsAbility {
    private static final double RANGE = 10;
    private static final double MANA_FRACTION = 0.5;

    private JingleBellsAbility() {
    }

    public static boolean ring(SkyBlockPlayer player) {
        float cost = player.getMaxMana() * (float) MANA_FRACTION;
        if (player.getMana() < cost) {
            SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.MANA,
                    Text.of("<c><l>NOT ENOUGH MANA"), 40, 2);
            return false;
        }
        player.setMana(player.getMana() - cost);
        SkyBlockActionBar.getFor(player).addReplacement(SkyBlockActionBar.BarSection.MANA,
                Text.of("<b>-{} (<6>Jingle Bells</6>)", Math.round(cost)), 20, 2);

        for (Entity entity : player.getInstance().getNearbyEntities(player.getPosition(), RANGE)) {
            if (!(entity instanceof SkyBlockMob mob) || mob.isDead()) continue;
            mob.setTarget(player);
            mob.getNavigator().setPathTo(player.getPosition());
        }
        player.playSound(Sound.sound(Key.key("block.note_block.bell"), Sound.Source.PLAYER, 1f, 1.2f));
        return true;
    }
}
