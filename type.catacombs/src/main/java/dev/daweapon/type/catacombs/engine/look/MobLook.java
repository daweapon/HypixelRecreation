package dev.daweapon.type.catacombs.engine.look;

import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * How one kind of dungeon mob looks on Hypixel: its entity (a player model is a skinned mannequin here), its mob types
 * for the name tag, name colour and style, optional level and header line, and one or more captured outfits (skin plus
 * equipment) of which each spawn wears one.
 */
public record MobLook(EntityType entity, List<MobType> types, String nameColour, boolean bold, @Nullable String displayName,
                      @Nullable Integer level, boolean showMaxHealth, @Nullable String header, List<Outfit> outfits) {

    public record Outfit(@Nullable Skin skin, Map<EquipmentSlot, ItemSpec> equipment) {
        public Outfit {
            equipment = Map.copyOf(equipment);
        }
    }

    public MobLook {
        types = List.copyOf(types);
        outfits = List.copyOf(outfits);
    }

    public boolean playerModel() {
        return entity == EntityType.MANNEQUIN;
    }

    public Outfit pick(RandomGenerator random) {
        if (outfits.isEmpty()) return new Outfit(null, Map.of());
        return outfits.get(random.nextInt(outfits.size()));
    }

    public MobLook withOutfit(Outfit outfit, String name) {
        return new MobLook(entity, types, nameColour, bold, name, level, showMaxHealth, header, List.of(outfit));
    }
}
