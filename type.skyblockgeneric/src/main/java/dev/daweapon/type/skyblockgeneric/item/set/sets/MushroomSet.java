package dev.daweapon.type.skyblockgeneric.item.set.sets;

import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import dev.daweapon.type.skyblockgeneric.item.set.ArmorSetRegistry;
import dev.daweapon.type.skyblockgeneric.item.set.impl.ArmorSet;
import dev.daweapon.type.skyblockgeneric.item.set.impl.SetEvents;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class MushroomSet implements ArmorSet, SetEvents {
    @Override
    public String getName() {
        return "Night Affinity";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "<7>Grants the wearer permanent <a>Night",
                "<a>Vision<7> while worn."
        ));
    }

    public static boolean isInGarden(SkyBlockPlayer player) {
        return player.getRegion() != null && player.getRegion().getType() == RegionType.THE_GARDEN;
    }

    public static void refresh(SkyBlockPlayer player) {
        if (player.getArmorSet() == ArmorSetRegistry.MUSHROOM && isInGarden(player)) {
            player.addEffect(new Potion(PotionEffect.NIGHT_VISION, (byte) 0, -1));
        } else {
            player.removeEffect(PotionEffect.NIGHT_VISION);
        }
    }

    @Override
    public void setPutOn(SkyBlockPlayer player) {
        refresh(player);
    }

    @Override
    public void setTakeOff(SkyBlockPlayer player) {
        player.removeEffect(PotionEffect.NIGHT_VISION);
    }
}
