package dev.daweapon.type.ravengardgeneric.user;

import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.ravengardgeneric.classes.RavengardClass;
import dev.daweapon.type.ravengardgeneric.data.RavengardDataHandler;
import dev.daweapon.type.ravengardgeneric.region.RavengardRegion;
import dev.daweapon.type.ravengardgeneric.data.datapoints.DatapointRavengardBoolean;
import dev.daweapon.type.ravengardgeneric.data.datapoints.DatapointRavengardInteger;
import dev.daweapon.type.ravengardgeneric.data.datapoints.DatapointRavengardString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RavengardPlayer extends HypixelPlayer {
    public RavengardPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
        super(playerConnection, gameProfile);
    }

    public RavengardDataHandler getRavengardDataHandler() {
        return RavengardDataHandler.getUser(this.getUuid());
    }

    public boolean isBlockingWithShield() {
        var meta = (net.minestom.server.entity.metadata.LivingEntityMeta) getEntityMeta();
        if (!meta.isHandActive()) {
            return false;
        }
        var type = new dev.daweapon.type.ravengardgeneric.item.attribute
                .RavengardItemAttributeHandler(getItemInOffHand()).getType();
        var standard = type == null ? null : type.component(
                dev.daweapon.type.ravengardgeneric.item.components.StandardItemComponent.class);
        return standard != null && standard.isShield() && type.usableBy(getRavengardClass());
    }

    public @Nullable RavengardClass getRavengardClass() {
        RavengardDataHandler handler = getRavengardDataHandler();
        if (handler == null) {
            return null;
        }
        return RavengardClass.fromKey(handler
                .get(RavengardDataHandler.Data.CLASS, DatapointRavengardString.class)
                .getValue());
    }

    public void setRavengardClass(@Nullable RavengardClass value) {
        RavengardDataHandler handler = getRavengardDataHandler();
        if (handler == null) {
            return;
        }
        handler.get(RavengardDataHandler.Data.CLASS, DatapointRavengardString.class)
                .setValue(value == null ? "" : value.name());
    }

    public int getRavengardLevel() {
        RavengardDataHandler handler = getRavengardDataHandler();
        if (handler == null) {
            return 1;
        }
        Integer level = handler
                .get(RavengardDataHandler.Data.LEVEL, DatapointRavengardInteger.class)
                .getValue();
        return level == null ? 1 : level;
    }

    public @Nullable RavengardRegion getRegion() {
        return RavengardRegion.getRegionOfPosition(getPosition());
    }

    public @Nullable java.util.UUID getSelectedProfile() {
        RavengardDataHandler handler = getRavengardDataHandler();
        return handler == null ? null : handler.getCurrentProfileId();
    }

    public boolean isTutorial() {
        RavengardDataHandler handler = getRavengardDataHandler();
        if (handler == null) {
            return true;
        }
        Boolean value = handler
                .get(RavengardDataHandler.Data.IS_TUTORIAL, DatapointRavengardBoolean.class)
                .getValue();
        return value == null || value;
    }

    public void setTutorial(boolean value) {
        RavengardDataHandler handler = getRavengardDataHandler();
        if (handler == null) {
            return;
        }
        handler.get(RavengardDataHandler.Data.IS_TUTORIAL, DatapointRavengardBoolean.class).setValue(value);
    }

    public int getCrowns() {
        RavengardDataHandler handler = getRavengardDataHandler();
        if (handler == null) {
            return 0;
        }
        Integer value = handler
                .get(RavengardDataHandler.Data.CROWNS, DatapointRavengardInteger.class)
                .getValue();
        return value == null ? 0 : value;
    }

    public void setCrowns(int value) {
        RavengardDataHandler handler = getRavengardDataHandler();
        if (handler == null) {
            return;
        }
        handler.get(RavengardDataHandler.Data.CROWNS, DatapointRavengardInteger.class).setValue(value);
    }

    public void setRavengardLevel(int value) {
        RavengardDataHandler handler = getRavengardDataHandler();
        if (handler == null) {
            return;
        }
        handler.get(RavengardDataHandler.Data.LEVEL, DatapointRavengardInteger.class).setValue(value);
    }
}
