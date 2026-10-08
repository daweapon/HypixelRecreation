package net.swofty.type.skyblockgeneric.accessory.effect;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public record AccessoryEffect(AccessoryEffectKind kind,
                              List<String> text,
                              ItemStatistics statistics,
                              AccessoryConditions conditions,
                              Set<MobType> mobTypes,
                              double percent,
                              @Nullable String handler,
                              @Nullable String reason) {

    public boolean appliesTo(SkyBlockPlayer player) {
        return kind.isActive() && conditions.test(player);
    }

    public boolean matches(@Nullable SkyBlockMob mob) {
        if (mobTypes.isEmpty()) return true;
        return mob != null && mob.getMobTypes().stream().anyMatch(mobTypes::contains);
    }
}
