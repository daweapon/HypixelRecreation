package dev.daweapon.type.catacombs.engine;

import net.minestom.server.entity.EntityType;

final class MobEntityTypes {
    private MobEntityTypes() {
    }

    static EntityType of(String mobId) {
        if (mobId.contains("SKELETON") || mobId.contains("SNIPER") || mobId.contains("SKELETOR") || mobId.contains("ARCHER")) {
            return EntityType.SKELETON;
        }
        if (mobId.contains("SPIDER")) return EntityType.SPIDER;
        if (mobId.equals("BAT")) return EntityType.BAT;
        return EntityType.ZOMBIE;
    }
}
