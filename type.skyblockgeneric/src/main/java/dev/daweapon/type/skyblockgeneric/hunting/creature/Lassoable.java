package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.LivingEntity;

public interface Lassoable {
    String lassoName();

    int lassoBars();

    boolean lassoAllowed();

    void setHeader(String text);

    void capture(SkyBlockPlayer player);

    default LivingEntity entity() {
        return (LivingEntity) this;
    }
}
