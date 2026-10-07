package net.swofty.type.skyblockgeneric.spray;

import lombok.Getter;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeSprayCan;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/** What one Spray Can holds: its Ink Storage and the spray (and arrow direction) it has selected. */
@Getter
public final class SprayCanData {
    private static final String ATTRIBUTE = "spray_can";

    private final Map<SprayInk, Integer> ink = new EnumMap<>(SprayInk.class);
    private @Nullable String selectedSpray;
    private SprayDirection direction = SprayDirection.RIGHT;

    public static SprayCanData of(SkyBlockItem item) {
        SprayCanData data = new SprayCanData();
        String raw = ((ItemAttributeSprayCan) item.getAttribute(ATTRIBUTE)).getValue();
        if (raw == null || raw.isBlank()) return data;
        JSONObject json = new JSONObject(raw);
        JSONObject ink = json.optJSONObject("ink");
        if (ink != null) {
            for (SprayInk dye : SprayInk.values()) {
                int percent = ink.optInt(dye.name(), 0);
                if (percent > 0) data.ink.put(dye, Math.min(SprayInk.MAX_PERCENT, percent));
            }
        }
        data.selectedSpray = json.optString("spray", null);
        data.direction = parseDirection(json.optString("direction", null));
        return data;
    }

    public void writeTo(SkyBlockItem item) {
        JSONObject json = new JSONObject();
        JSONObject inkJson = new JSONObject();
        ink.forEach((dye, percent) -> inkJson.put(dye.name(), percent.intValue()));
        json.put("ink", inkJson);
        if (selectedSpray != null) json.put("spray", selectedSpray);
        json.put("direction", direction.name());
        ((ItemAttributeSprayCan) item.getAttribute(ATTRIBUTE)).setValue(json.toString());
    }

    public int inkOf(SprayInk dye) {
        return ink.getOrDefault(dye, 0);
    }

    /** Adds up to {@code dyes} percent of ink and returns how many dyes were used. */
    public int refill(SprayInk dye, int dyes) {
        int used = Math.max(0, Math.min(dyes, SprayInk.MAX_PERCENT - inkOf(dye)));
        if (used > 0) ink.put(dye, inkOf(dye) + used);
        return used;
    }

    public boolean canAfford(Spray spray) {
        return spray.ink().entrySet().stream().allMatch(cost -> inkOf(cost.getKey()) >= cost.getValue());
    }

    public void spend(Spray spray) {
        spray.ink().forEach((dye, cost) -> ink.put(dye, inkOf(dye) - cost));
    }

    public Optional<Spray> selected() {
        return SprayCatalogue.byId(selectedSpray).filter(Spray::isUnlocked);
    }

    public void select(Spray spray, SprayDirection direction) {
        this.selectedSpray = spray.id();
        this.direction = direction;
    }

    private static SprayDirection parseDirection(@Nullable String name) {
        if (name == null) return SprayDirection.RIGHT;
        try {
            return SprayDirection.valueOf(name);
        } catch (IllegalArgumentException e) {
            return SprayDirection.RIGHT;
        }
    }
}
